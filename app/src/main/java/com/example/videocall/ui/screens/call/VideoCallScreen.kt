package com.example.videocall.ui.screens.call

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.os.IBinder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.videocall.service.CallPeer
import com.example.videocall.service.CallService
import com.example.videocall.ui.components.CallPeerView
import com.example.videocall.ui.components.SwipeGrid
import org.webrtc.EglBase

@Composable
fun VideoCallScreen(
    roomId: Int,
    roomName: String,
    goBack: () -> Unit,
    viewModel: CallViewModel = hiltViewModel(),
) {
    var callService: CallService? by remember { mutableStateOf(null) }
    val uiState = viewModel.uiState
    val context = LocalContext.current
    val intent = Intent(context, CallService::class.java).apply {
        putExtra("roomId", roomId)
        putExtra("roomName", roomName)
        action = CallService.ACTION_START
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if (it) {
            viewModel.setCameraPermission(true)
            viewModel.toggleVideo()
        }
    }

    val microphonePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if (it) {
            viewModel.setMicrophonePermission(true)
            viewModel.toggleMic()
        }
    }

    val postNotificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}

    DisposableEffect(Unit) {
        viewModel.setCameraPermission(
            context.checkSelfPermission(Manifest.permission.CAMERA) == PERMISSION_GRANTED
        )
        viewModel.setMicrophonePermission(
            context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PERMISSION_GRANTED
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            val granted = context.checkSelfPermission(permission) == PERMISSION_GRANTED
            if (!granted)
                postNotificationPermissionLauncher.launch(permission)
        }

        val serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                callService = (service as CallService.LocalBinder).getService()
                viewModel.setServiceInstance(callService)
            }

            override fun onServiceDisconnected(name: ComponentName) {
                callService = null
                viewModel.setServiceInstance(null)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }

        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        onDispose {
            context.unbindService(serviceConnection)
        }
    }

    if (callService == null) return
    Surface {
        VideoCallScreenContent(
            title = roomName,
            isMicOn = uiState.isMicOn,
            isVideoOn = uiState.isVideoOn,
            callPeers = uiState.callPeers,
            onMicToggle = {
                if (uiState.hasMicrophonePermission) viewModel.toggleMic()
                else microphonePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            },
            onVideoToggle = {
                if (uiState.hasCameraPermission) viewModel.toggleVideo()
                else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            },
            onCallLeave = {
                context.stopService(intent)
                goBack()
            },
            onCameraSwitch = {
                viewModel.switchCamera()
            },
            eglBaseContext = viewModel.eglBase.eglBaseContext
        )
    }
}

@Composable
private fun VideoCallScreenContent(
    title: String = "",
    isMicOn: Boolean = false,
    isVideoOn: Boolean = false,
    callPeers: List<CallPeer> = emptyList(),
    onMicToggle: () -> Unit = {},
    onVideoToggle: () -> Unit = {},
    onCallLeave: () -> Unit = {},
    onCameraSwitch: () -> Unit = {},
    eglBaseContext: EglBase.Context,
) {
    var barsVisible by remember { mutableStateOf(true) }
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(interactionSource = interactionSource, indication = null) {
                barsVisible = !barsVisible
            }
    ) {
        AnimatedVisibility(
            visible = barsVisible,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter),
        ) {
            CallScreenTopBar(title = title, onCameraSwitch = onCameraSwitch)
        }

        AnimatedVisibility(
            visible = barsVisible,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            CallScreenBottomBar(isMicOn, isVideoOn, onMicToggle, onVideoToggle, onCallLeave)
        }

        Column(
            modifier = Modifier
                .systemBarsPadding()
                .fillMaxSize()
                .zIndex(-1.0f),
            verticalArrangement = Arrangement.Center
        ) {
            SwipeGrid(
                items = callPeers,
                itemsPerPage = 6,
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp),
                itemContent = { item, modifier ->
                    CallPeerView(item, eglBaseContext, modifier)
                }
            )
        }
    }
}

@Composable
private fun CallScreenTopBar(
    title: String,
    onCameraSwitch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.4f),
                        Color.Transparent
                    )
                )
            )
            .statusBarsPadding()
            .padding(top = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
        ) {
            val shadowColor = if (isSystemInDarkTheme()) Color.Black else Color.Transparent
            Text(
                text = title,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.headlineMedium.copy(
                    shadow = Shadow(
                        color = shadowColor,
                        offset = Offset(2f, 2f),
                        blurRadius = 6f
                    )
                )
            )
            IconButton(onClick = onCameraSwitch, modifier = Modifier.padding(horizontal = 8.dp)) {
                Icon(
                    imageVector = Icons.Default.Cameraswitch,
                    contentDescription = "Switch Camera",
                    modifier = modifier.size(32.dp).shadow(
                        elevation = 8.dp,
                        shape = CircleShape,
                        ambientColor = shadowColor,
                        spotColor = shadowColor
                    )
                )
            }
        }
    }
}

@Composable
private fun CallScreenBottomBar(
    isMicOn: Boolean,
    isVideoOn: Boolean,
    onMicToggle: () -> Unit,
    onVideoToggle: () -> Unit,
    onCallLeave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.4f)
                    )
                )
            )
            .padding(bottom = 60.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Card(shape = CircleShape, elevation = CardDefaults.cardElevation(8.dp)) {
            IconButton(onClick = onMicToggle) {
                Icon(
                    imageVector = if (isMicOn) Icons.Default.Mic else Icons.Default.MicOff,
                    contentDescription = "Toggle Mic"
                )
            }
        }
        Card(
            shape = CircleShape,
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            IconButton(onClick = onVideoToggle) {
                Icon(
                    imageVector = if (isVideoOn)
                        Icons.Default.Videocam
                    else Icons.Default.VideocamOff,
                    contentDescription = "Toggle Video"
                )
            }
        }
        Card(
            shape = CircleShape,
            elevation = CardDefaults.cardElevation(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Red)
        ) {
            IconButton(onClick = onCallLeave) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    tint = Color.White,
                    contentDescription = "Leave"
                )
            }
        }
    }
}