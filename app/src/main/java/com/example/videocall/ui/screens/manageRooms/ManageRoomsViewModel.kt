package com.example.videocall.ui.screens.manageRooms

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.videocall.data.local.UserProfilePreference
import com.example.videocall.data.remote.Api
import com.example.videocall.data.remote.dto.DeleteRoomRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManageRoomsViewModel @Inject constructor(
    private val api: Api,
    private val userProfilePreference: UserProfilePreference,
) : ViewModel() {
    var uiState by mutableStateOf(ManageRoomsUiState())
        private set

    init {
        load()
    }

    fun load() {
        uiState = uiState.copy(state = ManageRoomsUiState.State.LOADING)
        viewModelScope.launch {
            val token = userProfilePreference.userProfile.first().token
            try {
                val response = api.getUserRooms("Bearer $token")
                if (response.code() != 200) {
                    uiState = uiState.copy(state = ManageRoomsUiState.State.NETWORK_ERROR)
                    return@launch
                }
                uiState = uiState.copy(
                    state = ManageRoomsUiState.State.SUCCESS,
                    rooms = response.body()!!.rooms
                )
            } catch (e: Exception) {
                uiState = uiState.copy(state = ManageRoomsUiState.State.NETWORK_ERROR)
                Log.e("ManageRoomsViewModel", e.stackTraceToString())
            }
        }
    }

    fun dismissDialog() {
        uiState = uiState.copy(roomIdToDelete = null)
    }

    fun deleteRoom(roomId: Int, showSnackBar: (String) -> Unit) {
        dismissDialog()
        viewModelScope.launch {
            val token = userProfilePreference.userProfile.first().token
            try {
                val response = api.deleteRoom("Bearer $token", DeleteRoomRequest(roomId))
                Log.d("ManageRoomsViewModel", "deleteRoom: ${response.code()}")
                if (response.code() != 200) {
                    showSnackBar("Failed to delete room")
                    return@launch
                }
                uiState = uiState.copy(rooms = uiState.rooms.filter { it.id != roomId })
                showSnackBar("Room Deleted")
            } catch (e: Exception) {
                showSnackBar("Network Error")
                Log.e("ManageRoomsViewModel", e.stackTraceToString())
            }
        }
    }

    fun showDeleteDialog(roomId: Int) {
        uiState = uiState.copy(roomIdToDelete = roomId)
    }
}