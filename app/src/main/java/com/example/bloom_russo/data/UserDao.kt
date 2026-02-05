package com.example.bloom_russo.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
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

    @Query("SELECT * FROM user_cycle_data LIMIT 1")
    suspend fun getUserDataSync(): UserCycleData?
}