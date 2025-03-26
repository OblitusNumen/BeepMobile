package oblitusnumen.beepmobile

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import oblitusnumen.beepmobile.impl.WebSocketService
import oblitusnumen.beepmobile.impl.createNotificationChannel
import oblitusnumen.beepmobile.impl.getUsername
import oblitusnumen.beepmobile.impl.saveUsername
import oblitusnumen.beepmobile.ui.BeepViewModel
import oblitusnumen.beepmobile.ui.WebSocketUI
import oblitusnumen.beepmobile.ui.theme.BeepMobileTheme


class MainActivity : ComponentActivity() {
    var webSocketService: WebSocketService? = null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as? WebSocketService.LocalBinder
            webSocketService = binder?.getService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            webSocketService = null
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                    val viewModel: BeepViewModel = viewModel()
                    val connected by viewModel.connected.collectAsState()
                    Box(Modifier.padding(innerPadding)) {
                        if (connected) {
                            WebSocketUI(this@MainActivity, webSocketService!!, this@MainActivity::stopBeepService)
                        } else {
                            Column(Modifier.fillMaxSize().padding(16.dp).align(Alignment.Center)) {
                                var message by remember { mutableStateOf(getUsername(this@MainActivity)) }
                                OutlinedTextField(
                                    value = message,
                                    onValueChange = { message = it },
                                    label = { Text("Nickname") },
                                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp)
                                )
                                Button(
                                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp),
                                    onClick = {
                                        saveUsername(this@MainActivity, message)
                                        startBeepService()
                                    }
                                ) {
                                    Text(text = "Connect")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun startBeepService() {
        val intent = Intent(this@MainActivity, WebSocketService::class.java)
        ContextCompat.startForegroundService(this@MainActivity, intent)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    fun stopBeepService() {
        // 1 Unbind if you previously did bindService()
        try {
            unbindService(connection)
        } catch (e: IllegalArgumentException) {
            // this means the service wasn't bound - optional
            e.printStackTrace()
        }
        // 2 Stop the foreground service
        val intent = Intent(this@MainActivity, WebSocketService::class.java)
        stopService(intent)
    }
}