package com.example.videocall.socket

import com.example.videocall.service.CallPeer
import com.example.videocall.service.CallService
import com.example.videocall.socket.dto.MediaToggleResponse
import com.example.videocall.socket.dto.PeerSummary
import kotlinx.serialization.json.Json
import org.json.JSONObject

fun CallService.setupEventListeners() {
    io.on("userMicToggle") {
        val json = it[0] as JSONObject
        val response = Json.decodeFromString<MediaToggleResponse>(json.toString())
        if (!dataFlow.value.callPeers.containsKey(response.socketId))
            return@on
        dataFlow.value.callPeers[response.socketId]!!.isMicOn = response.isMicOn!!
        dataFlow.value = dataFlow.value.copy(forceEmit = System.currentTimeMillis())
    }

    io.on("userVideoToggle") {
        val json = it[0] as JSONObject
        val response = Json.decodeFromString<MediaToggleResponse>(json.toString())
        if (!dataFlow.value.callPeers.containsKey(response.socketId))
            return@on
        dataFlow.value.callPeers[response.socketId]!!.isVideoOn = response.isVideoOn!!
        dataFlow.value = dataFlow.value.copy(forceEmit = System.currentTimeMillis())
    }

    io.on("userJoined") {
        val json = it[0] as JSONObject
        val peerSummary = Json.decodeFromString<PeerSummary>(json.toString())
        dataFlow.value.callPeers[peerSummary.socketId] = CallPeer(
            peerSummary.socketId,
            peerSummary.name,
            peerSummary.email,
            peerSummary.isMicOn,
            peerSummary.isVideoOn
        )
        dataFlow.value = dataFlow.value.copy(forceEmit = System.currentTimeMillis())
    }

    io.on("userLeft") {
        val json = it[0] as JSONObject
        val socketId = json.getString("socketId")
        if (!dataFlow.value.callPeers.containsKey(socketId))
            return@on
        val peer = dataFlow.value.callPeers[socketId]!!
        peer.audioTrack?.setEnabled(false)
        peer.videoTrack?.setEnabled(false)
        peer.audioConsumer?.close()
        peer.videoConsumer?.close()
        dataFlow.value.callPeers.remove(socketId)
        dataFlow.value = dataFlow.value.copy(forceEmit = System.currentTimeMillis())
    }

    io.on("producerCreated") {
        val json = it[0] as JSONObject
        val socketId = json.getString("socketId")
        val kind = json.getString("kind")
        if (!dataFlow.value.callPeers.containsKey(socketId))
            return@on
        when (kind) {
            "audio" -> consumeAudioProducer(socketId)
            "video" -> consumeVideoProducer(socketId)
        }
    }
}