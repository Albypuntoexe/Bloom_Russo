package com.example.bloom_russo.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.bloom_russo.data.AppDatabase
import com.example.bloom_russo.data.UserCycleData
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.abs

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).userDao()

    // Osserviamo i dati dal DB
    val userData: LiveData<UserCycleData?> = dao.getUserData()

    // Variabili per la UI (DataBinding)
    val mainTitle = MutableLiveData<String>()     // Es. "1st Day" o "5 Days Left"
    val subTitle = MutableLiveData<String>()      // Es. "Feb 3 - Next Period"
    val buttonText = MutableLiveData<String>()    // "Period Ends" o "Period Starts"
    val isPeriodActive = MutableLiveData<Boolean>() // Per cambiare colori o icone se necessario

    // Questo osservatore ricalcola tutto ogni volta che i dati utente cambiano
    val dataObserver = MediatorLiveData<UserCycleData?>()

    init {
        dataObserver.addSource(userData) { user ->
            user?.let { calculateCycleStatus(it) }
        }
    }

    private fun calculateCycleStatus(user: UserCycleData) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val today = LocalDate.now()

        // Se non c'è una data valida, esci
        val lastPeriodStr = user.lastPeriodDate ?: return
        val lastPeriodDate = LocalDate.parse(lastPeriodStr, formatter)

        val daysSinceStart = ChronoUnit.DAYS.between(lastPeriodDate, today).toInt()

        if (daysSinceStart < user.periodDuration) {
            // CASO 1: DURANTE IL CICLO (Image 2.png)
            val currentDay = daysSinceStart + 1
            mainTitle.value = "${ordinal(currentDay)} Day"
            subTitle.value = "Period Phase"
            buttonText.value = "Period Ends"
            isPeriodActive.value = true
        } else {
            // CASO 2: DOPO IL CICLO (Image 3.png e 4.png)
            isPeriodActive.value = false
            buttonText.value = "Period Starts"

            val nextPeriodDate = lastPeriodDate.plusDays(user.cycleLength.toLong())
            val daysUntilNext = ChronoUnit.DAYS.between(today, nextPeriodDate).toInt()

            // Formatta la data del prossimo ciclo (es. "Feb 10")
            val nextDateStr = nextPeriodDate.format(DateTimeFormatter.ofPattern("MMM d"))

            if (daysUntilNext > 0) {
                // Mancano giorni (Image 4.png)
                if (daysUntilNext == 1) {
                    mainTitle.value = "1 Day Left"
                } else {
                    mainTitle.value = "$daysUntilNext Days Left"
                }
                subTitle.value = "Next Period: $nextDateStr"
            } else if (daysUntilNext == 0) {
                // Oggi
                mainTitle.value = "Today"
                subTitle.value = "Period expected today"
            } else {
                // In ritardo (Image 3.png)
                val daysLate = abs(daysUntilNext)
                mainTitle.value = "$daysLate Days Late"
                subTitle.value = "Expected: $nextDateStr"
            }
        }
    }

    // Helper per trasformare 1 in "1st", 2 in "2nd", ecc.
    private fun ordinal(i: Int): String {
        val sufixes = arrayOf("th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th")
        return when (i % 100) {
            11, 12, 13 -> i.toString() + "th"
            else -> i.toString() + sufixes[if (i % 10 > 9) 0 else i % 10]
        }
    }
}