package com.example.videocall.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateRoomRequest(
    val roomName: String,
)
