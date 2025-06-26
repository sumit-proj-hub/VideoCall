package com.example.videocall.ui.screens.manageRooms

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.videocall.R
import com.example.videocall.data.remote.dto.Room
import com.example.videocall.ui.components.UnderlinedTopAppBar
import kotlinx.coroutines.launch

@Composable
fun ManageRoomsScreen(
    gotoRoomInfo: (Int?, String?) -> Unit,
    goBack: () -> Unit,
    viewModel: ManageRoomsViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState
    val snackBarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    Scaffold(
        topBar = { UnderlinedTopAppBar(title = "Manage Rooms", goBack = goBack) },
        snackbarHost = { SnackbarHost(snackBarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (uiState.state) {
                ManageRoomsUiState.State.LOADING -> LoadingScreen()
                ManageRoomsUiState.State.NETWORK_ERROR -> NetworkErrorScreen(onRetry = viewModel::load)
                ManageRoomsUiState.State.SUCCESS -> ManageRoomsContent(
                    rooms = uiState.rooms,
                    onJoin = { gotoRoomInfo(it, null) },
                    onDelete = { viewModel.showDeleteDialog(it) },
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .align(Alignment.TopCenter)
                )
            }
            ConfirmDeleteDialog(
                roomIdToDelete = uiState.roomIdToDelete,
                onDismiss = viewModel::dismissDialog,
                onConfirm = {
                    viewModel.deleteRoom(it) {
                        coroutineScope.launch { snackBarHostState.showSnackbar(it) }
                    }
                }
            )
        }
    }
}

@Preview
@Composable
private fun ConfirmDeleteDialog(
    roomIdToDelete: Int? = 0,
    onDismiss: () -> Unit = {},
    onConfirm: (Int) -> Unit = {},
) {
    if (roomIdToDelete == null) return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Delete") },
        text = { Text("Are you sure you want to delete this room?") },
        confirmButton = {
            TextButton(onClick = { onConfirm(roomIdToDelete) }) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ManageRoomsContent(
    rooms: List<Room>,
    onJoin: (Int) -> Unit,
    onDelete: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (rooms.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            EmptyRoomsScreen()
        }
        return
    }
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        items(rooms.size) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = rooms[it].roomName,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { onDelete(rooms[it].id) }) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = Color.Red
                    )
                }
                TextButton(onClick = { onJoin(rooms[it].id) }) {
                    Text("JOIN")
                }
            }
            HorizontalDivider()
        }
    }
}

@Preview
@Composable
private fun EmptyRoomsScreen(modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Icon(
            painter = painterResource(R.drawable.empty_icon),
            contentDescription = "Empty Rooms",
            tint = Color.Gray,
            modifier = Modifier.size(100.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "You had not created any rooms", style = MaterialTheme.typography.titleMedium)
    }
}

@Preview
@Composable
private fun LoadingScreen(modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(8.dp))
        Text("Loading", style = MaterialTheme.typography.titleMedium)
    }
}

@Preview
@Composable
private fun NetworkErrorScreen(modifier: Modifier = Modifier, onRetry: () -> Unit = {}) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Icon(
            imageVector = Icons.Default.CloudOff,
            contentDescription = "Network Error",
            tint = Color.Gray,
            modifier = Modifier.size(100.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text("Network Error", style = MaterialTheme.typography.titleMedium)
        TextButton(onClick = onRetry) { Text("RETRY") }
    }
}