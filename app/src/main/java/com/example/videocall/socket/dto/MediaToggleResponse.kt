package com.example.videocall.socket.dto

import kotlinx.serialization.Serializable

@Serializable
data class MediaToggleResponse(
    val socketId: String,
    val isMicOn: Boolean? = null,
    val isVideoOn: Boolean? = null,
)
