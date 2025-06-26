package com.example.videocall.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MergeType
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.videocall.R
import com.example.videocall.data.local.RecentRoom
import com.example.videocall.ui.components.LabelledIconButton
import com.example.videocall.ui.components.UnderlinedTopAppBar

@Composable
fun HomeScreen(
    onUserNotFound: () -> Unit,
    gotoRoomInfo: (Int?, String?) -> Unit,
    gotoLogin: () -> Unit,
    gotoManageRooms: () -> Unit,
    viewModel: HomeScreenViewModel = hiltViewModel(),
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val recentRooms by viewModel.recentRooms.collectAsState()
    val uiState = viewModel.uiState

    LaunchedEffect(userProfile) {
        if (userProfile != null && userProfile!!.token.isEmpty())
            onUserNotFound()
    }
    if (userProfile == null || userProfile!!.token.isEmpty())
        return

    Scaffold(
        topBar = {
            UnderlinedTopAppBar(
                title = "Video Call",
                actions = {
                    IconButton(onClick = { viewModel.openConfirmLogoutDialog() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Logout"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                Text(
                    text = "Welcome ${userProfile!!.name}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )

                ActionButtons(
                    onJoinClick = viewModel::onJoinClick,
                    onCreateClick = viewModel::onCreateClick,
                    onManageClick = gotoManageRooms,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Recent Meetings",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )

                RecentMeetings(
                    recentRooms = recentRooms ?: emptyList(),
                    onJoin = { gotoRoomInfo(it, null) },
                    modifier = Modifier
                        .padding(horizontal = 28.dp)
                        .weight(1f)
                )
            }
            CreateRoomDialog(
                isDialogOpen = uiState.isCreateDialogOpen,
                roomName = uiState.roomName,
                onDismiss = viewModel::dismissDialogs,
                onCreate = { viewModel.createRoom(gotoRoomInfo) },
                onRoomNameChange = viewModel::onRoomNameChange
            )
            JoinRoomDialog(
                isDialogOpen = uiState.isJoinDialogOpen,
                roomId = uiState.roomJoinId,
                onDismiss = viewModel::dismissDialogs,
                onJoin = { viewModel.joinRoom(gotoRoomInfo) },
                onRoomIdChange = viewModel::onRoomIdChange,
            )
            ConfirmLogoutDialog(
                isDialogOpen = uiState.isConfirmLogoutDialogOpen,
                onDismiss = viewModel::dismissDialogs,
                onConfirm = { viewModel.logout(gotoLogin) }
            )
        }
    }
}

@Preview
@Composable
private fun ActionButtons(
    modifier: Modifier = Modifier,
    onJoinClick: () -> Unit = {},
    onCreateClick: () -> Unit = {},
    onManageClick: () -> Unit = {},
) {
    Column(modifier) {
        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier.fillMaxWidth()
        ) {
            LabelledIconButton(
                icon = Icons.AutoMirrored.Filled.MergeType,
                text = "Join Room",
                modifier = Modifier.weight(1f),
                onClick = onJoinClick
            )
            LabelledIconButton(
                icon = Icons.Default.VideoCall,
                text = "Create Room",
                modifier = Modifier.weight(1f),
                onClick = onCreateClick
            )
        }
        LabelledIconButton(
            icon = Icons.Default.MeetingRoom,
            text = "Manage Rooms",
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 20.dp),
            onClick = onManageClick
        )
    }
}

@Composable
private fun RecentMeetings(
    recentRooms: List<RecentRoom>,
    onJoin: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (recentRooms.isEmpty()) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    painter = painterResource(R.drawable.empty_icon),
                    contentDescription = "Empty Rooms",
                    tint = Color.Gray,
                    modifier = Modifier.size(100.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No Recent Meetings",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Gray
                )
            }
        }
        return
    }
    LazyColumn(modifier) {
        items(recentRooms.size) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = recentRooms[it].name,
                    style = MaterialTheme.typography.titleLarge
                )
                TextButton(onClick = {
                    onJoin(recentRooms[it].id)
                }) {
                    Text("JOIN")
                }
            }
            HorizontalDivider()
        }
    }
}

@Preview
@Composable
private fun ConfirmLogoutDialog(
    isDialogOpen: Boolean = true,
    onDismiss: () -> Unit = {},
    onConfirm: () -> Unit = {},
) {
    if (!isDialogOpen) return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Logout") },
        text = { Text("Are you sure you want to logout?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Logout")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview
@Composable
private fun CreateRoomDialog(
    modifier: Modifier = Modifier,
    isDialogOpen: Boolean = true,
    roomName: String = "",
    onDismiss: () -> Unit = {},
    onCreate: () -> Unit = {},
    onRoomNameChange: (String) -> Unit = {},
) {
    if (!isDialogOpen) return
    AlertDialog(
        onDismissRequest = onDismiss,
        text = {
            OutlinedTextField(
                value = roomName,
                label = { Text("Room Name") },
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                onValueChange = onRoomNameChange,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        title = { Text("Create Room") },
        confirmButton = {
            TextButton(onClick = onCreate) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        modifier = modifier
    )
}

@Preview
@Composable
private fun JoinRoomDialog(
    modifier: Modifier = Modifier,
    isDialogOpen: Boolean = true,
    roomId: String = "",
    onDismiss: () -> Unit = {},
    onJoin: () -> Unit = {},
    onRoomIdChange: (String) -> Unit = {},
) {
    if (!isDialogOpen) return
    AlertDialog(
        onDismissRequest = onDismiss,
        text = {
            OutlinedTextField(
                value = roomId,
                label = { Text("Room Id") },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                onValueChange = onRoomIdChange,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        title = { Text("Join Room") },
        confirmButton = {
            TextButton(onClick = onJoin) {
                Text("Join")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        modifier = modifier
    )
}