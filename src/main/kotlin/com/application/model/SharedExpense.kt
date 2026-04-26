package com.application.model

import java.time.LocalDate

data class SharedExpense(
    val date: LocalDate,
    val amount: Double,
    val currency: String,
    val bucket: String,
    val description: String,
    val isPianoRecuperoCrediti: Boolean = false,
    val numberOfMonths: Int? = null,
)
