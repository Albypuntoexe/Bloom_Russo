package com.example.bloom_russo.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.bloom_russo.data.AppDatabase
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class AnalysisViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).userDao()

    // Lista per l'Adapter
    val cyclesList = MutableLiveData<List<CycleHistoryItem>>()

    // Medie per le Card
    val averagePeriod = MutableLiveData<Int>(0)
    val averageCycle = MutableLiveData<Int>(0)
    val totalCycles = MutableLiveData<Int>(0)

    init {
        loadCycles()
    }

    private fun loadCycles() {
        viewModelScope.launch {
            val allDays = dao.getAllPeriodDays()
            val userData = dao.getUserDataSync()

            if (allDays.isEmpty() || userData == null) {
                cyclesList.postValue(emptyList())
                return@launch
            }

            // Convertiamo stringhe in LocalDate
            val sortedDates = allDays.map { LocalDate.parse(it.date) }.sortedDescending()
            val cycleStarts = mutableListOf<LocalDate>()

            // 1. Identifica date di inizio ciclo (buchi > 1 giorno)
            if (sortedDates.isNotEmpty()) {
                cycleStarts.add(sortedDates[0])
                for (i in 0 until sortedDates.size - 1) {
                    val current = sortedDates[i]
                    val prev = sortedDates[i+1]
                    if (ChronoUnit.DAYS.between(prev, current) > 1) {
                        cycleStarts.add(prev)
                    }
                }
            }
            // Ordina dal più recente
            cycleStarts.sortDescending()

            val historyItems = mutableListOf<CycleHistoryItem>()
            var totalPeriodDays = 0
            var totalCycleDays = 0
            var cycleCount = 0

            for (i in 0 until cycleStarts.size) {
                val start = cycleStarts[i]
                // Fine teorica basata sulla durata media (per la visualizzazione grafica)
                val end = start.plusDays(userData.periodDuration.toLong() - 1)

                // Calcola lunghezza ciclo rispetto al prossimo inizio
                val nextStart = if (i > 0) cycleStarts[i - 1] else null
                val cycleLen = if (nextStart != null) {
                    ChronoUnit.DAYS.between(start, nextStart).toInt()
                } else {
                    userData.cycleLength // Default per l'ultimo
                }

                // CREAZIONE OGGETTO CORRETTO
                historyItems.add(CycleHistoryItem(
                    startDate = start,
                    endDate = end,
                    periodDuration = userData.periodDuration,
                    cycleLength = cycleLen,
                    isCurrent = (i == 0) // Il primo della lista è quello corrente
                ))

                if (nextStart != null) {
                    totalPeriodDays += userData.periodDuration
                    totalCycleDays += cycleLen
                    cycleCount++
                }
            }

            cyclesList.postValue(historyItems)

            if (cycleCount > 0) {
                averagePeriod.postValue(totalPeriodDays / cycleCount)
                averageCycle.postValue(totalCycleDays / cycleCount)
                totalCycles.postValue(cycleCount)
            } else {
                averagePeriod.postValue(userData.periodDuration)
                averageCycle.postValue(userData.cycleLength)
                totalCycles.postValue(historyItems.size)
            }
        }
    }
}