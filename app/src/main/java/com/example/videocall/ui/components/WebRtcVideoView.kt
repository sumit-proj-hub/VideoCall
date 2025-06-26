package com.example.videocall.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.webrtc.EglBase
import org.webrtc.RendererCommon
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoTrack

@Composable
fun WebRtcVideoView(
    videoTrack: VideoTrack?,
    eglBaseContext: EglBase.Context,
    modifier: Modifier = Modifier,
    scalingType: RendererCommon.ScalingType = RendererCommon.ScalingType.SCALE_ASPECT_FIT
) {
    val context = LocalContext.current
    val videoRenderer = remember { SurfaceViewRenderer(context) }

    DisposableEffect(videoTrack) {
        videoRenderer.init(eglBaseContext, null)
        videoRenderer.setScalingType(scalingType)
        videoRenderer.setZOrderMediaOverlay(true)
        videoTrack?.addSink(videoRenderer)
        onDispose {
            videoTrack?.removeSink(videoRenderer)
            videoRenderer.release()
        }
    }

    AndroidView(
        factory = { videoRenderer },
        modifier = modifier
    )
}