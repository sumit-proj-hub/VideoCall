package com.example.videocall.ui.screens.call

import com.example.videocall.service.CallPeer

data class CallUiState(
    val isMicOn: Boolean = false,
    val isVideoOn: Boolean = false,
    val hasCameraPermission: Boolean = false,
    val hasMicrophonePermission: Boolean = false,
    val callPeers: List<CallPeer> = emptyList(),
    val forceUpdate: Long = 0L
)
