package oblitusnumen.beepmobile.impl

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import oblitusnumen.beepmobile.MainActivity

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
    val notification = NotificationCompat.Builder(context, "beep_notification")
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


// Create Notification Channel (required for Android 8.0 and higher)
fun createNotificationChannel(c: Context) {
    val notificationManager: NotificationManager =
        c.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(
        NotificationChannel(
            "beep_notification",
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
