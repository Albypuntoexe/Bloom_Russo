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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(data: UserCycleData)

    @Update
    suspend fun update(data: UserCycleData)

    @Query("SELECT * FROM user_cycle_data LIMIT 1")
    fun getUserData(): LiveData<UserCycleData?>

    // FIX: Rimossa la parola "suspend".
    // Dato che usiamo allowMainThreadQueries(), possiamo chiamarla direttamente in MainActivity.
    @Query("SELECT * FROM user_cycle_data LIMIT 1")
    fun getUserDataSync(): UserCycleData?

    // --- Gestione Cancellazione Totale ---
    @Query("DELETE FROM user_cycle_data")
    suspend fun deleteAllUserData()

    // --- Period Days ---

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPeriodDay(day: PeriodDay)

    @Delete
    suspend fun deletePeriodDay(day: PeriodDay)

    @Query("SELECT * FROM period_days")
    suspend fun getAllPeriodDays(): List<PeriodDay>

    @Query("DELETE FROM period_days")
    suspend fun deleteAllPeriodDays()
}