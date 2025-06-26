package com.example.videocall.ui.screens.manageRooms

import com.example.videocall.data.remote.dto.Room

data class ManageRoomsUiState(
    val roomIdToDelete: Int? = null,
    val state: State = State.LOADING,
    val rooms: List<Room> = emptyList(),
) {
    enum class State {
        LOADING,
        NETWORK_ERROR,
        SUCCESS
    }
}
