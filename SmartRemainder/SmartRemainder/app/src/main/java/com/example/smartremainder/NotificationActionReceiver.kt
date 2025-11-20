package com.example.smartremainder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return

        val medicationId = intent.getLongExtra("MEDICATION_ID", -1L)
        if (medicationId == -1L) {
            return // Invalid medication ID
        }

        val notificationId = intent.getIntExtra("NOTIFICATION_ID", 0)

        val dbHelper = DatabaseHelper(context)
        val notificationHelper = NotificationHelper(context)

        when (action) {
            "TAKEN" -> {
                dbHelper.addHistoryEvent(medicationId, "Taken")
                Toast.makeText(context, "Taken", Toast.LENGTH_SHORT).show()
                notificationHelper.cancelNotification(notificationId)
            }
            "SKIPPED" -> {
                dbHelper.addHistoryEvent(medicationId, "Skipped")
                Toast.makeText(context, "You skipped the medicine", Toast.LENGTH_SHORT).show()
                notificationHelper.cancelNotification(notificationId)
            }
        }
    }
}
