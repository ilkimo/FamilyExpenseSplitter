package com.application.parsers

import com.application.model.Cost
import com.application.model.Installment
import com.application.model.Person
import com.application.model.SharedBucket
import com.application.model.SharedDeposit
import com.application.model.SharedExpense
import java.io.File
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class SpeseParser(
    private val filePath: String,
    private val persons: MutableMap<String, Person>,
    private val sharedBucket: SharedBucket,
    private val recurringCutoff: LocalDate = LocalDate.now(),
) : Parser() {
    private var recurringGenerated: Int = 0

    override fun parse() {
        val file = File(filePath)
        if (!file.exists()) throw Exception("Expenses file not found at $filePath")

        val lines = file.readLines()
        if (lines.size <= 1) throw Exception("Expenses file is empty or invalid.")

        val dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")

        lines.drop(1).forEachIndexed { index, line ->
            val tokens = line.split(",")
            val lineNum = index + 2

            if (tokens.size == 7 && tokens[1].toDoubleOrNull() == null) {
                val date = LocalDate.parse(tokens[0], dateFormatter)
                val kind = tokens[1]
                val bucket = tokens[2]
                val amount = tokens[3].toDoubleOrNull()
                    ?: throw Exception("Invalid deposit amount at line $lineNum")
                val currency = tokens[4]
                val depositor = tokens[5]
                val description = tokens[6]

                if (depositor !in persons)
                    throw Exception(
                        "Deposit at line $lineNum from unknown depositor '$depositor' " +
                            "(must be a person known from salari.csv)"
                    )

                val deposit = SharedDeposit(date, amount, currency, depositor, bucket, kind, description)
                for (d in expandRecurring(deposit)) sharedBucket.addDeposit(d)
                return@forEachIndexed
            }

            if (tokens.size != 5 && tokens.size != 7)
                throw Exception("Invalid format in spese.csv at line $lineNum, tokens.size=${tokens.size}")

            val date = LocalDate.parse(tokens[0], dateFormatter)
            val amount = tokens[1].toDoubleOrNull()
                ?: throw Exception("Invalid amount at line $lineNum")
            val currency = tokens[2]
            val payerName = tokens[3]
            val description = tokens[4]

            val isPianoRecuperoCrediti: Boolean
            val numberOfMonths: Int?

            if (tokens.size == 7) {
                val label = tokens[5]
                if (label != "piano recupero crediti")
                    throw Exception("Invalid label at line $lineNum")
                numberOfMonths = tokens[6].toIntOrNull()
                    ?: throw Exception("Invalid number of months at line $lineNum")
                isPianoRecuperoCrediti = true
            } else {
                isPianoRecuperoCrediti = false
                numberOfMonths = null
            }

            if (payerName !in persons) {
                sharedBucket.addExpense(
                    SharedExpense(
                        date, amount, currency, payerName, description,
                        isPianoRecuperoCrediti, numberOfMonths,
                    )
                )
                return@forEachIndexed
            }

            val person = persons.getValue(payerName)

            if (isPianoRecuperoCrediti && numberOfMonths != null) {
                val monthlyAmount = amount / numberOfMonths
                val startMonth = YearMonth.from(date)
                for (i in 0 until numberOfMonths) {
                    val installmentMonth = startMonth.plusMonths(i.toLong())
                    person.installments.add(Installment(installmentMonth, monthlyAmount))
                }
            } else {
                person.costs.add(
                    Cost(
                        date, amount, currency, payerName, description,
                        isPianoRecuperoCrediti, numberOfMonths,
                    )
                )
            }
        }

        if (recurringGenerated > 0) {
            println(
                "Note: generated $recurringGenerated synthetic recurring deposit(s) through " +
                    "$recurringCutoff (monthly/weekly expansions)."
            )
        }
    }

    private fun expandRecurring(d: SharedDeposit): List<SharedDeposit> {
        val kindLower = d.kind.lowercase()
        val isMonthly = "ricorrente mensile" in kindLower
        val isWeekly = "ricorrente settimanale" in kindLower
        if (!isMonthly && !isWeekly) return listOf(d)

        val out = mutableListOf(d)
        var next = if (isMonthly) d.date.plusMonths(1) else d.date.plusWeeks(1)
        while (!next.isAfter(recurringCutoff)) {
            out.add(d.copy(date = next))
            recurringGenerated++
            next = if (isMonthly) next.plusMonths(1) else next.plusWeeks(1)
        }
        return out
    }
}
