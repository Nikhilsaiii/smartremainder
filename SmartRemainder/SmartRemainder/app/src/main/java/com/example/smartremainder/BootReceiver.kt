package com.example.smartremainder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            // This is where you would reschedule your alarms
            val dbHelper = DatabaseHelper(context)
            val medications = dbHelper.getAllMedications()
            val alarmScheduler = AlarmScheduler(context)
            for (medication in medications) {
                // Assuming you have a way to get the alarm time from the medication object
                // and an id for the pending intent.
                // alarmScheduler.schedule(medication.time, medication.id)
            }
        }
    }
}
