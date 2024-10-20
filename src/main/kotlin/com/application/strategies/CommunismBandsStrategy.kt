package com.application.strategies

import com.application.model.ContributionsResult
import com.application.model.ContributionsStrategy
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.min

class CommunismBandsStrategy : ContributionsStrategy {

        data class Band(val limit: Double, val communismMeter: Double?, val percentage: Double?)

        override fun calculate(
                        person1Earnings: Double,
                        person2Earnings: Double,
                        totalCosts: Double
        ): ContributionsResult {
                val bands =
                                listOf(
                                                Band(
                                                                limit = 300.0,
                                                                communismMeter = null,
                                                                percentage = 50.0
                                                ),
                                                Band(
                                                                limit = 1300.0,
                                                                communismMeter = 1.2,
                                                                percentage = null
                                                ),
                                                Band(
                                                                limit = 3300.0,
                                                                communismMeter = 0.8,
                                                                percentage = null
                                                ),
                                                Band(
                                                                limit = Double.MAX_VALUE,
                                                                communismMeter = 0.6,
                                                                percentage = null
                                                )
                                )

                var remainingCosts = totalCosts
                var person1Contribution = 0.0
                var person2Contribution = 0.0

                var previousLimit = 0.0

                for (band in bands) {
                        val bandAmount = min(band.limit - previousLimit, remainingCosts)
                        if (bandAmount <= 0) {
                                previousLimit = band.limit
                                continue
                        }

                        val (p1Contribution, p2Contribution) =
                                        if (band.percentage != null) {
                                                // Fixed percentage split
                                                val p1C = bandAmount * (band.percentage / 100.0)
                                                val p2C =
                                                                bandAmount *
                                                                                (1 -
                                                                                                (band.percentage /
                                                                                                                100.0))
                                                Pair(p1C, p2C)
                                        } else if (band.communismMeter != null) {
                                                // Calculate percentages using SimpleStrategy with
                                                // specified communismMeter
                                                val (p1Perc, p2Perc) =
                                                                calculatePercentages(
                                                                                person1Earnings,
                                                                                person2Earnings,
                                                                                band.communismMeter
                                                                )
                                                val p1C = bandAmount * (p1Perc / 100.0)
                                                val p2C = bandAmount * (p2Perc / 100.0)
                                                Pair(p1C, p2C)
                                        } else {
                                                throw Exception("Invalid band configuration")
                                        }

                        person1Contribution += p1Contribution
                        person2Contribution += p2Contribution

                        remainingCosts -= bandAmount
                        previousLimit = band.limit

                        if (remainingCosts <= 0) break
                }

                val totalContribution = person1Contribution + person2Contribution
                val person1Percentage = (person1Contribution / totalContribution) * 100.0
                val person2Percentage = (person2Contribution / totalContribution) * 100.0

                return ContributionsResult(
                                person1Percentage,
                                person2Percentage,
                                person1Contribution,
                                person2Contribution
                )
        }

        private fun calculatePercentages(
                        person1Earnings: Double,
                        person2Earnings: Double,
                        communismMeter: Double
        ): Pair<Double, Double> {
                // Same as in SimpleStrategy, but with provided communismMeter
                if (person1Earnings == 0.0 && person2Earnings == 0.0) {
                        return Pair(50.0, 50.0)
                }

                val (higherEarner, lowerEarner) =
                                if (person1Earnings >= person2Earnings) {
                                        person1Earnings to person2Earnings
                                } else {
                                        person2Earnings to person1Earnings
                                }

                val salaryMultiplier = higherEarner / lowerEarner

                fun f(x: Double, communismMeter: Double): Double {
                        return 50 + (100 / PI) * atan((x - 1) / communismMeter)
                }

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

                val p1Percentage =
                                if (person1Earnings >= person2Earnings) {
                                        g(salaryMultiplier, communismMeter)
                                } else {
                                        g(1 / salaryMultiplier, communismMeter)
                                }

                val p2Percentage = 100.0 - p1Percentage

                return Pair(p1Percentage, p2Percentage)
        }
}
