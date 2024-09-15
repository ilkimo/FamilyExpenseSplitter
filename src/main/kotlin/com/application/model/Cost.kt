package com.application.model

import java.time.LocalDate

data class Cost(
    override val date: LocalDate,
    override val amount: Double,
    val currency: String,
    val payer: String,
    val description: String
) : WithDateAndAmount

