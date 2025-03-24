package com.example.codeinjectpoc.websockets

import okhttp3.*
import kotlinx.coroutines.channels.Channel
import android.util.Log

// Basic class to manage the Websocket
class WebSocketController(private val client: OkHttpClient, private val serverUrl: String) {
    private var webSocket: WebSocket? = null
    private val msgChannel = Channel<String>(capacity = 1) // message buffer/manager

    fun start() {
        val request = Request.Builder().url(serverUrl).build()
        webSocket = client.newWebSocket(request, object: WebSocketListener() {
            // Basic anonymous object with the event managers
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSocket", "Connected to server !")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WebSocket", "Message received ! ==> $text")
                msgChannel.trySend(text)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocket", "Disconnected ! ==> $reason")
                webSocket.close(code, reason)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocket", "Error ! ==> ${t.message}")
            }
        })
    }

    suspend fun getMessage(): String { // Coroutine needed to keep it asynchronous
        return msgChannel.receive()
    }

    fun sendMessage(message: String) { // Non-suspended
        webSocket?.send(message)
    }

    fun end() {
        webSocket?.close(1000, "Sending closing signal to Websocket...")
    }
}