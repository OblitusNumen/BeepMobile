package oblitusnumen.beepmobile.impl

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import oblitusnumen.beepmobile.MainActivity
import oblitusnumen.beepmobile.R
import kotlin.random.Random

const val NOTIFICATION_CHANNEL_ID = "beep_notification"

fun sendNotification(context: Context, msg: String?) {
    // Create an intent that opens MainActivity when the notification is clicked
    val intent = Intent(context, MainActivity::class.java)
    val pendingIntent: PendingIntent = PendingIntent.getActivity(
        context,
        0,
        intent,
        PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    // Build the notification
    val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_dialog_info) // Icon for the notification
        .setContentTitle("New Notification") // Title
        .setContentText("Notification from $msg") // Message body
        .setPriority(NotificationCompat.PRIORITY_DEFAULT) // Notification priority
        .setContentIntent(pendingIntent) // When clicked, the app will open
        .setAutoCancel(true) // Remove notification when clicked
        .build()

    // Get NotificationManager system service
    val notificationManager = NotificationManagerCompat.from(context)

    // Show the notification with an ID (can be used for updating/removing)
    notificationManager.notify(1, notification)
}

fun createNotification(c: Context): Notification {
    return NotificationCompat.Builder(c, NOTIFICATION_CHANNEL_ID)
        .setContentTitle("Beep Mobile")
        .setContentText("Beep Service is running")
        .setSmallIcon(R.drawable.ic_beep)
        .build()
}

// Create Notification Channel (required for Android 8.0 and higher)
fun createNotificationChannel(c: Context) {
    val notificationManager: NotificationManager =
        c.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(
        NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "Normal",
            NotificationManager.IMPORTANCE_DEFAULT
        )
    )
}

fun playSound(mediaPlayer: MediaPlayer) {
    if (!mediaPlayer.isPlaying) {
        mediaPlayer.start()  // Start playback
    }
}

const val SHARED_PREFERENCES_NAME = "preferences"
const val USERNAME_PREFERENCE_NAME = "username"

fun saveUsername(context: Context, username: String) {
    getSharedPrefs(context).edit().putString(USERNAME_PREFERENCE_NAME, username).apply()
}

fun getUsername(context: Context): String {
    return getSharedPrefs(context).getString(USERNAME_PREFERENCE_NAME, null) ?: "AndroidUser@${Random.nextInt(1000)}"
}

fun getSharedPrefs(context: Context): SharedPreferences {
    return context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
}
