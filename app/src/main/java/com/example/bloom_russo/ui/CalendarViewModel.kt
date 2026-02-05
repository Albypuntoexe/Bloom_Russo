package com.example.bloom_russo.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.bloom_russo.data.AppDatabase
import com.example.bloom_russo.data.UserCycleData
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).userDao()

    // Dati della lista calendario
    val calendarItems = MutableLiveData<List<CalendarItem>>()

    // Dati per il pannello inferiore (DataBinding)
    val selectedDateText = MutableLiveData<String>()
    val cycleDayText = MutableLiveData<String>()
    val fertilityText = MutableLiveData<String>()

    // Variabile per tenere traccia della data selezionata
    val selectedDate = MutableLiveData<LocalDate>(LocalDate.now())

    private var currentUser: UserCycleData? = null

    init {
        // Carica i dati dal DB
        viewModelScope.launch {
            currentUser = dao.getUserDataSync()
            if (currentUser != null) {
                generateCalendarData(currentUser!!)
                // Aggiorna i testi per la data di oggi appena i dati sono pronti
                onDateSelected(selectedDate.value ?: LocalDate.now())
            }
        }
    }

    // Questa funzione viene chiamata quando l'utente clicca una data
    fun onDateSelected(date: LocalDate) {
        selectedDate.value = date
        // Aggiorna Titolo Data (es. "Jan 13")
        selectedDateText.value = date.format(DateTimeFormatter.ofPattern("MMM d"))

        val user = currentUser ?: return

        val lastPeriodDate = try {
            if (user.lastPeriodDate != null) LocalDate.parse(user.lastPeriodDate) else LocalDate.now()
        } catch (e: Exception) { LocalDate.now() }

        // Calcola i giorni di differenza
        val daysDiff = ChronoUnit.DAYS.between(lastPeriodDate, date)

        if (daysDiff < 0) {
            // Se clicchi una data precedente all'ultimo ciclo registrato
            cycleDayText.value = "Cycle Day -"
            fertilityText.value = "Low - Chance of getting pregnant"
        } else {
            // Calcolo Indice Giorno (0, 1, ... 27)
            val cycleDayIndex = (daysDiff % user.cycleLength).toInt()

            // Per il testo "Cycle Day X", aggiungiamo 1 perché agli umani piace contare da 1
            cycleDayText.value = "Cycle Day ${cycleDayIndex + 1}"

            // Parametri Matematici (Devono essere IDENTICI a generateCalendarData)
            // In un ciclo di 28gg: Index 14 è ovulazione (15° giorno)
            val ovulationIndex = user.cycleLength - 14
            val fertileStartIndex = user.cycleLength - 19

            when {
                // Periodo Mestruale
                cycleDayIndex < user.periodDuration -> {
                    fertilityText.value = "Period - Low chance of pregnancy"
                }

                // Giorno Ovulazione (High) - Uovo
                cycleDayIndex == ovulationIndex -> {
                    fertilityText.value = "HIGH - Ovulation Day"
                }

                // Finestra Fertile (Medium) - Foglia
                cycleDayIndex >= fertileStartIndex && cycleDayIndex < ovulationIndex -> {
                    fertilityText.value = "MEDIUM - Chance of getting pregnant"
                }

                // Tutto il resto (Low)
                else -> {
                    fertilityText.value = "Low - Chance of getting pregnant"
                }
            }
        }
    }

    private fun generateCalendarData(user: UserCycleData) {
        val items = mutableListOf<CalendarItem>()
        val startMonth = YearMonth.now().minusMonths(6)
        val endMonth = YearMonth.now().plusMonths(6)

        val lastPeriodDate = try {
            if (user.lastPeriodDate != null) LocalDate.parse(user.lastPeriodDate) else LocalDate.now()
        } catch (e: Exception) { LocalDate.now() }

        var currentMonth = startMonth
        while (!currentMonth.isAfter(endMonth)) {
            // Header Mese
            items.add(CalendarItem.Header(currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))))

            // Padding giorni vuoti
            val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek.value
            val emptySlots = if (firstDayOfWeek == 7) 0 else firstDayOfWeek

            for (i in 0 until emptySlots) {
                items.add(CalendarItem.Day(null, DayStatus.NONE))
            }

            // Giorni
            val daysInMonth = currentMonth.lengthOfMonth()
            for (day in 1..daysInMonth) {
                val date = currentMonth.atDay(day)
                val status = calculateDayStatus(date, lastPeriodDate, user.cycleLength, user.periodDuration)
                items.add(CalendarItem.Day(date, status))
            }

            currentMonth = currentMonth.plusMonths(1)
        }
        calendarItems.postValue(items)
    }

    // Logica condivisa per determinare lo status visivo
    private fun calculateDayStatus(date: LocalDate, lastPeriod: LocalDate, cycleLen: Int, periodLen: Int): DayStatus {
        val daysDiff = ChronoUnit.DAYS.between(lastPeriod, date)
        if (daysDiff < 0) return DayStatus.NONE

        // Indice 0-based
        val cycleDayIndex = (daysDiff % cycleLen).toInt()

        val ovulationIndex = cycleLen - 14
        val fertileStartIndex = cycleLen - 19

        return when {
            cycleDayIndex < periodLen -> {
                if (date.isAfter(LocalDate.now())) DayStatus.PREDICTED_PERIOD else DayStatus.PERIOD
            }
            cycleDayIndex == ovulationIndex -> DayStatus.OVULATION
            cycleDayIndex >= fertileStartIndex && cycleDayIndex < ovulationIndex -> DayStatus.FERTILE
            else -> DayStatus.NONE
        }
    }
}