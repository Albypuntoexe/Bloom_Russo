package com.example.bloom_russo.ui

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bloom_russo.R
import java.time.LocalDate

class EditPeriodAdapter(
    private val context: Context,
    private val onDateClick: (LocalDate) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<CalendarItem>()
    private val selectedDates = mutableSetOf<String>()

    fun submitList(newItems: List<CalendarItem>, newSelections: Set<String>) {
        items.clear()
        items.addAll(newItems)
        selectedDates.clear()
        selectedDates.addAll(newSelections)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is CalendarItem.Header -> 0
            is CalendarItem.Day -> 1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 0) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_calendar_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_edit_day, parent, false)
            DayViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is CalendarItem.Header -> (holder as HeaderViewHolder).bind(item)
            is CalendarItem.Day -> (holder as DayViewHolder).bind(item, selectedDates, onDateClick)
        }
    }

    override fun getItemCount(): Int = items.size

    val spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
        override fun getSpanSize(position: Int): Int {
            return when (items[position]) {
                is CalendarItem.Header -> 7
                is CalendarItem.Day -> 1
            }
        }
    }

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val title: TextView = view.findViewById(R.id.headerTitle)
        fun bind(item: CalendarItem.Header) {
            title.text = item.text
        }
    }

    class DayViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val text: TextView = view.findViewById(R.id.dayText)
        private val bgSelected: ImageView = view.findViewById(R.id.bgSelected)
        private val bgUnselected: ImageView = view.findViewById(R.id.bgUnselected)
        private val checkIcon: ImageView = view.findViewById(R.id.checkIcon)

        fun bind(
            item: CalendarItem.Day,
            selectedDates: Set<String>,
            onClickListener: (LocalDate) -> Unit
        ) {
            if (item.date == null) {
                text.text = ""
                bgSelected.visibility = View.GONE
                bgUnselected.visibility = View.GONE
                checkIcon.visibility = View.GONE
                itemView.setOnClickListener(null)
                return
            }

            text.text = item.date.dayOfMonth.toString()

            // Date Future: Disabilitate
            if (item.date.isAfter(LocalDate.now())) {
                text.setTextColor(Color.LTGRAY)
                bgSelected.visibility = View.GONE
                bgUnselected.visibility = View.GONE
                checkIcon.visibility = View.GONE
                itemView.setOnClickListener(null)
                return
            }

            // Date Normali
            val dateStr = item.date.toString()
            val isSelected = selectedDates.contains(dateStr)

            if (isSelected) {
                bgSelected.visibility = View.VISIBLE
                bgUnselected.visibility = View.GONE
                checkIcon.visibility = View.VISIBLE
                text.setTextColor(Color.WHITE)
            } else {
                bgSelected.visibility = View.GONE
                bgUnselected.visibility = View.VISIBLE
                checkIcon.visibility = View.GONE
                text.setTextColor(Color.BLACK)
            }

            itemView.setOnClickListener {
                onClickListener(item.date)
            }
        }
    }
}