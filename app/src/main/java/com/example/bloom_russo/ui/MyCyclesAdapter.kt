package com.example.bloom_russo.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bloom_russo.R

class MyCyclesAdapter : RecyclerView.Adapter<MyCyclesAdapter.ViewHolder>() {

    private val items = mutableListOf<CycleHistoryItem>()

    fun submitList(newItems: List<CycleHistoryItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cycle_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val dateRange: TextView = view.findViewById(R.id.tvDateRange)
        private val duration: TextView = view.findViewById(R.id.tvDuration)
        private val year: TextView = view.findViewById(R.id.tvYear)

        fun bind(item: CycleHistoryItem) {
            dateRange.text = "${item.startDate} - ${item.endDate}"
            duration.text = "${item.duration} days"
            year.text = item.year.toString()
        }
    }
}