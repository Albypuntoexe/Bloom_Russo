package com.example.bloom_russo.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// AGGIUNTO PeriodDay::class alle entities e cambiato version = 2
@Database(entities = [UserCycleData::class, PeriodDay::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bloom_database"
                )
                    .allowMainThreadQueries()
                    // Questo cancellerà il DB vecchio e ne creerà uno nuovo con la nuova struttura
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}