package com.example.bloom_russo.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.example.bloom_russo.data.AppDatabase
import com.example.bloom_russo.data.UserCycleData
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).userDao()

    val calendarItems = MutableLiveData<List<CalendarItem>>()
    val selectedDateText = MutableLiveData<String>()
    val cycleDayText = MutableLiveData<String>()
    val fertilityText = MutableLiveData<String>()
    val selectedDate = MutableLiveData<LocalDate>(LocalDate.now())

    private var currentUser: UserCycleData? = null

    // Observer per ricalcolare tutto quando il DB cambia
    private val userDataObserver = MediatorLiveData<UserCycleData?>()

    init {
        // Colleghiamo l'observer al LiveData del database
        userDataObserver.addSource(dao.getUserData()) { user ->
            currentUser = user
            if (user != null) {
                generateCalendarData(user)
                // Rinfresca i testi del pannello inferiore usando i nuovi dati
                onDateSelected(selectedDate.value ?: LocalDate.now())
            }
        }
        // Attiviamo l'osservazione
        userDataObserver.observeForever { }
    }

    // IMPORTANTE: Ricordarsi di rimuovere l'observer quando il VM muore
    override fun onCleared() {
        super.onCleared()
        // Nota: in un'app reale gestiresti meglio il ciclo di vita, ma per ora va bene
    }

    fun onDateSelected(date: LocalDate) {
        selectedDate.value = date
        selectedDateText.value = date.format(DateTimeFormatter.ofPattern("MMM d"))

        val user = currentUser ?: return
        val lastPeriodDate = try {
            if (user.lastPeriodDate != null) LocalDate.parse(user.lastPeriodDate) else LocalDate.now()
        } catch (e: Exception) { LocalDate.now() }

        val daysDiff = ChronoUnit.DAYS.between(lastPeriodDate, date)

        if (daysDiff < 0) {
            cycleDayText.value = "Cycle Day -"
            fertilityText.value = "Low - Chance of getting pregnant"
        } else {
            val cycleDayIndex = (daysDiff % user.cycleLength).toInt()
            cycleDayText.value = "Cycle Day ${cycleDayIndex + 1}"

            val ovulationIndex = user.cycleLength - 14
            val fertileStartIndex = user.cycleLength - 19

            when {
                cycleDayIndex < user.periodDuration -> fertilityText.value = "Period - Low chance of pregnancy"
                cycleDayIndex == ovulationIndex -> fertilityText.value = "HIGH - Ovulation Day"
                cycleDayIndex >= fertileStartIndex && cycleDayIndex < ovulationIndex -> fertilityText.value = "MEDIUM - Chance of getting pregnant"
                else -> fertilityText.value = "Low - Chance of getting pregnant"
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
            items.add(CalendarItem.Header(currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))))
            val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek.value
            val emptySlots = if (firstDayOfWeek == 7) 0 else firstDayOfWeek
            for (i in 0 until emptySlots) items.add(CalendarItem.Day(null, DayStatus.NONE))

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