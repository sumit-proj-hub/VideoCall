package com.example.videocall.ui.screens.home

data class HomeUiState(
    val isCreateDialogOpen: Boolean = false,
    val isJoinDialogOpen: Boolean = false,
    val isConfirmLogoutDialogOpen: Boolean = false,
    val roomName: String = "",
    val roomJoinId: String = "",
)
