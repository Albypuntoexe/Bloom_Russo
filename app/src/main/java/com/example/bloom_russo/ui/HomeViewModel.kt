package com.example.bloom_russo.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.bloom_russo.data.AppDatabase
import com.example.bloom_russo.data.PeriodDay
import com.example.bloom_russo.data.UserCycleData
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.abs

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).userDao()
    val userData: LiveData<UserCycleData?> = dao.getUserData()

    val mainTitle = MutableLiveData<String>("Loading...")
    val subTitle = MutableLiveData<String>("")
    val buttonText = MutableLiveData<String>("")
    val navigateToEdit = MutableLiveData<Boolean>(false)
    val dataObserver = MediatorLiveData<UserCycleData?>()

    init {
        dataObserver.addSource(userData) { user ->
            if (user != null) calculateCycleStatus(user)
        }
    }

    fun onActionButtonClick() {
        if (buttonText.value == "Period Starts") {
            logPeriodStartToday()
        } else {
            navigateToEdit.value = true
        }
    }

    fun onNavigationComplete() { navigateToEdit.value = false }

    private fun logPeriodStartToday() {
        viewModelScope.launch {
            val todayStr = LocalDate.now().toString()
            // Inserisci oggi
            dao.insertPeriodDay(PeriodDay(todayStr))
            // Aggiorna utente
            val user = dao.getUserDataSync()
            if (user != null) {
                user.lastPeriodDate = todayStr
                dao.insertOrUpdate(user)
            }
        }
    }

    private fun calculateCycleStatus(user: UserCycleData) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val today = LocalDate.now()

        // Se la data è nulla o vuota (es. hai cancellato tutto), resetta lo stato
        val lastPeriodDate = if (user.lastPeriodDate.isNullOrEmpty()) {
            null
        } else {
            try { LocalDate.parse(user.lastPeriodDate, formatter) } catch (e: Exception) { null }
        }

        if (lastPeriodDate == null) {
            mainTitle.value = "Welcome"
            subTitle.value = "Tap to track period"
            buttonText.value = "Period Starts"
            return
        }

        val daysSinceStart = ChronoUnit.DAYS.between(lastPeriodDate, today).toInt()

        if (daysSinceStart >= 0 && daysSinceStart < user.periodDuration) {
            val currentDay = daysSinceStart + 1
            mainTitle.value = "${ordinal(currentDay)} Day"
            subTitle.value = "Period Phase"
            buttonText.value = "Period Ends"
        } else {
            buttonText.value = "Period Starts"

            val nextPeriodDate = lastPeriodDate.plusDays(user.cycleLength.toLong())
            val daysUntilNext = ChronoUnit.DAYS.between(today, nextPeriodDate).toInt()
            val nextDateStr = nextPeriodDate.format(DateTimeFormatter.ofPattern("MMM d"))

            if (daysUntilNext > 0) {
                if (daysUntilNext == 1) mainTitle.value = "1 Day Left"
                else mainTitle.value = "$daysUntilNext Days Left"
                subTitle.value = "Next Period: $nextDateStr"
            } else if (daysUntilNext == 0) {
                mainTitle.value = "Today"
                subTitle.value = "Period expected today"
            } else {
                val daysLate = abs(daysUntilNext)
                mainTitle.value = "$daysLate Days Late"
                subTitle.value = "Expected: $nextDateStr"
            }
        }
    }

    private fun ordinal(i: Int): String {
        val sufixes = arrayOf("th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th")
        return when (i % 100) {
            11, 12, 13 -> i.toString() + "th"
            else -> i.toString() + sufixes[if (i % 10 > 9) 0 else i % 10]
        }
    }
}