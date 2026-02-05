package com.example.bloom_russo.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bloom_russo.R
import java.time.LocalDate

// Classi dati necessarie per l'Adapter
sealed class CalendarItem {
    data class Header(val text: String) : CalendarItem()
    data class Day(val date: LocalDate?, val status: DayStatus) : CalendarItem()
}

enum class DayStatus {
    NONE, PERIOD, PREDICTED_PERIOD, FERTILE, OVULATION
}

class CalendarAdapter(
    private val context: Context,
    private val onDateClick: (LocalDate) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<CalendarItem>()
    private var selectedDate: LocalDate = LocalDate.now()

    fun submitList(newItems: List<CalendarItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun setSelectedDate(date: LocalDate) {
        selectedDate = date
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
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_calendar_day, parent, false)
            DayViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is CalendarItem.Header -> (holder as HeaderViewHolder).bind(item)
            is CalendarItem.Day -> (holder as DayViewHolder).bind(item, context, selectedDate, onDateClick)
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
        private val bg: View = view.findViewById(R.id.dayBackground)
        private val iconFertile: ImageView = view.findViewById(R.id.iconFertile)
        private val iconOvulation: ImageView = view.findViewById(R.id.iconOvulation)
        private val selectionBorder: View = view.findViewById(R.id.selectionBorder)

        fun bind(
            item: CalendarItem.Day,
            context: Context,
            selectedDate: LocalDate,
            onClickListener: (LocalDate) -> Unit
        ) {
            if (item.date == null) {
                text.text = ""
                bg.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
                iconFertile.visibility = View.GONE
                iconOvulation.visibility = View.GONE
                selectionBorder.visibility = View.GONE
                itemView.setOnClickListener(null)
                return
            }

            text.text = item.date.dayOfMonth.toString()

            // Reset visuale
            iconFertile.visibility = View.GONE
            iconOvulation.visibility = View.GONE

            when (item.status) {
                DayStatus.PERIOD -> {
                    bg.setBackgroundColor(ContextCompat.getColor(context, R.color.bloom_period))
                    bg.alpha = 1.0f
                }
                DayStatus.PREDICTED_PERIOD -> {
                    bg.setBackgroundColor(ContextCompat.getColor(context, R.color.bloom_period_light))
                    bg.alpha = 1.0f
                }
                DayStatus.FERTILE -> {
                    bg.setBackgroundColor(ContextCompat.getColor(context, R.color.bloom_fertile))
                    iconFertile.visibility = View.VISIBLE
                    bg.alpha = 1.0f
                }
                DayStatus.OVULATION -> {
                    bg.setBackgroundColor(ContextCompat.getColor(context, R.color.bloom_fertile))
                    iconOvulation.visibility = View.VISIBLE
                    bg.alpha = 1.0f
                }
                else -> {
                    bg.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
                }
            }

            if (item.date == selectedDate) {
                selectionBorder.visibility = View.VISIBLE
            } else {
                selectionBorder.visibility = View.GONE
            }

            itemView.setOnClickListener {
                onClickListener(item.date)
            }
        }
    }
}