package com.example.videocall.socket

import com.example.videocall.Constants
import com.example.videocall.service.CallPeer
import com.example.videocall.service.CallService
import com.example.videocall.socket.dto.InitResponse
import com.example.videocall.socket.dto.PeerSummary
import io.socket.client.Ack
import io.socket.client.Socket
import kotlinx.serialization.json.Json
import org.json.JSONArray
import org.json.JSONObject
import org.webrtc.PeerConnection

fun CallService.setupOnSocketConnect() {
    io.on(Socket.EVENT_CONNECT) {
        io.emit("initSession", object : Ack {
            override fun call(vararg args: Any?) {
                val response = args[0] as JSONObject
                if (response.has("error")) {
                    throw Exception(response.getString("error"))
                }
                val initResponse = InitResponse.fromJson(response)

                val stunServer =
                    PeerConnection.IceServer.builder(Constants.STUN_SERVER).createIceServer()
                val turnServer = PeerConnection.IceServer.builder(Constants.TURN_SERVER)
                    .setUsername(Constants.TURN_USERNAME)
                    .setPassword(Constants.TURN_PASSWORD)
                    .createIceServer()
                val iceServers = arrayListOf(stunServer, turnServer)
                val rtcConfig = PeerConnection.RTCConfiguration(iceServers)
                val options = org.mediasoup.droid.PeerConnection.Options()
                options.setRTCConfig(rtcConfig)
                options.setFactory(peerConnectionUtils.factory)

                mediasoupDevice.load(initResponse.rtpCapabilities, options)

                sendTransport = mediasoupDevice.createSendTransport(
                    createSendTransportListener(),
                    initResponse.sendTransportParams.id,
                    initResponse.sendTransportParams.iceParameters,
                    initResponse.sendTransportParams.iceCandidates,
                    initResponse.sendTransportParams.dtlsParameters
                )
                receiveTransport = mediasoupDevice.createRecvTransport(
                    createReceiveTransportListener(),
                    initResponse.recvTransportParams.id,
                    initResponse.recvTransportParams.iceParameters,
                    initResponse.recvTransportParams.iceCandidates,
                    initResponse.recvTransportParams.dtlsParameters
                )
            }
        })

        io.emit("getAllPeers", object : Ack {
            override fun call(vararg args: Any?) {
                val response = args[0] as JSONArray
                val peerList = Json.decodeFromString<List<PeerSummary>>(response.toString())
                dataFlow.value = dataFlow.value.copy(
                    callPeers = peerList
                        .filter { it.socketId != io.id() }
                        .map {
                            CallPeer(
                                it.socketId,
                                it.name,
                                it.email,
                                it.isMicOn,
                                it.isVideoOn
                            )
                        }
                        .associateBy { it.socketId }
                        .toMutableMap()
                )

                dataFlow.value.callPeers.forEach {
                    if (it.value.isMicOn) consumeAudioProducer(it.key)
                    if (it.value.isVideoOn) consumeVideoProducer(it.key)
                }
            }
        })
    }
}