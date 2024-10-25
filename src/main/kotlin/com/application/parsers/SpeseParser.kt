package com.application.parsers

import com.application.model.Cost
import com.application.model.Person
import com.application.model.Installment
import java.time.YearMonth
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class SpeseParser(
    private val filePath: String,
    private val persons: MutableMap<String, Person>,
) : Parser() {
    override fun parse() {
        val file = File(filePath)
        if (!file.exists()) throw Exception("Expenses file not found at $filePath")

        val lines = file.readLines()
        if (lines.size <= 1) throw Exception("Expenses file is empty or invalid.")

        val dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")

        lines.drop(1).forEachIndexed { index, line ->
            val tokens = line.split(",")
            if (tokens.size != 5 && tokens.size != 7)
                throw Exception("Invalid format in spese.csv at line ${index + 2}, tokens.size=${tokens.size}")

            val date = LocalDate.parse(tokens[0], dateFormatter)
            val amount = tokens[1].toDoubleOrNull()
                ?: throw Exception("Invalid amount at line ${index + 2}")
            val currency = tokens[2]
            val payerName = tokens[3]
            val description = tokens[4]

            val isPianoRecuperoCrediti: Boolean
            val numberOfMonths: Int?

            if (tokens.size == 7) {
                val label = tokens[5]
                if (label != "piano recupero crediti")
                    throw Exception("Invalid label at line ${index + 2}")
                numberOfMonths = tokens[6].toIntOrNull()
                    ?: throw Exception("Invalid number of months at line ${index + 2}")
                isPianoRecuperoCrediti = true
            } else {
                isPianoRecuperoCrediti = false
                numberOfMonths = null
            }

            val person = persons.getOrPut(payerName) { Person(payerName) }

            if (isPianoRecuperoCrediti && numberOfMonths != null) {
                // Create installments
                val monthlyAmount = amount / numberOfMonths
                val startMonth = YearMonth.from(date)
                for (i in 0 until numberOfMonths) {
                    val installmentMonth = startMonth.plusMonths(i.toLong())
                    val installment = Installment(installmentMonth, monthlyAmount)
                    person.installments.add(installment)
                }
            } else {
                // Regular cost
                person.costs.add(
                    Cost(
                        date,
                        amount,
                        currency,
                        payerName,
                        description,
                        isPianoRecuperoCrediti,
                        numberOfMonths
                    )
                )
            }
        }
    }
}
