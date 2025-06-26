package com.example.videocall.socket.dto

import org.json.JSONObject

data class InitResponse(
    val sendTransportParams: TransportParams,
    val recvTransportParams: TransportParams,
    val rtpCapabilities: String,
) {
    companion object {
        fun fromJson(json: JSONObject) = InitResponse(
            TransportParams.fromJson(json.getJSONObject("sendTransportParams")),
            TransportParams.fromJson(json.getJSONObject("recvTransportParams")),
            json.optString("rtpCapabilities")
        )
    }
}