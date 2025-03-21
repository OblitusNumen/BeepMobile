package oblitusnumen.beepmobile.ui

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import oblitusnumen.beepmobile.MainActivity
import oblitusnumen.beepmobile.impl.MyWebSocketClient
import oblitusnumen.beepmobile.impl.playSound
import oblitusnumen.beepmobile.impl.sendNotification
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@Composable
fun WebSocketUI(
    context: Context,
    mediaPlayer: MediaPlayer,
    webSocketClient: MyWebSocketClient,
    connected: MutableState<Boolean>
) {
    var message by remember { mutableStateOf("") }
    var receivedMessages by remember { mutableStateOf(listOf<String>()) }
    val users = remember { mutableStateMapOf<UUID, String>() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            if (webSocketClient.messages.isNotEmpty()) {
                for (msg in webSocketClient.messages) {
                    val jsonObject = JSONObject(msg)
                    when (jsonObject.getString("type")) {
                        "sound" -> {
                            if (UUID.fromString(jsonObject.getString("user_id")) != webSocketClient.uuid) {
                                sendNotification(context, users[UUID.fromString(jsonObject.getString("user_id"))])
                                playSound(mediaPlayer)
                            }
                            receivedMessages =
                                receivedMessages + ("[${
                                    LocalDateTime.ofInstant(
                                        Instant.ofEpochSecond(
                                            jsonObject.getString("timestamp").toLong() / 1000
                                        ), ZoneId.systemDefault()
                                    ).format(MainActivity.dateTimeFormatter)
                                }] from ${users[UUID.fromString(jsonObject.getString("user_id"))]}: [SOUND_SIGNAL]")
                        }

                        "chat" -> {
                            receivedMessages =
                                receivedMessages + ("[${
                                    LocalDateTime.ofInstant(
                                        Instant.ofEpochSecond(
                                            jsonObject.getString("timestamp").toLong() / 1000
                                        ), ZoneId.systemDefault()
                                    ).format(MainActivity.dateTimeFormatter)
                                }] from ${users[UUID.fromString(jsonObject.getString("user_id"))]}: ${
                                    jsonObject.getString(
                                        "message"
                                    )
                                }")
                        }

                        "join" -> users[UUID.fromString(jsonObject.getString("user_id"))] =
                            jsonObject.getString("nickname")

                        "nickname_change" -> users[UUID.fromString(jsonObject.getString("user_id"))] =
                            jsonObject.getString("new_nickname")

                        "user_left" -> users.remove(UUID.fromString(jsonObject.getString("user_id")))
                        "user_list" -> {
                            val jsonArray = jsonObject.getJSONArray("users")
                            for (i in 0 until jsonArray.length()) {
                                val jsonObject1 = jsonArray.getJSONObject(i)
                                users[UUID.fromString(jsonObject1.getString("user_id"))] =
                                    jsonObject1.getString("nickname")
                            }
                        }
                    }
                }
                webSocketClient.messages = listOf()
            }

            Text(text = "WebSocket Chat", style = MaterialTheme.typography.headlineMedium)

            users.values.sorted().forEach { user ->
                Text(text = " - $user")
            }

            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("Message") }
            )

            Row {
                Button(
                    onClick = {
                        if (message.isNotEmpty()) {
                            val packet = JSONObject()
                            packet.put("type", "chat")
                            packet.put("timestamp", System.currentTimeMillis())
                            packet.put("message", message)
                            webSocketClient.send(packet.toString())
                            message = ""
                        }
                    },
                    modifier = Modifier.weight(.5f)
                ) {
                    Text("Send message")
                }
                Button(
                    onClick = {
                        val packet = JSONObject()
                        packet.put("type", "sound")
                        packet.put("timestamp", System.currentTimeMillis())
                        webSocketClient.send(packet.toString())
                    },
                    modifier = Modifier.weight(.5f)
                ) {
                    Text("Send signal")
                }
            }
            Row {
                var changeNicknameDialog by remember { mutableStateOf(false) }
                Button(
                    onClick = {
                        changeNicknameDialog = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Change nickname")
                }
                if (changeNicknameDialog) {
                    changeNickname(webSocketClient.userName, {
                        webSocketClient.userName = it
                        changeNicknameDialog = false
                    }) {
                        changeNicknameDialog = false
                    }
                }
            }

            Button(
                onClick = {
                    webSocketClient.close()
                    connected.value = false
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Disconnect")
            }

            Text("Received Messages:")
        }
        items(receivedMessages.size) { index ->
            val text = receivedMessages[receivedMessages.size - index - 1]
            SelectionContainer {
                Text(text = text)
            }
        }
    }
}

@Composable
private fun changeNickname(curNickname: String, onOk: (String) -> Unit, onClose: () -> Unit) {
    var nickname by remember { mutableStateOf(curNickname) }
    AlertDialog(onDismissRequest = onClose, dismissButton = {
        TextButton(onClick = onClose) {
            Text("Cancel")
        }
    }, confirmButton = {
        TextButton(onClick = {
            onOk(nickname)
        }) {
            Text("OK")
        }
    }, text = {
        OutlinedTextField(
            value = nickname,
            onValueChange = { nickname = it },
            label = { Text("New nickname") },
        )
    })
}