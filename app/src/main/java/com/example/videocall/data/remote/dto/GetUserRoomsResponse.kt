package com.example.videocall.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class GetUserRoomsResponse(
    val success: Boolean,
    val rooms: List<Room>,
)