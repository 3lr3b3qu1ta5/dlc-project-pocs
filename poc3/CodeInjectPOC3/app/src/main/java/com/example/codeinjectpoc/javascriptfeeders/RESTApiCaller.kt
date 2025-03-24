package com.example.codeinjectpoc.javascriptfeeders

import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.example.codeinjectpoc.websockets.WebSocketController
import kotlinx.coroutines.*

// Includes Javascript interfaces to connect to REST APIs
class RESTApiCaller(private val webView: WebView, private val wsController: WebSocketController) {

    @JavascriptInterface
    fun callPublicRESTApi(url: String) {
        CoroutineScope(Dispatchers.Main).launch {
            wsController.sendMessage("GET$url") // Simplified messaging protocol
            val msgReceived: String = wsController.getMessage()
            Handler(Looper.getMainLooper()).post { // To modify webView context, access from main thread is mandatory
                webView.evaluateJavascript("window.valueCaptured = '${msgReceived.replace("\\", "\\\\")}';", null)
            }
        }
    }
}
