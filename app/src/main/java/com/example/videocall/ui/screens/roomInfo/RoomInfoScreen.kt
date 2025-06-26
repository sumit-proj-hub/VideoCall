package com.example.videocall.ui.screens.roomInfo

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
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.videocall.ui.components.UnderlinedTopAppBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RoomInfoScreen(
    roomId: Int?,
    roomName: String?,
    gotoVideoCall: (Int, String) -> Unit,
    goBack: () -> Unit,
    viewModel: RoomInfoViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState
    LaunchedEffect(Unit) {
        viewModel.load(roomId, roomName)
    }
    Scaffold(
        topBar = { UnderlinedTopAppBar("Room Info", goBack = goBack) },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (uiState.state) {
                RomInfoUiState.State.LOADING -> LoadingScreen()
                RomInfoUiState.State.NETWORK_ERROR -> NetworkErrorScreen {
                    viewModel.load(roomId, roomName)
                }

                RomInfoUiState.State.ROOM_NOT_FOUND -> RoomNotFoundScreen(goBack = goBack)
                RomInfoUiState.State.SUCCESS -> {
                    val context = LocalContext.current
                    if (uiState.roomInfo == null) return@Box
                    Content(
                        isHost = uiState.isHost,
                        roomName = uiState.roomInfo.roomName,
                        roomId = uiState.roomInfo.id.toString(),
                        hostName = uiState.roomInfo.roomOwnerName,
                        hostEmail = uiState.roomInfo.roomOwnerEmail,
                        createdOn = epochToFormattedDate(uiState.roomInfo.createdOnEpoch),
                        onJoinClick = {
                            gotoVideoCall(uiState.roomInfo.id, uiState.roomInfo.roomName)
                        },
                        onShareClick = {
                            viewModel.shareRoom(context)
                        },
                        modifier = Modifier.padding(bottom = paddingValues.calculateTopPadding())
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun Content(
    modifier: Modifier = Modifier,
    isHost: Boolean = true,
    roomName: String = "Room Name",
    roomId: String = "1234",
    hostName: String = "Jack Smith",
    hostEmail: String = "jack@gmail.com",
    createdOn: String = "12 December, 2011",
    onShareClick: () -> Unit = {},
    onJoinClick: () -> Unit = {},
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isHost) "Room Created" else "Room Found",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(Modifier.height(8.dp))
        Text(text = roomName, style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(16.dp))
        Row {
            Column {
                KeyText("Room Id:")
                KeyText("Host:")
                KeyText("Host Email:")
                KeyText("Created On:")
            }
            Spacer(Modifier.width(24.dp))
            Column {
                ValueText(roomId)
                ValueText(hostName)
                ValueText(hostEmail)
                ValueText(createdOn)
            }
        }
        Spacer(Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            OutlinedButton(onClick = onShareClick, shape = MaterialTheme.shapes.small) {
                Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
                Text(text = "SHARE", modifier = Modifier.padding(start = 8.dp))
            }

            Button(onClick = onJoinClick, shape = MaterialTheme.shapes.small) {
                Icon(imageVector = Icons.Default.Videocam, contentDescription = "Join")
                Text(text = "JOIN", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Composable
private fun KeyText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.titleLarge,
        modifier = modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun ValueText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        modifier = modifier.padding(vertical = 8.dp)
    )
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

@Preview
@Composable
private fun RoomNotFoundScreen(modifier: Modifier = Modifier, goBack: () -> Unit = {}) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = "Network Error",
            tint = Color.Gray,
            modifier = Modifier.size(100.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text("Room Not Found", style = MaterialTheme.typography.titleMedium)
        TextButton(onClick = goBack) { Text("GO BACK") }
    }
}

private fun epochToFormattedDate(epochSeconds: Double): String {
    val date = Date((epochSeconds * 1000).toLong())
    val format = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    return format.format(date)
}