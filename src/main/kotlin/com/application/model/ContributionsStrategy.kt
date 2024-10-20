package com.application.model

interface ContributionsStrategy {
    fun calculate(
            person1Earnings: Double,
            person2Earnings: Double,
            totalCosts: Double
    ): ContributionsResult
}
