package com.example.videocall

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import dagger.hilt.android.HiltAndroidApp
import org.webrtc.EglBase
import javax.inject.Inject

@HiltAndroidApp
class VideoCallApplication : Application() {
    @Inject
    lateinit var eglBase: EglBase

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.VIDEO_CALL_CHANNEL_ID,
                Constants.VIDEO_CALL_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Used to show ongoing video call status while you're in a call."
            }

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        eglBase.release()
    }
}