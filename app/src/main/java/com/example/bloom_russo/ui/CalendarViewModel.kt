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

    val calendarItems = MutableLiveData<List<CalendarItem>>()
    val currentMonthPosition = MutableLiveData<Int>()

    val selectedDateText = MutableLiveData<String>()
    val cycleDayText = MutableLiveData<String>()
    val fertilityText = MutableLiveData<String>()
    val selectedDate = MutableLiveData<LocalDate>(LocalDate.now())

    private var currentUser: UserCycleData? = null

    // Set veloce per controllare se un giorno è di flusso (Period)
    private var periodDaysSet = setOf<String>()

    // Lista ordinata delle DATE DI INIZIO ciclo reali
    private var cycleStartDates = listOf<LocalDate>()

    init {
        viewModelScope.launch { loadDataAndGenerateCalendar() }
    }

    fun refreshData() {
        viewModelScope.launch { loadDataAndGenerateCalendar() }
    }

    private suspend fun loadDataAndGenerateCalendar() {
        currentUser = dao.getUserDataSync()
        val dbDays = dao.getAllPeriodDays()

        // 1. Cache dei giorni di flusso (per colorare di rosa)
        periodDaysSet = dbDays.map { it.date }.toSet()

        // 2. Troviamo le date di INIZIO ciclo reali
        // Un giorno è "inizio" se il giorno prima NON era mestruazione
        val sortedDays = dbDays.map { LocalDate.parse(it.date) }.sorted()
        val starts = mutableListOf<LocalDate>()
        if (sortedDays.isNotEmpty()) {
            starts.add(sortedDays[0]) // Il primo in assoluto è un inizio
            for (i in 1 until sortedDays.size) {
                val current = sortedDays[i]
                val prev = sortedDays[i-1]
                // Se c'è un buco > 1 giorno, 'current' è un nuovo inizio
                if (ChronoUnit.DAYS.between(prev, current) > 1) {
                    starts.add(current)
                }
            }
        }
        cycleStartDates = starts

        generateCalendarData()
        onDateSelected(selectedDate.value ?: LocalDate.now())
    }

    fun onDateSelected(date: LocalDate) {
        selectedDate.value = date
        selectedDateText.value = date.format(DateTimeFormatter.ofPattern("MMM d"))

        val user = currentUser
        if (user == null) {
            cycleDayText.value = ""
            fertilityText.value = ""
            return
        }

        // Calcoliamo lo status esatto per il testo in basso
        val status = calculateDayStatus(date)

        // Se è un giorno di flusso reale
        if (periodDaysSet.contains(date.toString())) {
            // Cerchiamo l'inizio di QUESTO ciclo specifico per dire "Giorno X"
            val anchorDate = findAnchorDate(date)
            if (anchorDate != null) {
                val dayIndex = ChronoUnit.DAYS.between(anchorDate, date).toInt() + 1
                cycleDayText.value = "Cycle Day $dayIndex"
                fertilityText.value = "Period"
            } else {
                cycleDayText.value = "Period"
                fertilityText.value = "Period"
            }
            return
        }

        // Se non è flusso, vediamo se è fertile/ovulazione
        when (status) {
            DayStatus.OVULATION -> {
                fertilityText.value = "HIGH - Ovulation Day"
                updateCycleDayText(date)
            }
            DayStatus.FERTILE -> {
                fertilityText.value = "MEDIUM - Chance of getting pregnant"
                updateCycleDayText(date)
            }
            DayStatus.PREDICTED_PERIOD -> {
                fertilityText.value = "Period Expected"
                updateCycleDayText(date)
            }
            else -> {
                fertilityText.value = "Low - Chance of getting pregnant"
                updateCycleDayText(date)
            }
        }
    }

    private fun updateCycleDayText(date: LocalDate) {
        val anchorDate = findAnchorDate(date)
        if (anchorDate != null) {
            val dayIndex = ChronoUnit.DAYS.between(anchorDate, date).toInt() + 1
            cycleDayText.value = "Cycle Day $dayIndex"
        } else {
            cycleDayText.value = "-"
        }
    }

    // Trova la data di inizio ciclo che "governa" la data target
    private fun findAnchorDate(targetDate: LocalDate): LocalDate? {
        val user = currentUser ?: return null

        // 1. Cerca l'ultimo inizio REALE avvenuto prima o durante la targetDate
        val lastRealStart = cycleStartDates.lastOrNull { !it.isAfter(targetDate) }

        if (lastRealStart != null) {
            // Se siamo entro una lunghezza ragionevole (es. ciclo + 10 giorni), usiamo questo
            val daysDiff = ChronoUnit.DAYS.between(lastRealStart, targetDate)
            if (daysDiff < (user.cycleLength + 10)) {
                return lastRealStart
            }

            // Se siamo molto avanti nel futuro rispetto all'ultimo dato reale,
            // proiettiamo in avanti (Future Prediction)
            if (targetDate.isAfter(LocalDate.now())) {
                // Calcoliamo quanti cicli sono passati teoricamente
                val cyclesPassed = (daysDiff / user.cycleLength)
                return lastRealStart.plusDays(cyclesPassed * user.cycleLength.toLong())
            }
        }

        return null
    }

    private fun generateCalendarData() {
        val items = mutableListOf<CalendarItem>()
        val startMonth = YearMonth.now().minusMonths(12)
        val endMonth = YearMonth.now().plusMonths(12)

        var currentMonth = startMonth
        var todayMonthIndex = -1
        var currentIndex = 0

        while (!currentMonth.isAfter(endMonth)) {
            if (currentMonth == YearMonth.now()) todayMonthIndex = currentIndex
            items.add(CalendarItem.Header(currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))))
            currentIndex++

            val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek.value
            val emptySlots = if (firstDayOfWeek == 7) 0 else firstDayOfWeek
            for (i in 0 until emptySlots) {
                items.add(CalendarItem.Day(null, DayStatus.NONE))
                currentIndex++
            }

            val daysInMonth = currentMonth.lengthOfMonth()
            for (day in 1..daysInMonth) {
                val date = currentMonth.atDay(day)
                items.add(CalendarItem.Day(date, calculateDayStatus(date)))
                currentIndex++
            }
            currentMonth = currentMonth.plusMonths(1)
        }
        calendarItems.postValue(items)
        if (todayMonthIndex != -1) currentMonthPosition.postValue(todayMonthIndex)
    }

    private fun calculateDayStatus(date: LocalDate): DayStatus {
        // 1. REALTÀ
        if (periodDaysSet.contains(date.toString())) return DayStatus.PERIOD

        val user = currentUser ?: return DayStatus.NONE

        // 2. Trova l'ancora (Start Date) per questo giorno
        val anchorDate = findAnchorDate(date) ?: return DayStatus.NONE

        val dayIndex = ChronoUnit.DAYS.between(anchorDate, date).toInt()

        val ovulationIndex = user.cycleLength - 14
        val fertileStartIndex = user.cycleLength - 19

        // Logica Fertilità
        if (dayIndex == ovulationIndex) return DayStatus.OVULATION
        if (dayIndex >= fertileStartIndex && dayIndex < ovulationIndex) return DayStatus.FERTILE

        // Previsione Periodo Futuro
        if (date.isAfter(LocalDate.now()) && dayIndex < user.periodDuration) {
            return DayStatus.PREDICTED_PERIOD
        }

        return DayStatus.NONE
    }
}