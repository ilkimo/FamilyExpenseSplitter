package com.application.model

import java.time.YearMonth

data class Person(
    val name: String,
    val earnings: MutableList<Earning> = mutableListOf(),
    val costs: MutableList<Cost> = mutableListOf(),
    val installments: MutableList<Installment> = mutableListOf(),
    var cumulativeCredit: Double = 0.0,
    val monthlyCredit: MutableMap<YearMonth, Double> = mutableMapOf(),
)

