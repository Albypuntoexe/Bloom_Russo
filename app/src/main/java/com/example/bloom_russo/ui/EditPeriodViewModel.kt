package com.example.bloom_russo.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.bloom_russo.data.AppDatabase
import com.example.bloom_russo.data.PeriodDay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.abs

class EditPeriodViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).userDao()

    val calendarItems = MutableLiveData<List<CalendarItem>>()

    // Set di stringhe per le date selezionate (Working Copy)
    private val _selectedDates = mutableSetOf<String>()
    val selectedDatesLiveData = MutableLiveData<Set<String>>()

    private var lastToggledDate: LocalDate? = null

    init {
        viewModelScope.launch {
            // 1. CARICA SOLO QUELLO CHE C'È NEL DB. Niente magie.
            val savedDays = dao.getAllPeriodDays()
            savedDays.forEach { _selectedDates.add(it.date) }

            selectedDatesLiveData.postValue(_selectedDates)
            generateCalendarGrid()
        }
    }

    fun toggleDate(date: LocalDate) {
        // Impedisci modifica date future
        if (date.isAfter(LocalDate.now())) return

        val dateStr = date.toString()

        // Logica Range (riempie i buchi se clicchi due date vicine)
        if (lastToggledDate != null) {
            val prevDate = lastToggledDate!!
            val daysDiff = abs(ChronoUnit.DAYS.between(prevDate, date))
            // Se la distanza è tra 2 e 20 giorni, riempiamo il buco
            if (daysDiff > 1 && daysDiff < 20) {
                val startDate = if (date.isAfter(prevDate)) prevDate else date
                for (i in 1 until daysDiff) {
                    val intermediate = startDate.plusDays(i)
                    if (!intermediate.isAfter(LocalDate.now())) {
                        _selectedDates.add(intermediate.toString())
                    }
                }
            }
        }

        if (_selectedDates.contains(dateStr)) {
            _selectedDates.remove(dateStr)
            lastToggledDate = null
        } else {
            _selectedDates.add(dateStr)
            lastToggledDate = date
        }
        selectedDatesLiveData.value = _selectedDates
    }

    fun saveChanges(onComplete: () -> Unit) {
        viewModelScope.launch {
            // 1. Cancella tutto il vecchio DB
            val oldList = dao.getAllPeriodDays()
            oldList.forEach { dao.deletePeriodDay(it) }

            // 2. Scrivi esattamente quello che l'utente ha selezionato
            _selectedDates.forEach { dao.insertPeriodDay(PeriodDay(it)) }

            // 3. Aggiorna la data "Ultimo Ciclo" per la Home
            updateLastPeriodDate()

            onComplete()
        }
    }

    private suspend fun updateLastPeriodDate() {
        val user = dao.getUserDataSync() ?: return

        // SE L'UTENTE HA CANCELLATO TUTTO:
        // Impostiamo lastPeriodDate a NULL. Così la Home saprà che non ci sono cicli attivi.
        if (_selectedDates.isEmpty()) {
            user.lastPeriodDate = null
            dao.insertOrUpdate(user)
            return
        }

        // Altrimenti, troviamo l'inizio del blocco più recente
        val sortedDates = _selectedDates.map { LocalDate.parse(it) }.sortedDescending()

        val mostRecentBlock = mutableListOf<LocalDate>()
        if (sortedDates.isNotEmpty()) {
            mostRecentBlock.add(sortedDates[0])
            for (i in 0 until sortedDates.size - 1) {
                val current = sortedDates[i]
                val next = sortedDates[i+1]
                // Se c'è un buco > 1 giorno, stop
                if (ChronoUnit.DAYS.between(next, current) > 1) break
                mostRecentBlock.add(next)
            }
        }

        val newStartDate = mostRecentBlock.minOrNull() ?: return

        user.lastPeriodDate = newStartDate.toString()
        dao.insertOrUpdate(user)
    }

    private fun generateCalendarGrid() {
        val items = mutableListOf<CalendarItem>()
        // Mostra 12 mesi indietro
        val startMonth = YearMonth.now().minusMonths(12)
        val endMonth = YearMonth.now()

        var currentMonth = startMonth
        while (!currentMonth.isAfter(endMonth)) {
            items.add(CalendarItem.Header(currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))))
            val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek.value
            val emptySlots = if (firstDayOfWeek == 7) 0 else firstDayOfWeek
            for (i in 0 until emptySlots) items.add(CalendarItem.Day(null, DayStatus.NONE))
            val daysInMonth = currentMonth.lengthOfMonth()
            for (day in 1..daysInMonth) items.add(CalendarItem.Day(currentMonth.atDay(day), DayStatus.NONE))
            currentMonth = currentMonth.plusMonths(1)
        }
        calendarItems.postValue(items)
    }
}