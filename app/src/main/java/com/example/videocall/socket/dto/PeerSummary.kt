package com.example.videocall.socket.dto

import kotlinx.serialization.Serializable

@Serializable
data class PeerSummary(
    val socketId: String,
    val name: String,
    val email: String,
    val isMicOn: Boolean,
    val isVideoOn: Boolean,
)
