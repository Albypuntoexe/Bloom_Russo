package com.example.bloom_russo.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bloom_russo.data.AppDatabase
import com.example.bloom_russo.data.UserCycleData
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OnboardingViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).userDao()

    // Valori di default
    var periodDuration: Int = 4
    var cycleLength: Int = 28
    var lastPeriodDate: String = getCurrentDate()

    fun saveData() {
        viewModelScope.launch {
            val user = UserCycleData(
                periodDuration = periodDuration,
                cycleLength = cycleLength,
                lastPeriodDate = lastPeriodDate
            )
            dao.insertOrUpdate(user)
        }
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }
}