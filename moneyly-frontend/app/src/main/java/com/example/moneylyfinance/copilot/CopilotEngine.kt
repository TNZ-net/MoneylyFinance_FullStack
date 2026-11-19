package com.example.moneylyfinance.copilot

import com.example.moneylyfinance.copilot.Intent.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

data class Insight(val title: String, val detail: String, val warn: Boolean, val score: Int)

object CopilotEngine {

    private val model = IntentModel()

    val SUGGESTIONS = listOf(
        "How much did I spend this month?", "What do I spend the most on?", "Compare with last month",
        "Any unusual spending?", "Show my subscriptions", "Will I stay within budget?"
    )
    const val GREETING = "Hi! I analyse your expenses on this phone. Ask me something or tap a suggestion."
    private const val HELP = "I can help with: total spending, spending on a category, top categories, " +
        "comparing with last month/week, unusual expenses, recurring payments, and budget forecast."

    private val localFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)   // device time zone

    fun today(): Int = parseDay(localFmt.format(Date()))!!

    private fun rm(x: Double) = String.format(Locale.US, "RM %.2f", x)
    private fun cap(s: String) = s.replaceFirstChar { it.uppercase() }

    private class Period(val from: Int, val to: Int, val label: String)

    private fun period(q: String, today: Int) = when {
        "yesterday" in q -> Period(today - 1, today - 1, "yesterday")
        "today" in q -> Period(today, today, "today")
        "last month" in q -> Period(monthStart(today, -1), monthStart(today, 0) - 1, "last month")
        "week" in q || "7 day" in q -> Period(today - 6, today, "in the last 7 days")
        "30 day" in q -> Period(today - 29, today, "in the last 30 days")
        else -> Period(monthStart(today, 0), today, "this month")
    }

    fun answer(question: String, tx: List<Tx>, budget: Double?, today: Int): String {
        val q = question.lowercase()
        // Ambiguous wording that still mentions spending defaults to a plain total.
        val intent = model.predict(q) ?: if (Regex("spen[dt]|expense|cost").containsMatchIn(q)) TOTAL_SPEND else return HELP
        if (tx.isEmpty()) return "You have no expenses yet. Add a few and I can analyse them."

        val categories = tx.map { it.category }.distinct()
        val cat = categories.firstOrNull { Regex("\\b${Regex.escape(it.lowercase())}s?\\b").containsMatchIn(q) }
        val p = period(q, today)

        return when (if (intent == TOTAL_SPEND && cat != null) CATEGORY_SPEND else intent) {

            TOTAL_SPEND -> {
                val n = tx.count { it.day in p.from..p.to }
                "You spent ${rm(total(tx, p.from, p.to))} ${p.label} across $n expense${if (n == 1) "" else "s"}."
            }

            CATEGORY_SPEND -> {
                if (cat == null) return "Which category? You have: ${categories.joinToString()}."
                val mine = tx.filter { it.day in p.from..p.to && it.category == cat }
                if (mine.isEmpty()) return "Nothing spent on $cat ${p.label}."
                val all = total(tx, p.from, p.to)
                val big = topK(mine, 3) { it.amount }
                "$cat ${p.label}: ${rm(mine.sumOf { it.amount })} " +
                    "(${(mine.sumOf { it.amount } / all * 100).roundToInt()}% of all spending, ${mine.size} expenses).\n" +
                    "Biggest: " + big.joinToString { "${it.desc} ${rm(it.amount)} (${dayStr(it.day)})" }
            }

            TOP_CATEGORIES -> {
                val totals = byCategory(tx, p.from, p.to)
                if (totals.isEmpty()) return "No spending ${p.label}."
                val sum = totals.values.sum()
                "Top categories ${p.label}:\n" + topK(totals.entries, 3) { it.value }.withIndex()
                    .joinToString("\n") { (i, e) -> "${i + 1}. ${e.key} ${rm(e.value)} (${(e.value / sum * 100).roundToInt()}%)" }
            }

            COMPARE_PERIODS -> {
                val week = "week" in q
                val ps = monthStart(today, -1)
                val cs = monthStart(today, 0)
                val cur = if (week) today - 6..today else cs..today
                val prv = if (week) today - 13..today - 7 else ps..min(ps + (today - cs), cs - 1)
                val (nowLabel, thenLabel) =
                    if (week) "the last 7 days" to "the 7 days before" else "this month so far" to "the same days last month"
                val a = total(tx, cur.first, cur.last, cat)
                val b = total(tx, prv.first, prv.last, cat)
                val what = if (cat != null) "$cat spending" else "Spending"
                if (b == 0.0) return "$what $nowLabel: ${rm(a)}. There was none $thenLabel to compare with."
                val pct = ((a - b) / b * 100).roundToInt()
                var s = "$what $nowLabel: ${rm(a)} vs ${rm(b)} $thenLabel, " +
                    if (pct >= 0) "up $pct%." else "down ${-pct}%."
                if (cat == null) {
                    val ca = byCategory(tx, cur.first, cur.last)
                    val cb = byCategory(tx, prv.first, prv.last)
                    (ca.keys + cb.keys).maxByOrNull { abs((ca[it] ?: 0.0) - (cb[it] ?: 0.0)) }?.let {
                        val d = (ca[it] ?: 0.0) - (cb[it] ?: 0.0)
                        s += " Biggest change: $it (${if (d >= 0) "+" else "-"}${rm(abs(d))})."
                    }
                }
                s
            }

            ANOMALIES -> {
                val found = topK(anomalies(tx, today - 29, today), 3) { it.first.amount / it.second }
                if (found.isEmpty()) "Nothing unusual in the last 30 days. (I need 5+ expenses in a category to judge.)"
                else "Unusual expenses in the last 30 days:\n" + found.joinToString("\n") { (t, typ) ->
                    "• ${t.desc} ${rm(t.amount)} on ${dayStr(t.day)} (${t.category}, usually about ${rm(typ)})"
                }
            }

            RECURRING -> {
                val r = recurring(tx, today)
                if (r.isEmpty()) "No repeating charges found yet. (I look for 3+ similar expenses at regular intervals.)"
                else "Likely recurring:\n" + r.take(5).joinToString("\n") {
                    "• ${cap(it.name)} about ${rm(it.amount)} every ~${it.every} days (next around ${dayStr(it.next)})"
                } + "\nThat's roughly ${rm(r.sumOf { it.amount * 30 / it.every })} per month."
            }

            FORECAST_BUDGET -> forecast(tx, budget, today)

        }
    }

    private fun forecast(tx: List<Tx>, budget: Double?, today: Int): String {
        val ms = monthStart(today, 0)
        val dim = monthStart(today, 1) - ms
        val elapsed = today - ms + 1
        val spent = total(tx, ms, today)
        val projected = spent / elapsed * dim
        val s = StringBuilder("Spent ${rm(spent)} so far this month; at this pace you'll finish near ${rm(projected)}")
        s.append(if (elapsed < 5) " (early in the month, so rough).\n" else ".\n")

        if (budget != null && budget > 0) {
            val left = budget - spent
            if (left < 0) s.append("You're ${rm(-left)} over your ${rm(budget)} budget.\n")
            else {
                s.append("Budget ${rm(budget)}: ${rm(left)} left")
                if (dim - elapsed > 0) s.append(", about ${rm(left / (dim - elapsed))}/day for the remaining ${dim - elapsed} days")
                s.append(".\n")
                if (projected > budget) s.append("At this pace you'd exceed it by ${rm(projected - budget)}.\n")
            }
        } else s.append("Set a monthly budget on the dashboard and I'll track it.\n")

        val months = tx.filter { it.day < ms }.groupBy { dayStr(it.day).take(7) }.toSortedMap().values.map { m -> m.sumOf { it.amount } }
        if (months.size >= 2) s.append("From your last ${months.size} full months' trend, next month looks like ${rm(linearNext(months))}.")
        return s.toString().trim()
    }

    /** Auto-generated cards: every detector runs, the 5 highest-scoring findings are shown. */
    fun insights(tx: List<Tx>, budget: Double?, today: Int): List<Insight> {
        if (tx.isEmpty()) return listOf(Insight("Welcome", "Add a few expenses and I'll start spotting patterns.", false, 1))
        val out = mutableListOf<Insight>()
        val ms = monthStart(today, 0)
        val dim = monthStart(today, 1) - ms
        val elapsed = today - ms + 1
        val spent = total(tx, ms, today)

        if (budget != null && budget > 0) {
            val projected = spent / elapsed * dim
            out += when {
                spent > budget -> Insight("Over budget", "${rm(spent - budget)} over your ${rm(budget)} budget.", true, 100)
                elapsed >= 5 && projected > budget ->
                    Insight("Heading over budget", "At this pace: ${rm(projected)} vs a ${rm(budget)} budget.", true, 80)
                else -> Insight("On track", "${rm(budget - spent)} left of your ${rm(budget)} budget.", false, 20)
            }
        }

        // Two adjacent 30-day windows: which categories jumped?
        val now = byCategory(tx, today - 29, today)
        val before = byCategory(tx, today - 59, today - 30)
        for ((c, v) in now) {
            val p = before[c] ?: 0.0
            if (p >= 20 && v - p >= 20 && v > p * 1.3)
                out += Insight("$c is up ${((v / p - 1) * 100).roundToInt()}%", "${rm(v)} in the last 30 days vs ${rm(p)} the 30 days before.", true, 60)
        }

        anomalies(tx, today - 29, today).maxByOrNull { it.first.amount / it.second }?.let { (t, typ) ->
            out += Insight("Unusual ${t.category} expense", "${t.desc} ${rm(t.amount)} on ${dayStr(t.day)}, usually about ${rm(typ)}.", true, 70)
        }

        tx.filter { it.day in today - 29..today }.groupBy { Triple(norm(it.desc), it.amount, it.day) }
            .filter { it.value.size > 1 }.keys.firstOrNull()?.let { (n, a, d) ->
                out += Insight("Possible duplicate", "\"$n\" ${rm(a)} was logged twice on ${dayStr(d)}.", true, 65)
            }

        val rec = recurring(tx, today)
        if (rec.isNotEmpty()) {
            val soon = rec.minBy { it.next }
            out += Insight("${rec.size} recurring charge${if (rec.size == 1) "" else "s"}",
                "About ${rm(rec.sumOf { it.amount * 30 / it.every })}/month. Next: ${cap(soon.name)} around ${dayStr(soon.next)}.", false, 40)
        }

        if (spent > 0) byCategory(tx, ms, today).maxByOrNull { it.value }?.let {
            out += Insight("${it.key} leads this month", "${(it.value / spent * 100).roundToInt()}% of spending (${rm(it.value)}).", false, 30)
        }

        return topK(out, 5) { it.score.toDouble() }
    }
}
