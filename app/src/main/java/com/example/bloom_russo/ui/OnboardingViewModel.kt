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
    // Inizializziamo a null. Se l'utente non sceglie, resta null.
    var lastPeriodDate: String? = null

    // Funzione modificata: accetta un booleano per sapere se abbiamo saltato
    fun saveData(isSkipped: Boolean) {
        viewModelScope.launch {
            // Se è skipped, forziamo null, altrimenti usiamo il valore (o oggi se null per errore)
            val dateToSave = if (isSkipped) null else (lastPeriodDate ?: getCurrentDate())

            // 1. Salva dati utente (con data NULL se skipped)
            val user = UserCycleData(
                periodDuration = periodDuration,
                cycleLength = cycleLength,
                lastPeriodDate = dateToSave
            )
            dao.insertOrUpdate(user)

            // 2. IMPORTANTE: Genera i giorni in period_days SOLO se NON abbiamo saltato
            if (!isSkipped && dateToSave != null) {
                try {
                    val startDate = LocalDate.parse(dateToSave)
                    for (i in 0 until periodDuration) {
                        val dateToAdd = startDate.plusDays(i.toLong())
                        dao.insertPeriodDay(PeriodDay(dateToAdd.toString()))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                // Se abbiamo saltato, assicuriamoci che la tabella period_days sia VUOTA
                // per evitare "fantasmi" di vecchie installazioni
                val allDays = dao.getAllPeriodDays()
                allDays.forEach { dao.deletePeriodDay(it) }
            }
        }
    }

    // Helper per settare la data di oggi quando si apre il DatePicker
    fun setDateToToday() {
        lastPeriodDate = getCurrentDate()
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }
}