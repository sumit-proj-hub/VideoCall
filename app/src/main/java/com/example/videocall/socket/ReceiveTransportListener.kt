package com.example.videocall.socket

import android.util.Log
import com.example.videocall.service.CallService
import io.socket.client.Ack
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.mediasoup.droid.RecvTransport
import org.mediasoup.droid.Transport
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

fun CallService.createReceiveTransportListener() = object : RecvTransport.Listener {
    override fun onConnect(transport: Transport?, dtlsParameters: String?) {
        runBlocking {
            sendDtlsParameters(dtlsParameters!!)
        }
    }

    override fun onConnectionStateChange(transport: Transport?, connectionState: String?) {
        Log.d("ReceiveTransport", "Connection State: $connectionState")
    }

    private suspend fun sendDtlsParameters(dtlsParameters: String) =
        suspendCoroutine { continuation ->
            io.emit(
                "transportConnect",
                JSONObject(
                    mapOf(
                        "isSender" to false,
                        "dtlsParameters" to JSONObject(dtlsParameters)
                    )
                ),
                object : Ack {
                    override fun call(vararg args: Any?) {
                        continuation.resume(Unit)
                    }
                }
            )
        }
}