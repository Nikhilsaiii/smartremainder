package com.example.smartremainder

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartremainder.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var userDbHelper: UserDatabaseHelper
    private lateinit var medicationAdapter: MedicationAdapter
    private lateinit var alarmScheduler: AlarmScheduler
    private var permissionsRequested = false

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            // After user responds to the permission dialog, we can check the exact alarm permission again in onResume
            permissionsRequested = true // Prevents re-requesting in an infinite loop
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        userDbHelper = UserDatabaseHelper(this)
        alarmScheduler = AlarmScheduler(this)

        binding.fabAddMedication.setOnClickListener {
            startActivity(Intent(this, AddMedicineActivity::class.java))
        }

        binding.quickActions.llAddMedication.setOnClickListener {
            startActivity(Intent(this, AddMedicineActivity::class.java))
        }

        binding.quickActions.llCalendarView.setOnClickListener {
            startActivity(Intent(this, CalendarActivity::class.java))
        }

        binding.quickActions.llHistoryLog.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        binding.quickActions.llRefillTracker.setOnClickListener {
            startActivity(Intent(this, RefillActivity::class.java))
        }

        binding.ivProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        setupRecyclerView()
        loadUserProfile()
    }

    override fun onResume() {
        super.onResume()
        // Check permissions every time the activity is resumed.
        if (!permissionsRequested) {
             checkAndRequestPermissions()
        }
        loadMedications()
        loadUserProfile()
        updateDailyProgress()
    }

    private fun updateDailyProgress() {
        val totalDoses = dbHelper.getTodayTotalDoses()
        val takenDoses = dbHelper.getTodayTakenDoses()

        val percentage = if (totalDoses > 0) {
            (takenDoses * 100) / totalDoses
        } else {
            0
        }

        binding.circularProgressBar.progress = percentage
        binding.tvPercentage.text = "$percentage%"
        binding.tvDoses.text = "$takenDoses of $totalDoses doses"
    }

    private fun loadUserProfile() {
        val user = userDbHelper.getUserProfile()
        if (user != null) {
            binding.tvUserName.text = "Welcome, ${user.name}!"
        } else {
            binding.tvUserName.text = "Welcome, User!"
        }
    }

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        var needsExactAlarmPermission = false

        // 1. Check for Notification Permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // 2. Check for Exact Alarm Permission (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                needsExactAlarmPermission = true
            }
        }

        // 3. Request permissions if needed
        if (permissionsToRequest.isNotEmpty()) {
            // Request standard permissions like POST_NOTIFICATIONS
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else if (needsExactAlarmPermission) {
            // If only exact alarm permission is needed, go to settings
            // This is a special permission that requires user to go to a settings screen.
            permissionsRequested = true // Mark as requested to avoid loops
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.fromParts("package", packageName, null)
            }
            startActivity(intent)
        }
    }


    private fun setupRecyclerView() {
        medicationAdapter = MedicationAdapter(emptyList()) { medication ->
            deleteMedication(medication)
        }
        binding.rvMedications.layoutManager = LinearLayoutManager(this)
        binding.rvMedications.adapter = medicationAdapter
    }

    private fun loadMedications() {
        val medications = dbHelper.getAllMedications()
        medicationAdapter.updateData(medications)
    }

    private fun deleteMedication(medication: Medication) {
        dbHelper.deleteMedication(medication.id)
        alarmScheduler.cancelAlarm(medication.id)
        loadMedications()
    }
}