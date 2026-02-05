package com.example.bloom_russo.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserDao {

    // --- Gestione Dati Utente Principali (UserCycleData) ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(data: UserCycleData)

    @Query("SELECT * FROM user_cycle_data LIMIT 1")
    fun getUserData(): LiveData<UserCycleData?>

    @Query("SELECT * FROM user_cycle_data LIMIT 1")
    suspend fun getUserDataSync(): UserCycleData?

    // --- NUOVO: Gestione Giorni Singoli (PeriodDay) ---

    // Inserisce un giorno come "giorno di mestruazioni"
    @Insert(onConflict = OnConflictStrategy.IGNORE) // IGNORE: se c'è già, non fare nulla
    suspend fun insertPeriodDay(day: PeriodDay)

    // Rimuove un giorno (quando l'utente toglie la spunta)
    @Delete
    suspend fun deletePeriodDay(day: PeriodDay)

    // Ottiene tutti i giorni di mestruazioni salvati nel DB
    @Query("SELECT * FROM period_days")
    suspend fun getAllPeriodDays(): List<PeriodDay>

    // Verifica se un giorno specifico è segnato come mestruazione (utile per il calendario)
    @Query("SELECT EXISTS(SELECT 1 FROM period_days WHERE date = :dateStr)")
    suspend fun isPeriodDay(dateStr: String): Boolean
}