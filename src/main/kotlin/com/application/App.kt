package com.application

import com.application.model.Person
import com.application.model.Couple
import com.application.model.ContributionsPercentuals
import com.application.model.Earning
import com.application.model.Cost
import com.application.model.WithDateAndAmount
import com.application.parsers.SalaryParser
import com.application.parsers.SpeseParser
import kotlin.system.exitProcess
import kotlin.math.abs
import java.io.File
import org.knowm.xchart.XYChartBuilder
import org.knowm.xchart.BitmapEncoder
import org.knowm.xchart.XYSeries
import org.knowm.xchart.style.Styler
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import org.knowm.xchart.*
import java.awt.Color
import java.nio.file.Paths
import java.time.ZoneId
import java.util.Date

fun main() {
    try {
        val persons = mutableMapOf<String, Person>()

        val salaryParser = SalaryParser("src/main/resources/data/salari.csv", persons)
        salaryParser.parse()

        val speseParser = SpeseParser("src/main/resources/data/spese.csv", persons)
        speseParser.parse()

        if (persons.size != 2) {
            throw Exception("The application supports exactly two persons.")
        }

        val personList = persons.values.toList()
        val couple = Couple(personList[0], personList[1])

        // Calculate contributions
        val contributions = calculateContributions(couple)
	printContributions(contributions, couple)

        // Generate plot
        generatePlot(contributions, couple)

    } catch (e: Exception) {
        println("An error occurred: ${e.message}")
        exitProcess(1)
    }
}

fun calculateContributions(couple: Couple): Map<YearMonth, Map<String, Any>> {
    val result = mutableMapOf<YearMonth, MutableMap<String, Any>>()
    val allMonths = (couple.person1.earnings + couple.person2.earnings + couple.person1.costs + couple.person2.costs)
        .map { YearMonth.from(it.date) }
        .distinct()
        .sorted()

    for (month in allMonths) {

	val person1Earnings = sumByMonth(couple.person1.earnings, month)
	val person2Earnings = sumByMonth(couple.person2.earnings, month)
        val totalEarnings = person1Earnings + person2Earnings
	
	val person1Costs = sumByMonth(couple.person1.costs, month)
	val person2Costs = sumByMonth(couple.person2.costs, month)
        val totalCosts = person1Costs + person2Costs

        // Calculate contribution percentages
        val contributionsPercentuals = ContributionsPercentuals.calculate(person1Earnings, person2Earnings)
        val person1Percentage = contributionsPercentuals.person1Percentage / 100.0
        val person2Percentage = contributionsPercentuals.person2Percentage / 100.0

        // Determine contributions
        val person1ContributionShouldBe = totalCosts * person1Percentage
        val person2ContributionShouldBe = totalCosts * person2Percentage

        val person1ActualContribution = person1Costs
        val person2ActualContribution = person2Costs

        // Calculate credits/debts
        val person1Credit = person1ActualContribution - person1ContributionShouldBe + couple.person1.cumulativeCredit
        val person2Credit = person2ActualContribution - person2ContributionShouldBe + couple.person2.cumulativeCredit

        // Update cumulative credits
        couple.person1.cumulativeCredit = person1Credit
        couple.person2.cumulativeCredit = person2Credit

        // Store monthly credit/debt
        couple.person1.monthlyCredit[month] = person1Credit
        couple.person2.monthlyCredit[month] = person2Credit

        // Verify credits sum to zero
        if (kotlin.math.abs(person1Credit + person2Credit) > 0.01) {
            throw Exception("Credit and debt do not balance for month $month.")
        }

        result[month] = mutableMapOf(
            "person1Earnings" to person1Earnings,
            "person2Earnings" to person2Earnings,
            "person1Costs" to person1Costs,
            "person2Costs" to person2Costs,
            "person1Percentage" to contributionsPercentuals.person1Percentage,
            "person2Percentage" to contributionsPercentuals.person2Percentage,
            "person1ContributionShouldBe" to person1ContributionShouldBe,
            "person2ContributionShouldBe" to person2ContributionShouldBe,
            "person1Credit" to person1Credit,
            "person2Credit" to person2Credit,
            "totalEarnings" to totalEarnings,
            "totalCosts" to totalCosts
        )
    }

    return result
}

fun sumByMonth(entries: List<WithDateAndAmount>, month: YearMonth): Double {
    return entries.filter {
        YearMonth.from(it.date) == month
    }.sumOf { it.amount }
}

fun printContributions(contributions: Map<YearMonth, Map<String, Any>>, couple: Couple) {
    val formatter = DateTimeFormatter.ofPattern("yyyy/MM")
    contributions.forEach { (month, data) ->
        println("Month: ${month.format(formatter)}")
        println("${couple.person1.name} Earnings: ${data["person1Earnings"]}")
        println("${couple.person2.name} Earnings: ${data["person2Earnings"]}")
        println("${couple.person1.name} Costs: ${data["person1Costs"]}")
        println("${couple.person2.name} Costs: ${data["person2Costs"]}")
        println("${couple.person1.name} Percentage: ${data["person1Percentage"]}%")
        println("${couple.person2.name} Percentage: ${data["person2Percentage"]}%")
        println("${couple.person1.name} Contribution Should Be: ${data["person1ContributionShouldBe"]}")
        println("${couple.person2.name} Contribution Should Be: ${data["person2ContributionShouldBe"]}")
        println("${couple.person1.name} Credit/Debt: ${data["person1Credit"]}")
        println("${couple.person2.name} Credit/Debt: ${data["person2Credit"]}")
        println("---------------------------------------------")
    }
}

fun generatePlot(contributions: Map<YearMonth, Map<String, Any>>, couple: Couple) {
    val months = contributions.keys.map {
        val localDate = it.atDay(1)
        Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
    }
    val totalEarnings = contributions.values.map { it["totalEarnings"] as Double }
    val totalCosts = contributions.values.map { it["totalCosts"] as Double }
    val person1Credit = contributions.values.map { it["person1Credit"] as Double }
    val person2Credit = contributions.values.map { it["person2Credit"] as Double }

    val chart = XYChartBuilder()
        .width(800)
        .height(600)
        .title("Financial Overview")
        .xAxisTitle("Month")
        .yAxisTitle("Amount")
        .build()

    // Customize Chart
    chart.styler.defaultSeriesRenderStyle = XYSeries.XYSeriesRenderStyle.Line
    chart.styler.isChartTitleVisible = true
    chart.styler.legendPosition = Styler.LegendPosition.InsideNW
    chart.styler.datePattern = "yyyy-MM" // Format the date display on the X-axis
    chart.styler.xAxisLabelRotation = 45
    chart.styler.isPlotGridLinesVisible = true

    // Add series
    chart.addSeries("Total Earnings", months, totalEarnings).lineColor = Color.GREEN
    chart.addSeries("Total Costs", months, totalCosts).lineColor = Color.RED
    chart.addSeries("${couple.person1.name} Credit", months, person1Credit).lineColor = Color.BLUE
    chart.addSeries("${couple.person2.name} Credit", months, person2Credit).lineColor = Color.ORANGE

    // Save chart
    val outputPath = Paths.get("outputs", "financial_plot.png").toString()
    BitmapEncoder.saveBitmap(chart, outputPath, BitmapEncoder.BitmapFormat.PNG)
    println("Plot saved to $outputPath")
}

