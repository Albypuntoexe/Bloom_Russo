package com.example.bloom_russo.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bloom_russo.R
import java.time.format.DateTimeFormatter

class MyCyclesAdapter : RecyclerView.Adapter<MyCyclesAdapter.ViewHolder>() {

    private val items = mutableListOf<CycleHistoryItem>()
    private val dateFormatter = DateTimeFormatter.ofPattern("MMM d")

    fun submitList(newItems: List<CycleHistoryItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cycle_history_new, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], dateFormatter)
    }

    override fun getItemCount() = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val dateRange: TextView = view.findViewById(R.id.tvDateRange)
        private val cycleLength: TextView = view.findViewById(R.id.tvCycleLength)
        private val periodProgressBar: ProgressBar = view.findViewById(R.id.periodProgressBar)
        private val cycleProgressBar: ProgressBar = view.findViewById(R.id.cycleProgressBar)
        private val tvCurrent: TextView = view.findViewById(R.id.tvCurrent)

        fun bind(item: CycleHistoryItem, formatter: DateTimeFormatter) {
            dateRange.text = "${item.startDate.format(formatter)} - ${item.endDate.format(formatter)}"
            cycleLength.text = "${item.cycleLength} Days"

            // Imposta le barre di avanzamento
            // Assumiamo una lunghezza massima del ciclo di 35 giorni per la scala
            val maxScale = 35
            periodProgressBar.max = maxScale
            periodProgressBar.progress = item.periodDuration

            cycleProgressBar.max = maxScale
            cycleProgressBar.progress = item.cycleLength

            if (item.isCurrent) {
                tvCurrent.visibility = View.VISIBLE
            } else {
                tvCurrent.visibility = View.GONE
            }
        }
    }
}