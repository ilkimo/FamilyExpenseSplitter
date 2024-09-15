package com.application.model

import kotlin.math.PI
import kotlin.math.atan

data class ContributionsPercentuals(
    val person1Percentage: Double,
    val person2Percentage: Double
) {
    companion object {
        private const val communismMeter = 1.15

        fun calculate(person1Earnings: Double, person2Earnings: Double): ContributionsPercentuals {
            // Handle zero earnings case
            if (person1Earnings == 0.0 && person2Earnings == 0.0) {
                return ContributionsPercentuals(50.0, 50.0)
            }

            // Determine who earns more
            val (higherEarner, lowerEarner) = if (person1Earnings >= person2Earnings) {
                person1Earnings to person2Earnings
            } else {
                person2Earnings to person1Earnings
            }

            // Calculate salaryMultiplier
            val salaryMultiplier = higherEarner / lowerEarner

            // Implement f(x)
            fun f(x: Double): Double {
                return 50 + (100 / PI) * atan((x - 1) / communismMeter)
            }

            // Implement g(x)
            fun g(x: Double): Double {
                return when {
                    x >= 1 -> f(x)
                    x in 0.0..1.0 -> 100 - f(1 / x)
                    else -> throw IllegalArgumentException("Invalid salary multiplier: $x")
                }
            }

            val person1Percentage = if (person1Earnings >= person2Earnings) {
                g(salaryMultiplier)
            } else {
                g(1 / salaryMultiplier)
            }

            val person2Percentage = 100.0 - person1Percentage

            // Check sum to 100%
            if (kotlin.math.abs(person1Percentage + person2Percentage - 100.0) > 0.01) {
                throw Exception("Percentages do not sum to 100%.")
            }

            return ContributionsPercentuals(person1Percentage, person2Percentage)
        }
    }
}

