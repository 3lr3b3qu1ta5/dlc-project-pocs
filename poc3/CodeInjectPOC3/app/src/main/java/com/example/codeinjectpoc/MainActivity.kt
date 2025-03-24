package com.example.codeinjectpoc

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import android.widget.EditText
import android.widget.TextView
import android.webkit.WebView
import org.json.JSONObject

import com.example.codeinjectpoc.javascriptfeeders.RESTApiCaller
import com.example.codeinjectpoc.websockets.WebSocketController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import okhttp3.*

class MainActivity : AppCompatActivity() {
    companion object { // Compile-time constants
        const val WEBVIEW_POLLING_INTERVAL_MS = 2000L
        const val WEBVIEW_POLLING_MAX_ATTEMPTS = 10
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val outputText = findViewById<TextView>(R.id.outputText) // Text label to reflect the result

        CoroutineScope(Dispatchers.Main).launch {
            val serverUrl = askForServerURL(this@MainActivity)
            if (serverUrl.isNotEmpty()) {
                val client = OkHttpClient()
                val wsController = WebSocketController(client, serverUrl)
                wsController.start()
                val msgReceived: String = wsController.getMessage() // Awaits for message
                if (msgReceived.isNotEmpty()) {
                    val escapedCode = JSONObject.quote(msgReceived)
                    val jsCode = """
                      (function() {
                        try {
                              return JSON.stringify(eval($escapedCode));
                          } catch (e) {
                              return "Error: " + e.message;
                          }
                      })();
                    """.trimIndent()
                    val webView = WebView(this@MainActivity)
                    webView.addJavascriptInterface(RESTApiCaller(webView, wsController), "device")
                    webView.settings.javaScriptEnabled = true
                    val jsResult = executeJavascript(webView, jsCode) ?: "No value computed"
                    outputText.text = jsResult
                    wsController.sendMessage(jsResult)
                }
            } else {
                outputText.text = "No code running!"
            }
        }

    }

    private suspend fun askForServerURL(context: Context): String { // Coroutine to ask for websocket address
        return suspendCancellableCoroutine { cont ->
            val editText = EditText(context)
            editText.hint = "Enter server URL"
            editText.setText("ws://")
            editText.setSelection(editText.text.length)
            val dialog = AlertDialog.Builder(context)
                .setTitle("Enter server URL")
                .setView(editText)
                .setCancelable(false)
                .setPositiveButton("OK") {_, _ ->
                    val serverUrl = editText.text.toString()
                    cont.resume(serverUrl) {}
                }
                .setNegativeButton("Cancel") {_,_ ->
                    cont.resume("") {}
                }
                .create()

            dialog.show()
        }
    }

    // Double coroutine ! First one is needed to suspend the execution from caller.
    // Second one is part of the evaluateJavascript behaviour
    private suspend fun executeJavascript(webView: WebView, jsCode: String): String? {
        return suspendCancellableCoroutine { cont ->
            webView.evaluateJavascript(jsCode) { result ->
                CoroutineScope(Dispatchers.Main).launch {
                    var i = 0
                    var keep = result.toString().trim().removeSurrounding("\"") == "{}" // coming from "eval" command
                    var output = result
                    while (keep && i < WEBVIEW_POLLING_MAX_ATTEMPTS) {
                        delay(WEBVIEW_POLLING_INTERVAL_MS)
                        webView.evaluateJavascript("window.jsResult;") { innerResult ->
                            output = innerResult
                            i++
                            keep = output.toString().trim().removeSurrounding("\"") == "null"
                        }
                    }
                    cont.resume(if (i >= WEBVIEW_POLLING_MAX_ATTEMPTS) "timeout in main !!" else output.toString()) {}
                }
            }
        }
    }
}
