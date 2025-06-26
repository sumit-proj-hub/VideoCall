package com.example.videocall.socket.dto

import org.json.JSONObject

data class TransportParams(
    val id: String,
    val iceParameters: String,
    val iceCandidates: String,
    val dtlsParameters: String,
) {
    companion object {
        fun fromJson(json: JSONObject) = TransportParams(
            id = json.optString("id"),
            iceParameters = json.optString("iceParameters"),
            iceCandidates = json.optString("iceCandidates"),
            dtlsParameters = json.optString("dtlsParameters")
        )
    }
}
