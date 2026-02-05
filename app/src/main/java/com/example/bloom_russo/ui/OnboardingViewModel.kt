package com.example.bloom_russo.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bloom_russo.data.AppDatabase
import com.example.bloom_russo.data.PeriodDay
import com.example.bloom_russo.data.UserCycleData
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale

class OnboardingViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).userDao()

    var periodDuration: Int = 4
    var cycleLength: Int = 28
    var lastPeriodDate: String = getCurrentDate()

    fun saveData() {
        viewModelScope.launch {
            // 1. Salva dati generali
            val user = UserCycleData(
                periodDuration = periodDuration,
                cycleLength = cycleLength,
                lastPeriodDate = lastPeriodDate
            )
            dao.insertOrUpdate(user)

            // 2. Popola la tabella period_days in base ai dati inseriti
            try {
                val startDate = LocalDate.parse(lastPeriodDate)
                for (i in 0 until periodDuration) {
                    val dateToAdd = startDate.plusDays(i.toLong())
                    dao.insertPeriodDay(PeriodDay(dateToAdd.toString()))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }
}