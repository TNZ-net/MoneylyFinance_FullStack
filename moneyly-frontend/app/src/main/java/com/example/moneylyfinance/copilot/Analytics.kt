package com.example.moneylyfinance.copilot

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.PriorityQueue
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

data class Tx(val day: Int, val amount: Double, val category: String, val desc: String)

data class Recurring(val name: String, val amount: Double, val every: Int, val next: Int)
private val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
    timeZone = TimeZone.getTimeZone("UTC"); isLenient = false
}

fun parseDay(s: String): Int? =
    runCatching { Math.floorDiv(fmt.parse(s)!!.time, 86_400_000L).toInt() }.getOrNull()

fun dayStr(d: Int): String = fmt.format(Date(d * 86_400_000L))

fun monthStart(day: Int, offset: Int): Int {
    val (y, m) = dayStr(day).split("-")
    val i = y.toInt() * 12 + (m.toInt() - 1) + offset
    return parseDay("%04d-%02d-01".format(i / 12, i % 12 + 1))!!
}

// ---------- algorithms ----------
fun total(tx: List<Tx>, from: Int, to: Int, cat: String? = null) =
    tx.filter { it.day in from..to && (cat == null || it.category.equals(cat, true)) }.sumOf { it.amount }

/** Hash-map group-by: category -> total. O(n). */
fun byCategory(tx: List<Tx>, from: Int, to: Int): Map<String, Double> =
    tx.filter { it.day in from..to }.groupingBy { it.category }.fold(0.0) { a, t -> a + t.amount }

/** Top-K with a min-heap of size k. O(n log k). */
fun <T> topK(items: Collection<T>, k: Int, score: (T) -> Double): List<T> {
    val heap = PriorityQueue<T>(compareBy(score))
    for (x in items) { heap.add(x); if (heap.size > k) heap.poll() }
    return heap.sortedByDescending(score)
}

fun median(v: List<Double>): Double {
    if (v.isEmpty()) return 0.0
    val s = v.sorted()
    return if (s.size % 2 == 1) s[s.size / 2] else (s[s.size / 2 - 1] + s[s.size / 2]) / 2
}

/**
 * Outliers per category using the modified z-score (median / MAD), which, unlike mean/stddev,
 * is not distorted by the outliers themselves. Returns (expense, typical amount for its category).
 */
fun anomalies(all: List<Tx>, from: Int, to: Int): List<Pair<Tx, Double>> =
    all.groupBy { it.category }.filter { it.value.size >= 5 }.flatMap { (_, l) ->
        val m = median(l.map { it.amount })
        val mad = median(l.map { abs(it.amount - m) })
        l.filter {
            it.day in from..to && it.amount > 1.5 * m &&
                (if (mad > 0) 0.6745 * (it.amount - m) / mad > 3.5 else it.amount > 2 * m)
        }.map { it to m }
    }

fun norm(desc: String) = desc.lowercase().replace(Regex("[^a-z ]"), " ").trim().replace(Regex("\\s+"), " ")

/** Same normalised name, 3+ times, steady gaps (median +-20%) and steady amount, still active. */
fun recurring(all: List<Tx>, today: Int): List<Recurring> =
    all.groupBy { norm(it.desc) }.mapNotNull { (name, l) ->
        if (name.isEmpty() || l.size < 3) return@mapNotNull null
        val s = l.sortedBy { it.day }
        val gaps = s.zipWithNext { a, b -> b.day - a.day }
        val g = median(gaps.map { it.toDouble() })
        val amt = median(l.map { it.amount })
        val steady = gaps.all { abs(it - g) <= max(2.0, g * 0.2) } && l.all { abs(it.amount - amt) <= amt * 0.2 }
        if (g >= 5 && steady && today - s.last().day <= 2 * g + 3)
            Recurring(name, amt, g.roundToInt(), s.last().day + g.roundToInt())
        else null
    }.sortedByDescending { it.amount }

/** Least-squares line through the points, extrapolated one step. */
fun linearNext(y: List<Double>): Double {
    val n = y.size
    val sx = (0 until n).sum().toDouble()
    val sxx = (0 until n).sumOf { it * it }.toDouble()
    val sy = y.sum()
    val sxy = y.indices.sumOf { it * y[it] }
    val slope = (n * sxy - sx * sy) / (n * sxx - sx * sx)
    return max(0.0, slope * n + (sy - slope * sx) / n)
}
