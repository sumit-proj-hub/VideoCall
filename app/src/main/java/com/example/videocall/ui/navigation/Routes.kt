package com.example.videocall.ui.navigation

import kotlinx.serialization.Serializable

object Routes {
    @Serializable
    object Home

    @Serializable
    object Login

    @Serializable
    object SignUp

    @Serializable
    data class ShowRoomInfo(val roomId: Int? = null, val roomName: String? = null)

    @Serializable
    data class VideoCallHome(val roomId: Int, val roomName: String)

    @Serializable
    object ManageRooms
}