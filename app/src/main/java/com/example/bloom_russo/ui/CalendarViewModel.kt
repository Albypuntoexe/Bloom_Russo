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
    private var periodDaysSet = setOf<String>()
    private var cycleStartDates = listOf<LocalDate>()
    private var lastRecordedDate: LocalDate = LocalDate.MIN

    init {
        viewModelScope.launch { loadDataAndGenerateCalendar() }
    }

    fun refreshData() {
        viewModelScope.launch { loadDataAndGenerateCalendar() }
    }

    private suspend fun loadDataAndGenerateCalendar() {
        currentUser = dao.getUserDataSync()
        val dbDays = dao.getAllPeriodDays()

        periodDaysSet = dbDays.map { it.date }.toSet()
        if (dbDays.isNotEmpty()) {
            lastRecordedDate = dbDays.map { LocalDate.parse(it.date) }.maxOrNull() ?: LocalDate.MIN
        } else {
            lastRecordedDate = LocalDate.MIN
        }

        val sortedDays = dbDays.map { LocalDate.parse(it.date) }.sorted()
        val starts = mutableListOf<LocalDate>()
        if (sortedDays.isNotEmpty()) {
            starts.add(sortedDays[0])
            for (i in 1 until sortedDays.size) {
                val current = sortedDays[i]
                val prev = sortedDays[i-1]
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

        val status = calculateDayStatus(date)

        if (periodDaysSet.contains(date.toString())) {
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

    private fun findAnchorDate(targetDate: LocalDate): LocalDate? {
        val user = currentUser ?: return null

        val lastRealStart = cycleStartDates.lastOrNull { !it.isAfter(targetDate) }

        if (lastRealStart != null) {
            val daysDiff = ChronoUnit.DAYS.between(lastRealStart, targetDate)

            // FIX CRUCIALE: Rimosso il "+ 10". Ora lo switch è matematico preciso.
            // Se superi la lunghezza del ciclo, proiettiamo subito il nuovo ciclo.
            if (daysDiff < user.cycleLength) {
                return lastRealStart
            }

            if (targetDate.isAfter(lastRealStart)) {
                val cyclesPassed = (daysDiff / user.cycleLength)
                return lastRealStart.plusDays(cyclesPassed * user.cycleLength.toLong())
            }
        }
        else if (cycleStartDates.isNotEmpty()) {
            val absoluteLast = cycleStartDates.last()
            if (targetDate.isAfter(absoluteLast)) {
                val daysDiff = ChronoUnit.DAYS.between(absoluteLast, targetDate)
                val cyclesPassed = (daysDiff / user.cycleLength)
                return absoluteLast.plusDays(cyclesPassed * user.cycleLength.toLong())
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
        if (periodDaysSet.contains(date.toString())) return DayStatus.PERIOD

        val user = currentUser ?: return DayStatus.NONE
        val anchorDate = findAnchorDate(date) ?: return DayStatus.NONE

        val dayIndex = ChronoUnit.DAYS.between(anchorDate, date).toInt()

        val ovulationIndex = user.cycleLength - 14
        val fertileStartIndex = user.cycleLength - 19

        if (dayIndex == ovulationIndex) return DayStatus.OVULATION
        if (dayIndex >= fertileStartIndex && dayIndex < ovulationIndex) return DayStatus.FERTILE

        if (date.isAfter(lastRecordedDate) && dayIndex < user.periodDuration) {
            return DayStatus.PREDICTED_PERIOD
        }

        return DayStatus.NONE
    }
}