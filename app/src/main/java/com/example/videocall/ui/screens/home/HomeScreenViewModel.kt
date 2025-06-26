package com.example.videocall.ui.screens.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.videocall.data.local.RecentRoomsPreference
import com.example.videocall.data.local.UserProfilePreference
import com.example.videocall.data.model.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val userProfilePreference: UserProfilePreference,
    recentRoomsPreference: RecentRoomsPreference
) : ViewModel() {
    var uiState by mutableStateOf(HomeUiState())
        private set

    val userProfile = userProfilePreference.userProfile.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val recentRooms = recentRoomsPreference.recentRooms.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun createRoom(gotoRoomInfo: (Int?, String?) -> Unit) {
        val roomName = uiState.roomName
        if (roomName.isEmpty()) return
        dismissDialogs()
        gotoRoomInfo(null, roomName)
    }

    fun joinRoom(gotoRoomInfo: (Int?, String?) -> Unit) {
        val roomId = uiState.roomJoinId
        if (roomId.isEmpty()) return
        dismissDialogs()
        gotoRoomInfo(roomId.toIntOrNull(), null)
    }

    fun onJoinClick() {
        uiState = uiState.copy(isJoinDialogOpen = true)
    }

    fun onCreateClick() {
        uiState = uiState.copy(isCreateDialogOpen = true)
    }

    fun dismissDialogs() {
        uiState = uiState.copy(
            isCreateDialogOpen = false,
            isJoinDialogOpen = false,
            isConfirmLogoutDialogOpen = false,
            roomName = "",
            roomJoinId = ""
        )
    }

    fun onRoomNameChange(roomName: String) {
        uiState = uiState.copy(roomName = roomName)
    }

    fun onRoomIdChange(roomId: String) {
        if (roomId.isEmpty() || roomId.toIntOrNull() != null)
            uiState = uiState.copy(roomJoinId = roomId)
    }

    fun logout(onLogout: () -> Unit) {
        viewModelScope.launch {
            userProfilePreference.saveUserProfile(
                UserProfile(
                    name = "",
                    email = "",
                    token = ""
                )
            )
            onLogout()
        }
    }

    fun openConfirmLogoutDialog() {
        uiState = uiState.copy(isConfirmLogoutDialogOpen = true)
    }
}