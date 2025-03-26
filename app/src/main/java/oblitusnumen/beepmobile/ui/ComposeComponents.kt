package oblitusnumen.beepmobile.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import oblitusnumen.beepmobile.impl.WebSocketService
import oblitusnumen.beepmobile.impl.getUsername
import org.json.JSONObject

@Composable
fun WebSocketUI(
    context: Context,
    service: WebSocketService,
    stopService: () -> Unit,
    viewModel: BeepViewModel = viewModel()
) {
    val messages by viewModel.receivedMessages.collectAsState()
    val users by viewModel.users.collectAsState()
    var message by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
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
                            service.send(packet.toString())
                            message = ""
                        }
                    },
                    modifier = Modifier.padding(4.dp).weight(.5f)
                ) {
                    Text("Send message")
                }
                Button(
                    onClick = {
                        val packet = JSONObject()
                        packet.put("type", "sound")
                        packet.put("timestamp", System.currentTimeMillis())
                        service.send(packet.toString())
                    },
                    modifier = Modifier.padding(4.dp).weight(.5f)
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
                    changeNickname(getUsername(context), {
                        service.userName = it
                        changeNicknameDialog = false
                    }) {
                        changeNicknameDialog = false
                    }
                }
            }

            Button(
                onClick = stopService,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Disconnect")
            }

            Text("Received Messages:")
        }
        items(messages.size) { index ->
            val text = messages[messages.size - index - 1]
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