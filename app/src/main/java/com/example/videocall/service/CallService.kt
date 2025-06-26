package com.example.videocall.service

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.Builder
import androidx.core.app.ServiceCompat
import androidx.core.net.toUri
import com.example.videocall.Constants
import com.example.videocall.MainActivity
import com.example.videocall.PeerConnectionUtils
import com.example.videocall.R
import com.example.videocall.data.local.UserProfilePreference
import com.example.videocall.data.model.UserProfile
import com.example.videocall.socket.createSocketConnection
import com.example.videocall.socket.setupEventListeners
import com.example.videocall.socket.setupOnSocketConnect
import dagger.hilt.android.AndroidEntryPoint
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.mediasoup.droid.Device
import org.mediasoup.droid.MediasoupClient
import org.mediasoup.droid.Producer
import org.mediasoup.droid.RecvTransport
import org.mediasoup.droid.SendTransport
import org.webrtc.AudioTrack
import org.webrtc.VideoTrack
import javax.inject.Inject
import kotlin.properties.Delegates


@AndroidEntryPoint
class CallService : Service() {
    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
    }

    @Inject
    lateinit var userProfilePreference: UserProfilePreference
    private val serviceJob = SupervisorJob()
    internal val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private val binder = LocalBinder()
    private var started = false
    private var foregroundServiceType: Int = 0
    private var roomId by Delegates.notNull<Int>()
    private var roomName: String = ""
    lateinit var userProfile: UserProfile
    private lateinit var audioManager: AudioManager

    @Inject
    lateinit var peerConnectionUtils: PeerConnectionUtils
    internal lateinit var io: Socket
    internal lateinit var mediasoupDevice: Device
    internal lateinit var sendTransport: SendTransport
    internal lateinit var receiveTransport: RecvTransport

    var audioTrack: AudioTrack? = null
    var videoTrack: VideoTrack? = null
    private var videoProducer: Producer? = null
    private var audioProducer: Producer? = null
    var dataFlow = MutableStateFlow(CallData())
        private set

    inner class LocalBinder : Binder() {
        fun getService(): CallService = this@CallService
    }

    private val audioDeviceCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<AudioDeviceInfo>) {
            updateAudioRoute()
        }

        override fun onAudioDevicesRemoved(removedDevices: Array<AudioDeviceInfo>) {
            updateAudioRoute()
        }
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        when (intent.action) {
            ACTION_START -> {
                if (started) return super.onStartCommand(intent, flags, startId)
                started = true
                roomId = intent.getIntExtra("roomId", -1)
                roomName = intent.getStringExtra("roomName") ?: ""
                if (roomId == -1) throw IllegalArgumentException("roomId is missing")

                audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
                audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
                audioManager.registerAudioDeviceCallback(
                    audioDeviceCallback,
                    Handler(Looper.getMainLooper())
                )
                audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
                audioManager.setSpeakerphoneOn(true)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    foregroundServiceType =
                        foregroundServiceType or ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                }
                upgradeToForeground(foregroundServiceType)

                serviceScope.launch {
                    userProfile = userProfilePreference.userProfile.first()
                    MediasoupClient.initialize(this@CallService)
                    peerConnectionUtils.createPeerConnectionFactory(this@CallService)
                    mediasoupDevice = Device()
                    createSocketConnection(roomId, userProfile.token)
                    setupOnSocketConnect()
                    setupEventListeners()
                    io.connect()
                }
            }

            ACTION_STOP -> stopSelf()
        }

        return super.onStartCommand(intent, flags, startId)
    }

    fun toggleVideo() {
        if (foregroundServiceType and ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA == 0)
            upgradeToCamera()
        if (!this::sendTransport.isInitialized) return
        if (dataFlow.value.isVideoOn) {
            videoProducer?.pause()
            videoTrack?.setEnabled(false)
            peerConnectionUtils.stopCapture()
            dataFlow.value = dataFlow.value.copy(isVideoOn = false)
            io.emit("clientVideoToggle", false)
            return
        }

        serviceScope.launch {
            if (videoTrack == null)
                videoTrack = peerConnectionUtils.createVideoTrack(this@CallService)
            if (videoProducer == null) {
                videoProducer = sendTransport.produce({
                    videoProducer = null
                }, videoTrack, null, null, null)
            }
            peerConnectionUtils.startCapture()
            videoTrack?.setEnabled(true)
            dataFlow.value = dataFlow.value.copy(isVideoOn = true)
            io.emit("clientVideoToggle", true)
        }
    }

    fun toggleAudio() {
        if (foregroundServiceType and ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE == 0)
            upgradeToMicrophone()
        if (!this::sendTransport.isInitialized) return
        if (dataFlow.value.isMicOn) {
            audioProducer?.pause()
            audioTrack?.setEnabled(false)
            dataFlow.value = dataFlow.value.copy(isMicOn = false)
            io.emit("clientMicToggle", false)
            return
        }

        serviceScope.launch {
            if (audioTrack == null)
                audioTrack = peerConnectionUtils.createAudioTrack(this@CallService)
            if (audioProducer == null) {
                audioProducer = sendTransport.produce({
                    audioProducer = null
                }, audioTrack, null, null, null)
            }
            audioTrack?.setEnabled(true)
            dataFlow.value = dataFlow.value.copy(isMicOn = true)
            io.emit("clientMicToggle", true)
        }
    }

    fun switchCamera() {
        peerConnectionUtils.switchCamera()
    }

    override fun onDestroy() {
        super.onDestroy()
        audioManager.unregisterAudioDeviceCallback(audioDeviceCallback)
        audioManager.mode = AudioManager.MODE_NORMAL
        audioManager.setSpeakerphoneOn(false)
        serviceJob.cancel()
        io.disconnect()
        io.close()
        sendTransport.close()
        receiveTransport.close()
        peerConnectionUtils.dispose()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun upgradeToForeground(foregroundServiceType: Int) {
        val deepLinkUri = "${Constants.CALL_DEEP_LINK}/${roomId}/${roomName}".toUri()

        val intent = Intent(
            Intent.ACTION_VIEW,
            deepLinkUri,
            this,
            MainActivity::class.java
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = Builder(this, Constants.VIDEO_CALL_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("In Video Call")
            .setContentText("Click here to return to call")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .build()

        ServiceCompat.startForeground(
            this,
            Constants.VIDEO_CALL_NOTIFICATION_ID,
            notification,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                foregroundServiceType
            else 0
        )
    }

    private fun upgradeToCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            foregroundServiceType =
                foregroundServiceType or ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA
        }
        upgradeToForeground(foregroundServiceType)
    }

    private fun upgradeToMicrophone() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            foregroundServiceType =
                foregroundServiceType or ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
        }
        upgradeToForeground(foregroundServiceType)
    }

    private fun updateAudioRoute() {
        val audioDevices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        var isHeadsetOrBluetoothConnected = false
        for (device in audioDevices) {
            when (device.type) {
                AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
                AudioDeviceInfo.TYPE_WIRED_HEADSET,
                AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
                    -> {
                    isHeadsetOrBluetoothConnected = true
                    break
                }

                else -> Unit
            }
        }
        audioManager.setSpeakerphoneOn(!isHeadsetOrBluetoothConnected)
    }
}