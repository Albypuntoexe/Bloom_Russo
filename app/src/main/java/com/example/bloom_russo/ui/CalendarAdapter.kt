package com.example.bloom_russo.ui

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bloom_russo.R
import java.time.LocalDate

// Modelli per la lista
sealed class CalendarItem {
    data class Header(val text: String) : CalendarItem()
    data class Day(
        val date: LocalDate?, // Null se è uno spazio vuoto
        val status: DayStatus
    ) : CalendarItem()
}

enum class DayStatus {
    NONE, PERIOD, PREDICTED_PERIOD, FERTILE, OVULATION
}

class CalendarAdapter(private val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<CalendarItem>()

    fun submitList(newItems: List<CalendarItem>) {
        items.clear()
        items.addAll(newItems)
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
            is CalendarItem.Day -> (holder as DayViewHolder).bind(item, context)
        }
    }

    override fun getItemCount(): Int = items.size

    // Configurazione per dire al GridManager che gli Header occupano 7 colonne
    val spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
        override fun getSpanSize(position: Int): Int {
            return when (items[position]) {
                is CalendarItem.Header -> 7 // Occupa tutta la riga
                is CalendarItem.Day -> 1    // Occupa 1 cella
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
        private val icon: View = view.findViewById(R.id.dayIcon)

        fun bind(item: CalendarItem.Day, context: Context) {
            if (item.date == null) {
                text.text = ""
                bg.backgroundTintList = null
                icon.visibility = View.GONE
                return
            }

            text.text = item.date.dayOfMonth.toString()

            // Reset colori
            icon.visibility = View.GONE

            // Imposta colori in base allo status
            val colorRes = when (item.status) {
                DayStatus.PERIOD -> R.color.bloom_period
                DayStatus.PREDICTED_PERIOD -> R.color.bloom_period_light
                DayStatus.FERTILE -> R.color.bloom_fertile
                DayStatus.OVULATION -> R.color.bloom_ovulation
                DayStatus.NONE -> android.R.color.transparent
            }

            // Colore di sfondo
            if (item.status != DayStatus.NONE) {
                bg.backgroundTintList = ContextCompat.getColorStateList(context, colorRes)
            } else {
                bg.backgroundTintList = null
            }

            // Evidenzia "Oggi" con un bordo o testo bold (opzionale)
            if (item.date == LocalDate.now()) {
                text.setTypeface(null, android.graphics.Typeface.BOLD)
                text.setTextColor(ContextCompat.getColor(context, R.color.bloom_primary))
            } else {
                text.setTypeface(null, android.graphics.Typeface.NORMAL)
                text.setTextColor(Color.BLACK)
            }
        }
    }
}