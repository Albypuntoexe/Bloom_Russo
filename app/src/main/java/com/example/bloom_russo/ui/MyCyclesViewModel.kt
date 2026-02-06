package com.example.bloom_russo.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.bloom_russo.data.AppDatabase
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class MyCyclesViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).userDao()
    val cyclesList = MutableLiveData<List<CycleHistoryItem>>()

    init {
        loadCycles()
    }

    private fun loadCycles() {
        viewModelScope.launch {
            val allDays = dao.getAllPeriodDays()

            if (allDays.isEmpty()) {
                cyclesList.postValue(emptyList())
                return@launch
            }

            // 1. Ordiniamo le date dalla più recente
            val sortedDates = allDays
                .map { LocalDate.parse(it.date) }
                .sortedDescending()

            val historyItems = mutableListOf<CycleHistoryItem>()

            // 2. Algoritmo di raggruppamento
            if (sortedDates.isNotEmpty()) {
                var blockEnd = sortedDates[0] // Data più recente del blocco
                var blockStart = sortedDates[0] // Diventerà la data più vecchia del blocco

                for (i in 0 until sortedDates.size - 1) {
                    val current = sortedDates[i]
                    val next = sortedDates[i+1] // Data precedente nel tempo

                    // Se c'è un buco > 1 giorno, il ciclo è finito
                    if (ChronoUnit.DAYS.between(next, current) > 1) {
                        // Chiudiamo il blocco corrente
                        blockStart = current
                        historyItems.add(createItem(blockStart, blockEnd))

                        // Iniziamo nuovo blocco
                        blockEnd = next
                    }
                    // Se sono consecutivi, il blocco continua, aggiorneremo solo lo start alla fine
                }
                // Aggiungi l'ultimo blocco rimasto
                blockStart = sortedDates.last()
                historyItems.add(createItem(blockStart, blockEnd))
            }

            cyclesList.postValue(historyItems)
        }
    }

    private fun createItem(start: LocalDate, end: LocalDate): CycleHistoryItem {
        val formatter = DateTimeFormatter.ofPattern("MMM d")
        // Calcolo durata inclusive (+1)
        val duration = ChronoUnit.DAYS.between(start, end).toInt() + 1
        return CycleHistoryItem(
            startDate = start.format(formatter),
            endDate = end.format(formatter),
            duration = duration,
            year = start.year
        )
    }
}