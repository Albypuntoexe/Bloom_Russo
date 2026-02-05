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

    // Per la logica del Range (click 6 poi 9)
    private var lastToggledDate: LocalDate? = null

    init {
        viewModelScope.launch {
            // 1. Carica i giorni esistenti dal DB ("period_days")
            val savedDays = dao.getAllPeriodDays()

            if (savedDays.isNotEmpty()) {
                // Caso A: Abbiamo già usato l'Edit in passato
                savedDays.forEach { _selectedDates.add(it.date) }
            } else {
                // Caso B (Richiesta 2): Mai usato Edit, ma abbiamo dati Onboarding.
                // Pre-selezioniamo i giorni basandoci sull'ultimo ciclo noto.
                val user = dao.getUserDataSync()
                if (user != null && !user.lastPeriodDate.isNullOrEmpty()) {
                    try {
                        val startDate = LocalDate.parse(user.lastPeriodDate)
                        val duration = user.periodDuration
                        for (i in 0 until duration) {
                            val dateToAdd = startDate.plusDays(i.toLong())
                            _selectedDates.add(dateToAdd.toString())
                        }
                    } catch (e: Exception) { e.printStackTrace() }
                }
            }

            selectedDatesLiveData.postValue(_selectedDates)
            generateCalendarGrid()
        }
    }

    fun toggleDate(date: LocalDate) {
        val dateStr = date.toString()

        // LOGICA RANGE (Richiesta 3)
        // Se abbiamo cliccato una data poco fa e clicchiamo un'altra vicina...
        if (lastToggledDate != null) {
            val prevDate = lastToggledDate!!
            val daysDiff = ChronoUnit.DAYS.between(prevDate, date)
            val absDiff = abs(daysDiff)

            // Se la distanza è tra 1 e 9 giorni, riempiamo il buco
            if (absDiff > 1 && absDiff < 10) {
                val startDate = if (date.isAfter(prevDate)) prevDate else date

                // Riempiamo i giorni in mezzo
                for (i in 1 until absDiff) {
                    val dateInBetween = startDate.plusDays(i)
                    _selectedDates.add(dateInBetween.toString())
                }
            }
        }

        // Logica Standard Toggle
        if (_selectedDates.contains(dateStr)) {
            _selectedDates.remove(dateStr)
            lastToggledDate = null // Se deselezioniamo, resettiamo la logica range
        } else {
            _selectedDates.add(dateStr)
            lastToggledDate = date // Salviamo questa come ultima data cliccata
        }

        selectedDatesLiveData.value = _selectedDates
    }

    fun saveChanges(onComplete: () -> Unit) {
        viewModelScope.launch {
            // 1. Pulisci tutto e riscrivi (metodo sicuro per evitare conflitti)
            val oldList = dao.getAllPeriodDays()
            oldList.forEach { dao.deletePeriodDay(it) }

            // 2. Inserisci i nuovi
            _selectedDates.forEach { dateStr ->
                dao.insertPeriodDay(PeriodDay(dateStr))
            }

            // 3. Aggiorna UserCycleData.lastPeriodDate per la Home
            updateLastPeriodDate()

            onComplete()
        }
    }

    private suspend fun updateLastPeriodDate() {
        if (_selectedDates.isEmpty()) return

        // Troviamo la data più recente inserita
        val maxDateStr = _selectedDates.maxOrNull() ?: return

        val user = dao.getUserDataSync()
        if (user != null) {
            // Troviamo l'INIZIO di quell'ultimo ciclo.
            // (Se ho selezionato 10,11,12,13... la data max è 13, ma l'inizio è 10)
            // Cerchiamo a ritroso dalla data massima finché troviamo date consecutive
            var currentCheck = LocalDate.parse(maxDateStr)
            var potentialStartDate = currentCheck

            while (_selectedDates.contains(currentCheck.minusDays(1).toString())) {
                currentCheck = currentCheck.minusDays(1)
                potentialStartDate = currentCheck
            }

            user.lastPeriodDate = potentialStartDate.toString()
            dao.insertOrUpdate(user)
        }
    }

    private fun generateCalendarGrid() {
        // ... (Codice identico a prima per la griglia) ...
        val items = mutableListOf<CalendarItem>()
        val startMonth = YearMonth.now().minusMonths(3)
        val endMonth = YearMonth.now().plusMonths(1)

        var currentMonth = startMonth
        while (!currentMonth.isAfter(endMonth)) {
            items.add(CalendarItem.Header(currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))))
            val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek.value
            val emptySlots = if (firstDayOfWeek == 7) 0 else firstDayOfWeek
            for (i in 0 until emptySlots) items.add(CalendarItem.Day(null, DayStatus.NONE))

            val daysInMonth = currentMonth.lengthOfMonth()
            for (day in 1..daysInMonth) {
                items.add(CalendarItem.Day(currentMonth.atDay(day), DayStatus.NONE))
            }
            currentMonth = currentMonth.plusMonths(1)
        }
        calendarItems.postValue(items)
    }
}