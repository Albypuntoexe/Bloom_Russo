package com.example.bloom_russo.ui

import java.time.LocalDate

// Nuova definizione per supportare la UI avanzata
data class CycleHistoryItem(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val periodDuration: Int, // Per la barra rosa
    val cycleLength: Int,    // Per la barra gialla
    val isCurrent: Boolean = false
)