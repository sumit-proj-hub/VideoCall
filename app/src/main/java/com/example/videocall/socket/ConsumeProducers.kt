package com.example.videocall.socket

import com.example.videocall.service.CallService
import com.example.videocall.socket.dto.ConsumeProducerResponse
import io.socket.client.Ack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.webrtc.AudioTrack
import org.webrtc.VideoTrack

fun CallService.consumeAudioProducer(
    producerSocketId: String,
) {
    io.emit(
        "consumeProducer",
        JSONObject(
            mapOf(
                "producerSocketId" to producerSocketId,
                "kind" to "audio",
                "rtpCapabilities" to JSONObject(mediasoupDevice.rtpCapabilities)
            )
        ), object : Ack {
            override fun call(vararg args: Any?) {
                val json = args[0] as JSONObject
                val response = ConsumeProducerResponse.fromJson(json)
                if (!dataFlow.value.callPeers.containsKey(producerSocketId))
                    return
                val peer = dataFlow.value.callPeers[producerSocketId]!!
                serviceScope.launch {
                    withContext(Dispatchers.IO) {
                        peer.audioConsumer = receiveTransport.consume({
                            peer.audioConsumer = null
                        }, response.id, response.producerId, response.kind, response.rtpParameters)
                        changeConsumerState(response.id, true)
                        peer.audioTrack = peer.audioConsumer?.track as AudioTrack
                        peer.audioTrack?.setEnabled(true)
                        dataFlow.value =
                            dataFlow.value.copy(forceEmit = System.currentTimeMillis())
                    }
                }
            }
        }
    )
}

fun CallService.consumeVideoProducer(
    producerSocketId: String,
) {
    io.emit(
        "consumeProducer",
        JSONObject(
            mapOf(
                "producerSocketId" to producerSocketId,
                "kind" to "video",
                "rtpCapabilities" to JSONObject(mediasoupDevice.rtpCapabilities)
            )
        ), object : Ack {
            override fun call(vararg args: Any?) {
                val json = args[0] as JSONObject
                val response = ConsumeProducerResponse.fromJson(json)
                if (!dataFlow.value.callPeers.containsKey(producerSocketId))
                    return
                val peer = dataFlow.value.callPeers[producerSocketId]!!
                serviceScope.launch {
                    withContext(Dispatchers.IO) {
                        peer.videoConsumer = receiveTransport.consume({
                            peer.videoConsumer = null
                        }, response.id, response.producerId, response.kind, response.rtpParameters)
                        changeConsumerState(response.id, true)
                        peer.videoTrack = peer.videoConsumer?.track as VideoTrack
                        peer.videoTrack?.setEnabled(true)
                        dataFlow.value =
                            dataFlow.value.copy(forceEmit = System.currentTimeMillis())
                    }
                }
            }
        }
    )
}

fun CallService.changeConsumerState(consumerId: String, state: Boolean) {
    io.emit(
        "changeConsumerState", JSONObject(
            mapOf(
                "consumerId" to consumerId,
                "state" to state
            )
        )
    )
}