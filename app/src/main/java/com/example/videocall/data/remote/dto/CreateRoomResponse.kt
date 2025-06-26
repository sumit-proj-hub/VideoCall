package com.example.videocall.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateRoomResponse(
    val roomId: Int,
    val success: Boolean,
)
