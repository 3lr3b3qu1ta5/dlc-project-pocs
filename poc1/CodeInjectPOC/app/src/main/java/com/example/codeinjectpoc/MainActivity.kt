package com.example.codeinjectpoc

import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.webkit.WebView
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val codeInput = findViewById<EditText>(R.id.codeInput)
        val runButton = findViewById<Button>(R.id.runButton)
        val outputText = findViewById<TextView>(R.id.outputText)

        val webView = WebView(this)
        webView.settings.javaScriptEnabled = true

        runButton.setOnClickListener {
            val code = codeInput.text.toString()
            val escapedCode = JSONObject.quote(code)

            val jsCode = """
                (function() {
                    try {
                        return JSON.stringify(eval($escapedCode));
                    } catch (e) {
                        return "Error: " + e.message;
                    }
                })();
            """.trimIndent()

            webView.evaluateJavascript(jsCode) { result ->
                outputText.text = result.toString()
            }
        }
    }
}
