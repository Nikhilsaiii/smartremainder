package com.example.smartremainder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat

class NotificationHelper(private val context: Context) {

    companion object {
        const val EXTRA_MEDICATION_ID = "MEDICATION_ID"
        const val EXTRA_NOTIFICATION_ID = "NOTIFICATION_ID"
        private const val CHANNEL_ID = "medication_reminder_channel"
        private const val CHANNEL_NAME = "Medication Reminders"
    }

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for medication reminder alarms"
                setBypassDnd(true) // Ensure it can bypass Do Not Disturb

                // This is the critical part for the ALARM SOUND
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), audioAttributes)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(medication: Medication) {
        val notificationId = medication.id.toInt()

        // Intent for when the notification itself is tapped (opens the app)
        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val mainPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // "Taken" button Intent
        val takenIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = "TAKEN"
            putExtra(EXTRA_MEDICATION_ID, medication.id)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        val takenPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 1, // Use a unique request code
            takenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // "Skipped" button Intent
        val skippedIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = "SKIPPED"
            putExtra(EXTRA_MEDICATION_ID, medication.id)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        val skippedPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 2, // Use a unique request code
            skippedIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your own icon
            .setContentTitle("Time for your medication!")
            .setContentText("Take ${medication.name} (${medication.dosage})")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(mainPendingIntent)
            .setAutoCancel(true)
            .addAction(0, "Taken", takenPendingIntent)
            .addAction(0, "Skipped", skippedPendingIntent)
            // Explicitly set sound again for maximum compatibility
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))


        notificationManager.notify(notificationId, builder.build())
    }

    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }
}
