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

    // Per lo scroll automatico
    val currentMonthPosition = MutableLiveData<Int>()

    // UI Data Binding
    val selectedDateText = MutableLiveData<String>()
    val cycleDayText = MutableLiveData<String>()
    val fertilityText = MutableLiveData<String>()
    val selectedDate = MutableLiveData<LocalDate>(LocalDate.now())

    // Cache dei dati
    private var currentUser: UserCycleData? = null
    private var periodDaysSet = setOf<String>() // Cache dei giorni reali spuntati

    init {
        // Carichiamo i dati. Appena cambiano, rigeneriamo il calendario.
        viewModelScope.launch {
            loadDataAndGenerateCalendar()
        }
    }

    // Funzione pubblica per forzare il refresh quando si torna da Edit
    fun refreshData() {
        viewModelScope.launch {
            loadDataAndGenerateCalendar()
        }
    }

    private suspend fun loadDataAndGenerateCalendar() {
        currentUser = dao.getUserDataSync()
        // FIX CRUCIALE: Carichiamo i giorni reali dal DB
        val dbDays = dao.getAllPeriodDays()
        periodDaysSet = dbDays.map { it.date }.toSet()

        generateCalendarData()

        // Aggiorna il pannello inferiore
        onDateSelected(selectedDate.value ?: LocalDate.now())
    }

    fun onDateSelected(date: LocalDate) {
        selectedDate.value = date
        selectedDateText.value = date.format(DateTimeFormatter.ofPattern("MMM d"))

        val user = currentUser

        // Se non c'è utente o non ci sono cicli storici, stato neutro
        if (user == null || user.lastPeriodDate.isNullOrEmpty()) {
            cycleDayText.value = "No cycle data"
            fertilityText.value = "Tap Edit to add period"
            return
        }

        // Calcoli basati sull'ultimo ciclo noto (solo per testo fertilità/giorno ciclo)
        val lastPeriodDate = try { LocalDate.parse(user.lastPeriodDate) } catch (e: Exception) { LocalDate.now() }
        val daysDiff = ChronoUnit.DAYS.between(lastPeriodDate, date)

        if (daysDiff < 0) {
            cycleDayText.value = "Past Cycle"
            fertilityText.value = "Low chance"
        } else {
            val cycleDayIndex = (daysDiff % user.cycleLength).toInt()
            cycleDayText.value = "Cycle Day ${cycleDayIndex + 1}"

            // Testi Fertilità (questi rimangono matematici per previsione)
            val ovulationIndex = user.cycleLength - 14
            val fertileStartIndex = user.cycleLength - 19

            // Però se è un giorno di flusso REALE, scriviamo "Period"
            if (periodDaysSet.contains(date.toString())) {
                fertilityText.value = "Period"
            } else {
                when {
                    cycleDayIndex == ovulationIndex -> fertilityText.value = "HIGH - Ovulation Day"
                    cycleDayIndex >= fertileStartIndex && cycleDayIndex < ovulationIndex -> fertilityText.value = "MEDIUM - Chance of getting pregnant"
                    else -> fertilityText.value = "Low - Chance of getting pregnant"
                }
            }
        }
    }

    private fun generateCalendarData() {
        val items = mutableListOf<CalendarItem>()
        // Mostra 12 mesi indietro e 12 avanti
        val startMonth = YearMonth.now().minusMonths(12)
        val endMonth = YearMonth.now().plusMonths(12)

        var currentMonth = startMonth
        var todayMonthIndex = -1
        var currentIndex = 0

        while (!currentMonth.isAfter(endMonth)) {
            // Salva l'indice se è il mese corrente
            if (currentMonth == YearMonth.now()) {
                todayMonthIndex = currentIndex
            }

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
                val status = calculateDayStatus(date)
                items.add(CalendarItem.Day(date, status))
                currentIndex++
            }
            currentMonth = currentMonth.plusMonths(1)
        }

        calendarItems.postValue(items)

        // Se abbiamo trovato il mese corrente, notifichiamo la View per scrollare
        if (todayMonthIndex != -1) {
            currentMonthPosition.postValue(todayMonthIndex)
        }
    }

    private fun calculateDayStatus(date: LocalDate): DayStatus {
        // 1. REALTÀ: Se è nel DB period_days -> È ROSA (Period). Punto.
        if (periodDaysSet.contains(date.toString())) {
            return DayStatus.PERIOD
        }

        // 2. PREVISIONE FUTURA (Solo se data > oggi)
        if (date.isAfter(LocalDate.now()) && currentUser != null && !currentUser!!.lastPeriodDate.isNullOrEmpty()) {
            val user = currentUser!!
            val lastPeriod = LocalDate.parse(user.lastPeriodDate)

            val daysDiff = ChronoUnit.DAYS.between(lastPeriod, date)
            if (daysDiff >= 0) {
                val cycleDayIndex = (daysDiff % user.cycleLength).toInt()
                val ovulationIndex = user.cycleLength - 14
                val fertileStartIndex = user.cycleLength - 19

                // Previsione mestruazioni future
                if (cycleDayIndex < user.periodDuration) return DayStatus.PREDICTED_PERIOD
                // Previsione fertilità
                if (cycleDayIndex == ovulationIndex) return DayStatus.OVULATION
                if (cycleDayIndex >= fertileStartIndex && cycleDayIndex < ovulationIndex) return DayStatus.FERTILE
            }
        }

        return DayStatus.NONE
    }
}