package com.example.videocall.ui.screens.roomInfo

import com.example.videocall.data.remote.dto.Room

data class RomInfoUiState(
    val roomInfo: Room? = null,
    val isHost: Boolean = false,
    val state: State = State.LOADING,
) {
    enum class State {
        LOADING,
        NETWORK_ERROR,
        ROOM_NOT_FOUND,
        SUCCESS,
    }
}
