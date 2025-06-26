package com.example.videocall

import android.content.Context
import dagger.hilt.android.scopes.ServiceScoped
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.Camera1Enumerator
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraVideoCapturer
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnectionFactory
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoSource
import org.webrtc.VideoTrack
import org.webrtc.audio.JavaAudioDeviceModule
import javax.inject.Inject

@ServiceScoped
class PeerConnectionUtils @Inject constructor(private val eglBase: EglBase) {
    lateinit var factory: PeerConnectionFactory
    private lateinit var audioSource: AudioSource
    private lateinit var videoSource: VideoSource
    private lateinit var camCapture: CameraVideoCapturer

    fun createPeerConnectionFactory(context: Context) {
        factory = PeerConnectionFactory.builder()
            .setAudioDeviceModule(JavaAudioDeviceModule.builder(context).createAudioDeviceModule())
            .setVideoEncoderFactory(DefaultVideoEncoderFactory(eglBase.eglBaseContext, true, true))
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglBase.eglBaseContext))
            .createPeerConnectionFactory()
    }

    private fun createAudioSource(context: Context) {
        if (!this::factory.isInitialized) createPeerConnectionFactory(context)
        audioSource = factory.createAudioSource(MediaConstraints())
    }

    private fun createCamCapture(context: Context) {
        val cameraEnumerator =
            if (Camera2Enumerator.isSupported(context))
                Camera2Enumerator(context)
            else
                Camera1Enumerator()

        for (deviceName in cameraEnumerator.deviceNames) {
            if (cameraEnumerator.isFrontFacing(deviceName)) {
                camCapture = cameraEnumerator.createCapturer(deviceName, null)
            }
        }
    }

    private fun createVideoSource(context: Context) {
        if (!this::factory.isInitialized) createPeerConnectionFactory(context)
        if (!this::camCapture.isInitialized)
            createCamCapture(context)
        videoSource = factory.createVideoSource(camCapture.isScreencast)
        val surfaceTextureHelper =
            SurfaceTextureHelper.create("CaptureThread", eglBase.eglBaseContext)
        camCapture.initialize(surfaceTextureHelper, context, videoSource.capturerObserver)
    }

    fun createAudioTrack(context: Context): AudioTrack {
        if (!this::factory.isInitialized) createPeerConnectionFactory(context)
        if (!this::audioSource.isInitialized) createAudioSource(context)
        return factory.createAudioTrack("audio", audioSource).apply { setEnabled(false) }
    }

    fun createVideoTrack(context: Context): VideoTrack {
        if (!this::factory.isInitialized) createPeerConnectionFactory(context)
        if (!this::videoSource.isInitialized) createVideoSource(context)
        return factory.createVideoTrack("videoFront", videoSource).apply { setEnabled(false) }
    }

    fun startCapture() {
        camCapture.startCapture(640, 480, 30)
    }

    fun stopCapture() {
        camCapture.stopCapture()
    }

    fun switchCamera() {
        if (!this::camCapture.isInitialized) return
        camCapture.switchCamera(null)
    }

    fun dispose() {
        if (this::camCapture.isInitialized) camCapture.dispose()
        if (this::audioSource.isInitialized) audioSource.dispose()
        if (this::videoSource.isInitialized) videoSource.dispose()
        if (this::factory.isInitialized) factory.dispose()
    }
}