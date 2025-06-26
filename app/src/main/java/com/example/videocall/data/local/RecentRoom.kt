package com.example.videocall.data.local

import kotlinx.serialization.Serializable

@Serializable
data class RecentRoom(
    val id: Int,
    val name: String,
)
