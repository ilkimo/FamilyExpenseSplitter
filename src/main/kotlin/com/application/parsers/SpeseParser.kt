package com.application.parsers

import com.application.parsers.Parser
import com.application.model.Cost
import com.application.model.Person
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class SpeseParser(
    private val filePath: String,
    private val persons: MutableMap<String, Person>
) : Parser() {

    override fun parse() {
        val file = File(filePath)
        if (!file.exists()) throw Exception("Expenses file not found at $filePath")

        val lines = file.readLines()
        if (lines.size <= 1) throw Exception("Expenses file is empty or invalid.")

        val dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")

        lines.drop(1).forEachIndexed { index, line ->
            val tokens = line.split(",")
            if (tokens.size != 5) throw Exception("Invalid format in spese.csv at line ${index + 2}")

            val date = LocalDate.parse(tokens[0], dateFormatter)
            val amount = tokens[1].toDoubleOrNull() ?: throw Exception("Invalid amount at line ${index + 2}")
            val currency = tokens[2]
            val payerName = tokens[3]
            val description = tokens[4]

            val person = persons.getOrPut(payerName) { Person(payerName) }
            person.costs.add(Cost(date, amount, currency, payerName, description))
        }
    }
}

