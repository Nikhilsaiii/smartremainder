package com.example.smartremainder

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HistoryAdapter(private var historyList: List<MedicationHistory>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val history = historyList[position]
        holder.medicationName.text = history.name
        holder.medicationDosage.text = history.dose
        holder.medicationTime.text = history.time
        holder.medicationStatus.text = history.status

        if (history.status.equals("Taken", ignoreCase = true)) {
            holder.medicationStatus.setTextColor(Color.parseColor("#4CAF50")) // Green
        } else {
            holder.medicationStatus.setTextColor(Color.parseColor("#EF5350")) // Red
        }

        try {
            holder.colorBar.setBackgroundColor(Color.parseColor(history.color))
        } catch (e: Exception) {
            holder.colorBar.setBackgroundColor(Color.GRAY)
        }
    }

    override fun getItemCount(): Int {
        return historyList.size
    }

    fun updateData(newHistoryList: List<MedicationHistory>) {
        historyList = newHistoryList
        notifyDataSetChanged()
    }

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val colorBar: View = itemView.findViewById(R.id.colorBar)
        val medicationName: TextView = itemView.findViewById(R.id.medicationName)
        val medicationDosage: TextView = itemView.findViewById(R.id.medicationDosage)
        val medicationTime: TextView = itemView.findViewById(R.id.medicationTime)
        val medicationStatus: TextView = itemView.findViewById(R.id.medicationStatus)
    }
}