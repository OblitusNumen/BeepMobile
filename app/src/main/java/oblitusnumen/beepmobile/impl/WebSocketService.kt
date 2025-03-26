package oblitusnumen.beepmobile.impl

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.util.Log
import oblitusnumen.beepmobile.R
import oblitusnumen.beepmobile.ui.BeepState
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class WebSocketService : Service() {
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var webSocket: WebSocket
    var userName: String = ""
        get() {
            if (field.isEmpty())
                field = getUsername(this)
            return field
        }
        set(value) {
            saveUsername(this, value)
            val packet = JSONObject()
            packet.put("type", "nickname_change")
            packet.put("new_nickname", value)
            send(packet.toString())
            field = value
        }
    private val uuid: UUID = UUID.randomUUID()

    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): WebSocketService = this@WebSocketService
    }

    override fun onCreate() {
        Log.d("BeepService", "startBeepService")
        super.onCreate()
        startForeground(1, createNotification(this)) // Keeps the service alive
        mediaPlayer = MediaPlayer.create(this, R.raw.beep)
        initWebSocket()
    }

    fun send(text: String) {
        Log.d("send", text)
        webSocket.send(text)
    }

    private fun initWebSocket() {
        val client = OkHttpClient()
        val request = Request.Builder().url(SERVER_URL).build()
        webSocket = client.newWebSocket(request, object : okhttp3.WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("onOpen", "onOpen")
                BeepState.setConnected(true)
                BeepState.setUsers(mapOf())
                BeepState.setReceivedMessages(listOf())
                val packet = JSONObject()
                packet.put("type", "join")
                packet.put("nickname", userName)
                packet.put("user_id", uuid.toString())
                send(packet.toString())
                Log.d("WebSocket", "Connected to WebSocket Server")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                handleIncomingMessage(text)
                Log.d("WebSocket", "Received: $text")
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocket", "Closed: $code, $reason")
                BeepState.setConnected(false)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocket", "Error: ${t.message} :response: ${response?.message}")
            }
        })
    }

    private fun handleIncomingMessage(msg: String) {
        val jsonObject = JSONObject(msg)
        when (jsonObject.getString("type")) {
            "sound" -> {
                if (UUID.fromString(jsonObject.getString("user_id")) != uuid) {
                    sendNotification(this, BeepState.users.value[UUID.fromString(jsonObject.getString("user_id"))])
                    playSound(mediaPlayer)
                }
                BeepState.setReceivedMessages(
                    BeepState.receivedMessages.value + ("[${
                        LocalDateTime.ofInstant(
                            Instant.ofEpochSecond(
                                jsonObject.getString("timestamp").toLong() / 1000
                            ), ZoneId.systemDefault()
                        ).format(dateTimeFormatter)
                    }] from ${BeepState.users.value[UUID.fromString(jsonObject.getString("user_id"))]}: [SOUND_SIGNAL]")
                )
            }

            "chat" -> {
                BeepState.setReceivedMessages(
                    BeepState.receivedMessages.value + ("[${
                        LocalDateTime.ofInstant(
                            Instant.ofEpochSecond(
                                jsonObject.getString("timestamp").toLong() / 1000
                            ), ZoneId.systemDefault()
                        ).format(dateTimeFormatter)
                    }] from ${BeepState.users.value[UUID.fromString(jsonObject.getString("user_id"))]}: ${
                        jsonObject.getString(
                            "message"
                        )
                    }")
                )
            }

            "join" -> BeepState.setUsers(
                BeepState.users.value + (UUID.fromString(jsonObject.getString("user_id")) to jsonObject.getString("nickname"))
            )

            "nickname_change" -> BeepState.setUsers(
                BeepState.users.value + (UUID.fromString(jsonObject.getString("user_id")) to jsonObject.getString("new_nickname"))
            )

            "user_left" -> BeepState.setUsers(BeepState.users.value - UUID.fromString(jsonObject.getString("user_id")))
            "user_list" -> {
                val jsonArray = jsonObject.getJSONArray("users")
                for (i in 0 until jsonArray.length()) {
                    val jsonObject1 = jsonArray.getJSONObject(i)
                    BeepState.setUsers(
                        BeepState.users.value + (UUID.fromString(jsonObject1.getString("user_id")) to jsonObject1.getString(
                            "nickname"
                        ))
                    )
                }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        mediaPlayer.release()
        webSocket.close(1000, "user disconnected")
    }

    companion object {
        private const val SERVER_URL = "wss://beep.demogram.ru/ws"
        val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    }
}