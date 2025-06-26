package com.example.videocall.service

import org.mediasoup.droid.Consumer
import org.webrtc.AudioTrack
import org.webrtc.VideoTrack

data class CallPeer(
    val socketId: String,
    val name: String,
    val email: String,
    var isMicOn: Boolean,
    var isVideoOn: Boolean,
    var audioConsumer: Consumer? = null,
    var videoConsumer: Consumer? = null,
    var audioTrack: AudioTrack? = null,
    var videoTrack: VideoTrack? = null,
)
