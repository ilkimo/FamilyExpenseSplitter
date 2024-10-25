package com.application.model

import java.time.YearMonth

data class Installment(
    val date: YearMonth,
    val amount: Double,
)
