package com.example.videocall.socket.dto

import org.json.JSONObject

data class ConsumeProducerResponse(
    val id: String,
    val producerId: String,
    val kind: String,
    val rtpParameters: String,
) {
    companion object {
        fun fromJson(json: JSONObject) = ConsumeProducerResponse(
            json.getString("id"),
            json.getString("producerId"),
            json.getString("kind"),
            json.optString("rtpParameters")
        )
    }
}