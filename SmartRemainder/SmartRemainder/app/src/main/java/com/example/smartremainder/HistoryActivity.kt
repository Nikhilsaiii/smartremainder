package com.example.smartremainder

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HistoryActivity : AppCompatActivity() {

    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var btnFilterAll: TextView
    private lateinit var btnFilterTaken: TextView
    private lateinit var btnFilterSkipped: TextView
    private lateinit var btnClearAll: TextView

    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        dbHelper = DatabaseHelper(this)

        initViews()
        setupRecyclerView()
        setupClickListeners()

        loadHistory("All")
    }

    private fun initViews() {
        historyRecyclerView = findViewById(R.id.historyRecyclerView)
        btnFilterAll = findViewById(R.id.btnFilterAll)
        btnFilterTaken = findViewById(R.id.btnFilterTaken)
        btnFilterSkipped = findViewById(R.id.btnFilterSkipped)
        btnClearAll = findViewById(R.id.btnClearAll)
    }

    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter(emptyList())
        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyRecyclerView.adapter = historyAdapter
    }

    private fun setupClickListeners() {
        btnFilterAll.setOnClickListener { 
            loadHistory("All") 
            updateFilterButtons("All")
        }
        btnFilterTaken.setOnClickListener { 
            loadHistory("Taken")
            updateFilterButtons("Taken")
        }
        btnFilterSkipped.setOnClickListener { 
            loadHistory("Skipped") 
            updateFilterButtons("Skipped")
        }
        btnClearAll.setOnClickListener { 
            dbHelper.clearAllHistory()
            loadHistory("All") // Refresh the list
        }
    }

    private fun loadHistory(filter: String) {
        val history = when (filter) {
            "Taken" -> dbHelper.getFilteredHistory("Taken")
            "Skipped" -> dbHelper.getFilteredHistory("Skipped")
            else -> dbHelper.getAllHistory()
        }
        historyAdapter.updateData(history)
    }

    private fun updateFilterButtons(selectedFilter: String) {
        btnFilterAll.isSelected = selectedFilter == "All"
        btnFilterTaken.isSelected = selectedFilter == "Taken"
        btnFilterSkipped.isSelected = selectedFilter == "Skipped"

        btnFilterAll.setBackgroundResource(if (selectedFilter == "All") R.drawable.filter_button_selected else R.drawable.filter_button_unselected)
        btnFilterTaken.setBackgroundResource(if (selectedFilter == "Taken") R.drawable.filter_button_selected else R.drawable.filter_button_unselected)
        btnFilterSkipped.setBackgroundResource(if (selectedFilter == "Skipped") R.drawable.filter_button_selected else R.drawable.filter_button_unselected)

        btnFilterAll.setTextColor(if (selectedFilter == "All") resources.getColor(R.color.white) else resources.getColor(R.color.dark_gray))
        btnFilterTaken.setTextColor(if (selectedFilter == "Taken") resources.getColor(R.color.white) else resources.getColor(R.color.dark_gray))
        btnFilterSkipped.setTextColor(if (selectedFilter == "Skipped") resources.getColor(R.color.white) else resources.getColor(R.color.dark_gray))
    }
}