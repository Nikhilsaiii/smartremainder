package com.example.smartremainder

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.Calendar

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 5 // Incremented version for schema change
        private const val DATABASE_NAME = "MedicationDatabase.db"
        private const val TABLE_MEDICATIONS = "medications"
        private const val TABLE_HISTORY = "medication_history" // New history table

        // Medication Table Columns
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_DOSAGE = "dosage"
        private const val COLUMN_FREQUENCY = "frequency"
        private const val COLUMN_DURATION = "duration"
        private const val COLUMN_START_DATE = "start_date"
        private const val COLUMN_TIME = "time"
        private const val COLUMN_REMINDER_ENABLED = "reminder_enabled"
        private const val COLUMN_REFILL_ENABLED = "refill_enabled"
        private const val COLUMN_NOTES = "notes"
        private const val COLUMN_CURRENT_SUPPLY = "current_supply"
        private const val COLUMN_MAX_SUPPLY = "max_supply"

        // History Table Columns
        private const val COLUMN_HISTORY_ID = "history_id"
        private const val COLUMN_HISTORY_MED_ID = "med_id" // Foreign key to medications table
        private const val COLUMN_HISTORY_STATUS = "status" // e.g., "Taken", "Skipped"
        private const val COLUMN_HISTORY_TIMESTAMP = "timestamp"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createMedicationsTable = ("CREATE TABLE " + TABLE_MEDICATIONS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_DOSAGE + " TEXT,"
                + COLUMN_FREQUENCY + " TEXT,"
                + COLUMN_DURATION + " TEXT,"
                + COLUMN_START_DATE + " TEXT,"
                + COLUMN_TIME + " TEXT,"
                + COLUMN_REMINDER_ENABLED + " INTEGER,"
                + COLUMN_REFILL_ENABLED + " INTEGER,"
                + COLUMN_NOTES + " TEXT,"
                + COLUMN_CURRENT_SUPPLY + " INTEGER,"
                + COLUMN_MAX_SUPPLY + " INTEGER" + ")")

        val createHistoryTable = ("CREATE TABLE " + TABLE_HISTORY + "("
                + COLUMN_HISTORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_HISTORY_MED_ID + " INTEGER,"
                + COLUMN_HISTORY_STATUS + " TEXT,"
                + COLUMN_HISTORY_TIMESTAMP + " INTEGER," // Store as Long (Unix timestamp)
                + "FOREIGN KEY(" + COLUMN_HISTORY_MED_ID + ") REFERENCES " + TABLE_MEDICATIONS + "(" + COLUMN_ID + ") ON DELETE CASCADE" + ")") // Added ON DELETE CASCADE

        db?.execSQL(createMedicationsTable)
        db?.execSQL(createHistoryTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 5) {
            db?.execSQL("ALTER TABLE $TABLE_MEDICATIONS ADD COLUMN $COLUMN_CURRENT_SUPPLY INTEGER NOT NULL DEFAULT 0")
            db?.execSQL("ALTER TABLE $TABLE_MEDICATIONS ADD COLUMN $COLUMN_MAX_SUPPLY INTEGER NOT NULL DEFAULT 0")
        }
    }

    fun addMedication(med: Medication): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, med.name)
            put(COLUMN_DOSAGE, med.dosage)
            put(COLUMN_FREQUENCY, med.frequency)
            put(COLUMN_DURATION, med.duration)
            put(COLUMN_START_DATE, med.startDate)
            put(COLUMN_TIME, med.time)
            put(COLUMN_REMINDER_ENABLED, if (med.reminderEnabled) 1 else 0)
            put(COLUMN_REFILL_ENABLED, if (med.refillEnabled) 1 else 0)
            put(COLUMN_NOTES, med.notes)
            put(COLUMN_CURRENT_SUPPLY, med.currentSupply)
            put(COLUMN_MAX_SUPPLY, med.maxSupply)
        }
        val id = db.insert(TABLE_MEDICATIONS, null, values)
        return id
    }

    fun getAllMedications(): List<Medication> {
        val medList = mutableListOf<Medication>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_MEDICATIONS", null)

        if (cursor.moveToFirst()) {
            do {
                val med = Medication(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                    dosage = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DOSAGE)),
                    frequency = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FREQUENCY)),
                    duration = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DURATION)),
                    startDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_START_DATE)),
                    time = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME)),
                    reminderEnabled = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REMINDER_ENABLED)) == 1,
                    refillEnabled = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REFILL_ENABLED)) == 1,
                    notes = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTES)),
                    currentSupply = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CURRENT_SUPPLY)),
                    maxSupply = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MAX_SUPPLY))
                )
                medList.add(med)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return medList
    }

    fun deleteMedication(medId: Long) {
        val db = this.writableDatabase
        db.delete(TABLE_MEDICATIONS, "$COLUMN_ID = ?", arrayOf(medId.toString()))
    }
    
    fun addHistoryEvent(medicationId: Long, status: String) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_HISTORY_MED_ID, medicationId)
            put(COLUMN_HISTORY_STATUS, status)
            put(COLUMN_HISTORY_TIMESTAMP, System.currentTimeMillis())
        }
        db.insert(TABLE_HISTORY, null, values)
    }

    fun getAllHistory(): List<MedicationHistory> {
        val historyList = mutableListOf<MedicationHistory>()
        val db = this.readableDatabase
        val query = "SELECT h.history_id, m.name, m.dosage, h.status, h.timestamp, m.time FROM $TABLE_HISTORY h JOIN $TABLE_MEDICATIONS m ON h.med_id = m.id ORDER BY h.timestamp DESC"
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val history = MedicationHistory(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow("history_id")),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                    dose = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DOSAGE)),
                    time = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME)),
                    date = "", // You may want to format the timestamp to a date string here
                    status = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HISTORY_STATUS)),
                    color = ""
                )
                historyList.add(history)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return historyList
    }

    fun getFilteredHistory(status: String): List<MedicationHistory> {
        val historyList = mutableListOf<MedicationHistory>()
        val db = this.readableDatabase
        val query = "SELECT h.history_id, m.name, m.dosage, h.status, h.timestamp, m.time FROM $TABLE_HISTORY h JOIN $TABLE_MEDICATIONS m ON h.med_id = m.id WHERE h.status = ? ORDER BY h.timestamp DESC"
        val cursor = db.rawQuery(query, arrayOf(status))

        if (cursor.moveToFirst()) {
            do {
                val history = MedicationHistory(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow("history_id")),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                    dose = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DOSAGE)),
                    time = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME)),
                    date = "", // You may want to format the timestamp to a date string here
                    status = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HISTORY_STATUS)),
                    color = ""
                )
                historyList.add(history)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return historyList
    }

    fun clearAllHistory() {
        val db = this.writableDatabase
        db.delete(TABLE_HISTORY, null, null)
    }

    fun recordRefill(medicineId: Long, refillAmount: Int): Boolean {
        val db = writableDatabase
        val cursor = db.rawQuery("SELECT $COLUMN_CURRENT_SUPPLY, $COLUMN_MAX_SUPPLY FROM $TABLE_MEDICATIONS WHERE $COLUMN_ID = ?", 
            arrayOf(medicineId.toString()))
        
        if (cursor.moveToFirst()) {
            val currentSupply = cursor.getInt(0)
            val maxSupply = cursor.getInt(1)
            val newSupply = (currentSupply + refillAmount).coerceAtMost(maxSupply)
            
            val values = ContentValues().apply {
                put(COLUMN_CURRENT_SUPPLY, newSupply)
            }
            cursor.close()
            return db.update(TABLE_MEDICATIONS, values, "$COLUMN_ID = ?", arrayOf(medicineId.toString())) > 0
        }
        cursor.close()
        return false
    }

    fun getTodayTotalDoses(): Int {
        val db = readableDatabase
        // This is a simplified logic. A more robust solution would parse frequency and start date.
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_MEDICATIONS", null)
        var totalDoses = 0
        if (cursor.moveToFirst()) {
            totalDoses = cursor.getInt(0)
        }
        cursor.close()
        return totalDoses
    }

    fun getTodayTakenDoses(): Int {
        val db = readableDatabase
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val todayEnd = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        val query = "SELECT COUNT(*) FROM $TABLE_HISTORY WHERE $COLUMN_HISTORY_STATUS = 'Taken' AND $COLUMN_HISTORY_TIMESTAMP BETWEEN ? AND ?"
        val cursor = db.rawQuery(query, arrayOf(todayStart.toString(), todayEnd.toString()))
        var takenDoses = 0
        if (cursor.moveToFirst()) {
            takenDoses = cursor.getInt(0)
        }
        cursor.close()
        return takenDoses
    }
}
