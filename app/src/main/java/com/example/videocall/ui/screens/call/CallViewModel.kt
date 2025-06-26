package com.example.videocall.ui.screens.call

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.videocall.service.CallPeer
import com.example.videocall.service.CallService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.webrtc.EglBase
import javax.inject.Inject

@HiltViewModel
class CallViewModel @Inject constructor(val eglBase: EglBase) : ViewModel() {
    private var serviceInstance = MutableStateFlow<CallService?>(null)

    init {
        viewModelScope.launch {
            serviceInstance.filterNotNull().collectLatest { service ->
                service.dataFlow.collect {
                    val callPeers = mutableListOf(
                        CallPeer(
                            socketId = "",
                            name = service.userProfile.name,
                            email = service.userProfile.email,
                            isMicOn = it.isMicOn,
                            isVideoOn = it.isVideoOn,
                            videoTrack = service.videoTrack,
                            audioTrack = service.audioTrack
                        )
                    )
                    callPeers.addAll(it.callPeers.values)
                    uiState = uiState.copy(
                        isMicOn = it.isMicOn,
                        isVideoOn = it.isVideoOn,
                        callPeers = callPeers,
                        forceUpdate = System.currentTimeMillis()
                    )
                }
            }
        }
    }

    var uiState by mutableStateOf(CallUiState())
        private set

    fun toggleMic() {
        serviceInstance.value?.toggleAudio()
    }

    fun toggleVideo() {
        serviceInstance.value?.toggleVideo()
    }

    fun switchCamera() {
        serviceInstance.value?.switchCamera()
    }

    fun setCameraPermission(hasCameraPermission: Boolean) {
        uiState = uiState.copy(hasCameraPermission = hasCameraPermission)
    }

    fun setMicrophonePermission(hasMicrophonePermission: Boolean) {
        uiState = uiState.copy(hasMicrophonePermission = hasMicrophonePermission)
    }

    fun setServiceInstance(serviceInstance: CallService?) {
        this.serviceInstance.value = serviceInstance
    }
}