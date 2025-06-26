package com.example.videocall.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.videocall.service.CallPeer
import org.webrtc.EglBase

@Composable
fun CallPeerView(
    callPeer: CallPeer,
    eglBaseContext: EglBase.Context,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier) {
        Box(modifier = Modifier.fillMaxSize()) {
            val isVideoPlaying = callPeer.isVideoOn && callPeer.videoTrack != null
            if (!isVideoPlaying) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Person Icon",
                    modifier = Modifier
                        .size(100.dp)
                        .align(Alignment.Center)
                )
            } else {
                WebRtcVideoView(
                    callPeer.videoTrack,
                    eglBaseContext,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .align(Alignment.BottomCenter)
            ) {
                Card(
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    if (callPeer.isMicOn)
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Mic On",
                            tint = Color.Black,
                            modifier = Modifier.size(28.dp)
                        )
                    else
                        Icon(
                            imageVector = Icons.Default.MicOff,
                            contentDescription = "Mic Off",
                            tint = Color.Black,
                            modifier = Modifier.size(28.dp)
                        )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = callPeer.name, style = TextStyle.Default.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isVideoPlaying) {
                            Color.White
                        } else {
                            MaterialTheme.colorScheme.onBackground
                        },
                        shadow = Shadow(
                            color = if (isVideoPlaying) Color.Black else Color.Transparent,
                            offset = Offset(2f, 2f),
                            blurRadius = 6f
                        )
                    )
                )
            }
        }
    }
}