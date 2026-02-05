package com.example.bloom_russo.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "period_days")
data class PeriodDay(
    @PrimaryKey
    val date: String // Formato YYYY-MM-DD. Usiamo la data come chiave primaria perché un giorno può essere inserito una sola volta.
)