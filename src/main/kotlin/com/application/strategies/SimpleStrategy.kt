package com.application.strategies

import com.application.model.ContributionsResult
import com.application.model.ContributionsStrategy
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan

class SimpleStrategy : ContributionsStrategy {
        override fun calculate(
                        person1Earnings: Double,
                        person2Earnings: Double,
                        totalCosts: Double
        ): ContributionsResult {
                // Handle zero earnings case
                if (person1Earnings == 0.0 && person2Earnings == 0.0) {
                        val percentage = 50.0
                        val contribution = totalCosts * 0.5
                        return ContributionsResult(
                                        percentage,
                                        percentage,
                                        contribution,
                                        contribution
                        )
                }

                // Determine who earns more
                val (higherEarner, lowerEarner) =
                                if (person1Earnings >= person2Earnings) {
                                        person1Earnings to person2Earnings
                                } else {
                                        person2Earnings to person1Earnings
                                }

                // Calculate salaryMultiplier
                val salaryMultiplier = higherEarner / lowerEarner

                // Implement f(x)
                fun f(x: Double, communismMeter: Double): Double {
                        return 50 + (100 / PI) * atan((x - 1) / communismMeter)
                }

                // Implement g(x)
                fun g(x: Double, communismMeter: Double): Double {
                        return when {
                                x >= 1 -> f(x, communismMeter)
                                x in 0.0..1.0 -> 100 - f(1 / x, communismMeter)
                                else ->
                                                throw IllegalArgumentException(
                                                                "Invalid salary multiplier: $x"
                                                )
                        }
                }

                val communismMeter = 1.15

                val person1Percentage =
                                if (person1Earnings >= person2Earnings) {
                                        g(salaryMultiplier, communismMeter)
                                } else {
                                        g(1 / salaryMultiplier, communismMeter)
                                }

                val person2Percentage = 100.0 - person1Percentage

                // Check sum to 100%
                if (abs(person1Percentage + person2Percentage - 100.0) > 0.01) {
                        throw Exception("Percentages do not sum to 100%.")
                }

                val person1Contribution = totalCosts * (person1Percentage / 100)
                val person2Contribution = totalCosts * (person2Percentage / 100)

                return ContributionsResult(
                                person1Percentage,
                                person2Percentage,
                                person1Contribution,
                                person2Contribution
                )
        }
}
