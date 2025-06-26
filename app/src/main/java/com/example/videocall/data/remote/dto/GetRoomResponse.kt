package com.example.videocall.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class GetRoomResponse(
    val room: Room?,
    val success: Boolean,
)
