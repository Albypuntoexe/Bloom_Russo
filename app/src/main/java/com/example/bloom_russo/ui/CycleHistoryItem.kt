package com.example.bloom_russo.ui

// Rappresenta una riga nella lista dello storico
data class CycleHistoryItem(
    val startDate: String, // "Feb 6"
    val endDate: String,   // "Feb 10"
    val duration: Int,     // 5 days
    val year: Int          // 2026 (per raggruppare se volessimo)
)