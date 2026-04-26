package com.application.model

import java.time.LocalDate

data class SharedDeposit(
    val date: LocalDate,
    val amount: Double,
    val currency: String,
    val depositor: String,
    val bucket: String,
    val kind: String,
    val description: String,
)
