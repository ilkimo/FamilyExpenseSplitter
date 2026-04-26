package com.application.model

import java.time.LocalDate
import java.time.YearMonth
import java.util.ArrayDeque

class SharedBucket {
    val deposits: MutableList<SharedDeposit> = mutableListOf()
    val expenses: MutableList<SharedExpense> = mutableListOf()

    fun addDeposit(d: SharedDeposit) {
        deposits.add(d)
    }

    fun addExpense(e: SharedExpense) {
        expenses.add(e)
    }

    var underrunTotal: Double = 0.0
        private set
    var underrunCount: Int = 0
        private set

    fun allocate(persons: MutableMap<String, Person>) {
        data class Chunk(var remaining: Double, val depositor: String)
        data class Event(val date: LocalDate, val isDeposit: Boolean, val index: Int)

        val events = mutableListOf<Event>()
        deposits.forEachIndexed { i, d -> events.add(Event(d.date, true, i)) }
        expenses.forEachIndexed { i, e -> events.add(Event(e.date, false, i)) }
        events.sortWith(compareBy({ it.date }, { !it.isDeposit }))

        val queue: ArrayDeque<Chunk> = ArrayDeque()

        for (event in events) {
            if (event.isDeposit) {
                val d = deposits[event.index]
                queue.addLast(Chunk(d.amount, d.depositor))
                continue
            }

            val e = expenses[event.index]
            val shares: MutableMap<String, Double> = linkedMapOf()
            var remaining = e.amount

            while (remaining > 0.0001) {
                val head = queue.peekFirst()
                if (head == null) {
                    val personNames = persons.keys.toList()
                    val per = remaining / personNames.size
                    for (n in personNames) shares.merge(n, per) { a, b -> a + b }
                    println(
                        "WARN shared bucket '${e.bucket}' underrun on ${e.date} for " +
                            "'${e.description}': ${"%.2f".format(remaining)} ${e.currency} " +
                            "uncovered, falling back to ${personNames.size}-way split"
                    )
                    underrunTotal += remaining
                    underrunCount++
                    remaining = 0.0
                    break
                }
                val take = if (head.remaining <= remaining) head.remaining else remaining
                shares.merge(head.depositor, take) { a, b -> a + b }
                head.remaining -= take
                remaining -= take
                if (head.remaining <= 0.0001) queue.pollFirst()
            }

            for ((depositor, share) in shares) {
                val person = persons[depositor]
                    ?: throw Exception(
                        "Shared bucket deposit attributed to unknown person '$depositor' " +
                            "(only persons from salari.csv are allowed as depositors)"
                    )
                if (e.isPianoRecuperoCrediti && e.numberOfMonths != null) {
                    val monthly = share / e.numberOfMonths
                    val start = YearMonth.from(e.date)
                    for (i in 0 until e.numberOfMonths) {
                        person.installments.add(Installment(start.plusMonths(i.toLong()), monthly))
                    }
                } else {
                    person.costs.add(
                        Cost(
                            e.date,
                            share,
                            e.currency,
                            depositor,
                            "[${e.bucket}] ${e.description}",
                            false,
                            null,
                        )
                    )
                }
            }
        }

        val leftover = queue.sumOf { it.remaining }
        if (leftover > 0.01) {
            println(
                "Note: shared bucket has ${"%.2f".format(leftover)} unspent across " +
                    "${queue.size} deposit chunk(s) — not attributed as costs."
            )
        }
        if (underrunCount > 0) {
            println(
                "Note: ${underrunCount} shared expense(s) totaling ${"%.2f".format(underrunTotal)} " +
                    "EUR were uncovered by deposits and split equally between persons."
            )
        }
    }
}
