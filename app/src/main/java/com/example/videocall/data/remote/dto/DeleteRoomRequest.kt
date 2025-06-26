package com.example.videocall.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class DeleteRoomRequest(
    val roomId: Int
)
