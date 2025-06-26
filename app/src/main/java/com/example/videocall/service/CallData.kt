package com.example.videocall.service

data class CallData(
    val isMicOn: Boolean = false,
    val isVideoOn: Boolean = false,
    var callPeers: MutableMap<String, CallPeer> = mutableMapOf(),
    val forceEmit: Long = 0L
)
