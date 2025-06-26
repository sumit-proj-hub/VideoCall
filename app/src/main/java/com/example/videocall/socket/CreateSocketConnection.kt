package com.example.videocall.socket

import com.example.videocall.Constants.BASE_URL
import com.example.videocall.service.CallService
import io.socket.client.IO
import java.net.URI

fun CallService.createSocketConnection(roomId: Int, token: String) {
    val options = IO.Options.builder()
        .setQuery("roomId=$roomId")
        .setExtraHeaders(mapOf("Authorization" to listOf("Bearer $token")))
        .build()
    io = IO.socket(URI.create(BASE_URL), options)
}