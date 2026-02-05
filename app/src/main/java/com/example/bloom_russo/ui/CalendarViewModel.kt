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

class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).userDao()
    val calendarItems = MutableLiveData<List<CalendarItem>>()

    init {
        viewModelScope.launch {
            val user = dao.getUserDataSync()
            if (user != null) {
                generateCalendarData(user)
            }
        }
    }

    private fun generateCalendarData(user: UserCycleData) {
        val items = mutableListOf<CalendarItem>()
        val startMonth = YearMonth.now().minusMonths(6) // Mostra 6 mesi passati
        val endMonth = YearMonth.now().plusMonths(6)   // e 6 mesi futuri

        val lastPeriodDate = LocalDate.parse(user.lastPeriodDate)

        // Calcoliamo i cicli futuri previsti (approssimazione semplice)
        // In una app reale, si userebbe una logica più complessa per i cicli passati

        var currentMonth = startMonth
        while (!currentMonth.isAfter(endMonth)) {
            // 1. Aggiungi Header Mese
            items.add(CalendarItem.Header(currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))))

            // 2. Calcola padding giorni (se il mese inizia di Mercoledì, servono 3 spazi vuoti)
            // DayOfWeek value: 1 (Mon) -> 7 (Sun). Se vogliamo che Sun sia la prima colonna (come nel layout xml), dobbiamo adattare.
            // Nel layout XML ho messo Sun come primo. Sun=7.
            val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek.value // 1=Mon, .. 7=Sun
            // Se Sun(7) è il primo, e il mese inizia Mon(1), serve 1 spazio.
            // Se il mese inizia Sun(7), servono 0 spazi.
            val emptySlots = if (firstDayOfWeek == 7) 0 else firstDayOfWeek

            for (i in 0 until emptySlots) {
                items.add(CalendarItem.Day(null, DayStatus.NONE))
            }

            // 3. Aggiungi i giorni del mese
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
        // Calcolo giorni trascorsi dall'ultimo ciclo noto
        val daysDiff = java.time.temporal.ChronoUnit.DAYS.between(lastPeriod, date)

        // Se è nel passato prima dell'ultimo ciclo registrato, ignoriamo per semplicità
        if (daysDiff < 0) return DayStatus.NONE

        // Calcoliamo la posizione relativa all'interno di un ciclo teorico
        val cycleDay = (daysDiff % cycleLen).toInt()
        // cycleDay va da 0 a 27 (se ciclo di 28gg)

        return when {
            // Giorni di mestruazioni (es. giorno 0, 1, 2, 3)
            cycleDay < periodLen -> {
                // Se la data è nel futuro, è una previsione
                if (date.isAfter(LocalDate.now())) DayStatus.PREDICTED_PERIOD else DayStatus.PERIOD
            }
            // Ovulazione (circa 14 giorni prima della fine del ciclo)
            // In un ciclo di 28gg, ovulazione al giorno 14 (indice 13)
            cycleDay == (cycleLen - 14) -> DayStatus.OVULATION

            // Finestra fertile (5 giorni prima dell'ovulazione)
            cycleDay >= (cycleLen - 19) && cycleDay < (cycleLen - 14) -> DayStatus.FERTILE

            else -> DayStatus.NONE
        }
    }
}