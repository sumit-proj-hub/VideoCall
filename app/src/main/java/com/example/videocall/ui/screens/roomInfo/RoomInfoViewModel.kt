package com.example.videocall.ui.screens.roomInfo

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.videocall.Constants
import com.example.videocall.data.local.RecentRoom
import com.example.videocall.data.local.RecentRoomsPreference
import com.example.videocall.data.local.UserProfilePreference
import com.example.videocall.data.remote.Api
import com.example.videocall.data.remote.dto.CreateRoomRequest
import com.example.videocall.data.remote.dto.Room
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoomInfoViewModel @Inject constructor(
    private val api: Api,
    private val userProfilePreference: UserProfilePreference,
    private val recentRoomsPreference: RecentRoomsPreference,
) : ViewModel() {
    var uiState by mutableStateOf(RomInfoUiState())
        private set

    private fun initJoinRoom(roomId: Int) {
        uiState = uiState.copy(isHost = false, state = RomInfoUiState.State.LOADING)
        viewModelScope.launch {
            try {
                val response = api.getRoom(roomId.toString())
                if (response.code() != 200) {
                    uiState = uiState.copy(state = RomInfoUiState.State.ROOM_NOT_FOUND)
                    return@launch
                }
                uiState = uiState.copy(
                    state = RomInfoUiState.State.SUCCESS,
                    roomInfo = response.body()!!.room
                )
                recentRoomsPreference.addRoom(
                    RecentRoom(id = roomId, name = uiState.roomInfo!!.roomName)
                )
            } catch (e: Exception) {
                uiState = uiState.copy(state = RomInfoUiState.State.NETWORK_ERROR)
                Log.e("RoomInfoViewModel", e.stackTraceToString())
            }
        }
    }

    private fun initCreateRoom(roomName: String) {
        uiState = uiState.copy(isHost = true, state = RomInfoUiState.State.LOADING)
        viewModelScope.launch {
            val userProfile = userProfilePreference.userProfile.first()
            try {
                val response = api.createRoom(
                    "Bearer ${userProfile.token}",
                    CreateRoomRequest(roomName)
                )
                if (response.code() != 200) {
                    uiState = uiState.copy(state = RomInfoUiState.State.NETWORK_ERROR)
                    return@launch
                }
                uiState = uiState.copy(
                    state = RomInfoUiState.State.SUCCESS,
                    roomInfo = Room(
                        id = response.body()!!.roomId,
                        roomName = roomName,
                        roomOwnerName = userProfile.name,
                        roomOwnerEmail = userProfile.email,
                        createdOnEpoch = System.currentTimeMillis() / 1000.0
                    )
                )
                recentRoomsPreference.addRoom(
                    RecentRoom(id = uiState.roomInfo!!.id, name = uiState.roomInfo!!.roomName)
                )
            } catch (e: Exception) {
                uiState = uiState.copy(state = RomInfoUiState.State.NETWORK_ERROR)
                Log.e("RoomInfoViewModel", e.stackTraceToString())
            }
        }
    }

    fun load(roomId: Int?, roomName: String?) {
        uiState = uiState.copy(state = RomInfoUiState.State.LOADING)
        if (roomId != null)
            initJoinRoom(roomId)
        else if (roomName != null)
            initCreateRoom(roomName)
    }

    fun shareRoom(context: Context) {
        if (uiState.roomInfo == null) return
        val roomName = uiState.roomInfo!!.roomName
        val deepLink = "${Constants.ROOM_DEEP_LINK}/room/${uiState.roomInfo!!.id}"
        val sendText = "Video Call: $roomName\nJoin Now:\n$deepLink"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, sendText)
        }
        context.startActivity(Intent.createChooser(intent, "Share Room Link"))
    }
}