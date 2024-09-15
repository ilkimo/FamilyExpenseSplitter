package com.application.model

import java.time.LocalDate

interface WithDateAndAmount {
    val date: LocalDate
    val amount: Double
}
