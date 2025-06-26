package com.example.videocall.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Room(
    val id: Int,
    @SerialName("room_name") val roomName: String,
    @SerialName("room_owner_name") val roomOwnerName: String,
    @SerialName("room_owner_email") val roomOwnerEmail: String,
    @SerialName("created_on_epoch") val createdOnEpoch: Double,
)
