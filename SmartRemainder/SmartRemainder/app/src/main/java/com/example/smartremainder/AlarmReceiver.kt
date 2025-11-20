package com.example.smartremainder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val medicationId = intent.getLongExtra("MEDICATION_ID", -1L)
        if (medicationId == -1L) return

        val dbHelper = DatabaseHelper(context)
        val notificationHelper = NotificationHelper(context)
        val alarmScheduler = AlarmScheduler(context)

        // Use a coroutine to fetch data off the main thread
        CoroutineScope(Dispatchers.IO).launch {
            val medications = dbHelper.getAllMedications()
            val medication = medications.find { it.id == medicationId }

            medication?.let {
                // Show the notification with sound and buttons
                notificationHelper.showNotification(it)

                // Reschedule the alarm for the next day
                alarmScheduler.schedule(it)
            }
        }
    }
}
