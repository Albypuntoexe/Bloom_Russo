package com.example.bloom_russo.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_cycle_data")
data class UserCycleData(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "period_duration")
    var periodDuration: Int = 4,

    @ColumnInfo(name = "cycle_length")
    var cycleLength: Int = 28,

    @ColumnInfo(name = "last_period_date")
    var lastPeriodDate: String? = null
)