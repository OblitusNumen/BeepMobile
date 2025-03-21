package oblitusnumen.beepmobile.impl

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONObject
import java.net.URI
import java.util.*

class MyWebSocketClient(serverUri: URI, userName: String) : WebSocketClient(serverUri) {
    var userName = userName
        set(value) {
            val packet = JSONObject()
            packet.put("type", "nickname_change")
            packet.put("new_nickname", value)
            send(packet.toString())
            field = value
        }
    var messages: List<String> by mutableStateOf(listOf())
    val uuid = UUID.randomUUID()

    override fun onOpen(handshakedata: ServerHandshake?) {
        init()
        Log.d("WebSocket", "Connected to WebSocket Server")
    }

    override fun send(text: String?) {
        Log.d("send", text ?: "")
        super.send(text)
    }

    override fun onMessage(message: String?) {
        if (message == null) return
        messages = messages + message
        Log.d("WebSocket", "Received: $message")
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        Log.d("WebSocket", "Closed: $code, $reason, $remote")
    }

    override fun onError(ex: Exception?) {
        Log.e("WebSocket", "Error: ${ex?.message}")
    }

    fun init() {
        val packet = JSONObject()
        packet.put("type", "join")
        packet.put("nickname", userName)
        packet.put("user_id", uuid.toString())
        send(packet.toString())
    }
}