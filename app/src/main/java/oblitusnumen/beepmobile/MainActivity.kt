package oblitusnumen.beepmobile

import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import oblitusnumen.beepmobile.impl.MyWebSocketClient
import oblitusnumen.beepmobile.impl.createNotificationChannel
import oblitusnumen.beepmobile.ui.WebSocketUI
import oblitusnumen.beepmobile.ui.theme.BeepMobileTheme
import java.net.URI
import java.time.format.DateTimeFormatter
import java.util.*
import android.Manifest
import org.json.JSONObject


class MainActivity : ComponentActivity() {
    private var webSocketClient: MyWebSocketClient? = null
    private var mediaPlayer: MediaPlayer? = null

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (mediaPlayer == null)
            mediaPlayer = MediaPlayer.create(this, R.raw.beep)

        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                    Log.d("Notifications", "isGranted")
                } else {
                    Log.d("Notifications", "not granted")
                    // Explain to the user that the feature is unavailable because the
                    // feature requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                }
            }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // You can use the API that requires the permission.
                    Log.d("Notifications", "already granted")
                }
                // TODO:
                /*ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS) -> {
                    // In an educational UI, explain to the user why your app requires this
                    // permission for a specific feature to behave as expected, and what
                    // features are disabled if it's declined. In this UI, include a
                    // "cancel" or "no, thanks" button that lets the user continue
                    // using your app without granting the permission.
                    //showInContextUI(...)
                    log("shouldShowRequestPermissionRationale")
                }*/
                else -> {
                    // You can directly ask for the permission.
                    // The registered ActivityResultCallback gets the result of this request.
                    Log.d("Notifications", "requestPermissionLauncher.launch")
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
        createNotificationChannel(this)

        enableEdgeToEdge()
        setContent {
            BeepMobileTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
                        CenterAlignedTopAppBar(
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = .9f),
                                titleContentColor = MaterialTheme.colorScheme.primary,
                            ),
                            title = { Text("Beep Mobile", maxLines = 1) },
                            scrollBehavior = scrollBehavior,
                        )
                    },
                    floatingActionButton = {
                    },
                    bottomBar = {
                    }
                ) { innerPadding ->
                    val connected = remember { mutableStateOf(false) }
                    Box(Modifier.padding(innerPadding)) {
                        if (connected.value) {
                            remember {
//                                webSocketClient!!.init()
                            }
                            WebSocketUI(this@MainActivity, mediaPlayer!!, webSocketClient!!, connected)
                        } else {
                            Column(Modifier.fillMaxSize().padding(16.dp).align(Alignment.Center)) {
                                var message by remember { mutableStateOf("User@${UUID.randomUUID()}") }
                                OutlinedTextField(
                                    value = message,
                                    onValueChange = { message = it },
                                    label = { Text("Nickname") },
                                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp)
                                )
                                Button(
                                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp),
                                    onClick = {
                                        val serverUrl = "wss://beep.demogram.ru/ws"  // Change to your actual server
                                        webSocketClient = MyWebSocketClient(URI(serverUrl), message)
                                        webSocketClient!!.connect()  // onnect WebSocket when the app starts
                                        connected.value = true
                                    }) {
                                    Text(text = "Connect")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        webSocketClient?.close()  // Close WebSocket when app is destroyed
        mediaPlayer?.release()
    }

    companion object {
        val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    }
}