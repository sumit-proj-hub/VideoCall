package com.example.videocall.socket

import android.util.Log
import com.example.videocall.service.CallService
import io.socket.client.Ack
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.mediasoup.droid.SendTransport
import org.mediasoup.droid.Transport
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

fun CallService.createSendTransportListener() = object : SendTransport.Listener {
    override fun onConnect(transport: Transport?, dtlsParameters: String?) {
        runBlocking {
            sendDtlsParameters(dtlsParameters!!)
        }
    }

    override fun onConnectionStateChange(transport: Transport?, connectionState: String?) {
        Log.d("SendTransport", "Connection State: $connectionState")
    }

    override fun onProduce(
        transport: Transport?,
        kind: String?,
        rtpParameters: String?,
        appData: String?,
    ): String {
        var producerId: String
        runBlocking { producerId = getProducerId(kind!!, rtpParameters!!) }
        return producerId
    }

    override fun onProduceData(
        transport: Transport?,
        sctpStreamParameters: String?,
        label: String?,
        protocol: String?,
        appData: String?,
    ): String = ""

    private suspend fun getProducerId(kind: String, rtpParameters: String) =
        suspendCoroutine { continuation ->
            io.emit(
                "transportProduce",
                JSONObject(mapOf("kind" to kind, "rtpParameters" to JSONObject(rtpParameters))),
                object : Ack {
                    override fun call(vararg args: Any?) {
                        val response = args[0] as JSONObject
                        continuation.resume(response.getString("id"))
                    }
                }
            )
        }

    private suspend fun sendDtlsParameters(dtlsParameters: String) =
        suspendCoroutine { continuation ->
            io.emit(
                "transportConnect",
                JSONObject(
                    mapOf(
                        "isSender" to true,
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