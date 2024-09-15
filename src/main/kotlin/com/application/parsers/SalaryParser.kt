package com.application.parsers

import com.application.parsers.Parser
import com.application.model.Earning
import com.application.model.Person
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class SalaryParser(
    private val filePath: String,
    private val persons: MutableMap<String, Person>
) : Parser() {

    override fun parse() {
        val file = File(filePath)
        if (!file.exists()) throw Exception("Salary file not found at $filePath")

        val lines = file.readLines()
        if (lines.size <= 1) throw Exception("Salary file is empty or invalid.")

        val dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")

        lines.drop(1).forEachIndexed { index, line ->
            val tokens = line.split(",")
            if (tokens.size != 3) throw Exception("Invalid format in salari.csv at line ${index + 2}")

            val date = LocalDate.parse(tokens[0], dateFormatter)
            val name = tokens[1]
            val amount = tokens[2].toDoubleOrNull() ?: throw Exception("Invalid amount at line ${index + 2}")

            val person = persons.getOrPut(name) { Person(name) }
            person.earnings.add(Earning(date, amount))
        }
    }
}
