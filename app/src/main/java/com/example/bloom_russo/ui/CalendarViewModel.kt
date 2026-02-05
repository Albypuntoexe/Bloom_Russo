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

    // Dati per il pannello inferiore (DataBinding)
    val selectedDateText = MutableLiveData<String>()
    val cycleDayText = MutableLiveData<String>()
    val selectedDate = MutableLiveData<LocalDate>(LocalDate.now())

    init {
        // Imposta testi iniziali
        onDateSelected(LocalDate.now())

        viewModelScope.launch {
            val user = dao.getUserDataSync()
            if (user != null) {
                generateCalendarData(user)
            }
        }
    }

    fun onDateSelected(date: LocalDate) {
        selectedDate.value = date
        // Formatta es. "Jan 13"
        selectedDateText.value = date.format(DateTimeFormatter.ofPattern("MMM d"))

        // Qui calcoleremmo il giorno del ciclo reale. Per ora mettiamo un placeholder o calcolo semplice
        cycleDayText.value = "Cycle Day..."
    }

    private fun generateCalendarData(user: UserCycleData) {
        val items = mutableListOf<CalendarItem>()
        val startMonth = YearMonth.now().minusMonths(6)
        val endMonth = YearMonth.now().plusMonths(6)

        // Conversione sicura della data
        val lastPeriodDate = try {
            if (user.lastPeriodDate != null) LocalDate.parse(user.lastPeriodDate) else LocalDate.now()
        } catch (e: Exception) { LocalDate.now() }

        var currentMonth = startMonth
        while (!currentMonth.isAfter(endMonth)) {
            // Header
            items.add(CalendarItem.Header(currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))))

            // Padding giorni vuoti
            val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek.value // 1=Mon, 7=Sun
            // Adattamento per layout che inizia con Domenica (Sun)
            // Se Sun(7) -> 0 spazi. Se Mon(1) -> 1 spazio.
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

    private fun calculateDayStatus(date: LocalDate, lastPeriod: LocalDate, cycleLen: Int, periodLen: Int): DayStatus {
        val daysDiff = ChronoUnit.DAYS.between(lastPeriod, date)
        if (daysDiff < 0) return DayStatus.NONE

        val cycleDay = (daysDiff % cycleLen).toInt()

        return when {
            cycleDay < periodLen -> {
                if (date.isAfter(LocalDate.now())) DayStatus.PREDICTED_PERIOD else DayStatus.PERIOD
            }
            cycleDay == (cycleLen - 14) -> DayStatus.OVULATION
            cycleDay >= (cycleLen - 19) && cycleDay < (cycleLen - 14) -> DayStatus.FERTILE
            else -> DayStatus.NONE
        }
    }
}