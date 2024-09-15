
package com.application.model

import java.time.LocalDate

data class Earning(
    override val date: LocalDate,
    override val amount: Double
) : WithDateAndAmount
