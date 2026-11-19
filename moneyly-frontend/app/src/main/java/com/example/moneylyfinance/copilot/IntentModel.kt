package com.example.moneylyfinance.copilot

import kotlin.math.exp
import kotlin.math.ln

enum class Intent {
    TOTAL_SPEND, CATEGORY_SPEND, TOP_CATEGORIES, COMPARE_PERIODS, ANOMALIES, RECURRING, FORECAST_BUDGET
}

class IntentModel {

    private val prior = HashMap<Intent, Int>()
    private val words = HashMap<Intent, HashMap<String, Int>>()
    private val wordTotal = HashMap<Intent, Int>()
    private val vocab = HashSet<String>()

    init {
        for ((intent, questions) in EXAMPLES) for (q in questions) {
            prior.merge(intent, 1, Int::plus)
            for (w in tokens(q)) {
                words.getOrPut(intent) { HashMap() }.merge(w, 1, Int::plus)
                wordTotal.merge(intent, 1, Int::plus)
                vocab += w
            }
        }
    }

    /** Best intent, or null when the question has no known words or the model isn't confident. */
    fun predict(question: String): Intent? {
        val ws = tokens(question).filter { it in vocab }
        if (ws.isEmpty()) return null
        val n = prior.values.sum()
        val logp = Intent.entries.associateWith { i ->
            ln(prior.getValue(i).toDouble() / n) + ws.sumOf { w ->
                ln(((words[i]?.get(w) ?: 0) + ALPHA) / (wordTotal.getValue(i) + ALPHA * vocab.size))
            }
        }
        val top = logp.maxBy { it.value }
        val confidence = 1.0 / logp.values.sumOf { exp(it - top.value) }   // softmax of the winner
        return if (confidence >= 0.5) top.key else null
    }

    private fun tokens(s: String) =
        s.lowercase().split(Regex("[^a-z]+")).filter { it.length > 1 && it !in FILLER }
            .map { if (it.length > 3 && it.endsWith("s")) it.dropLast(1) else it }

    private companion object {
        const val ALPHA = 0.1   // add-k smoothing; small so rare keywords like "subscription" carry weight
        val FILLER = setOf("the", "my", "me", "is", "are", "am", "did", "do", "does", "what", "how", "much",
            "many", "this", "that", "with", "for", "can", "any", "show", "tell", "have", "has", "all", "of", "to", "it")
        val EXAMPLES = mapOf(
            Intent.TOTAL_SPEND to listOf(
                "how much did i spend", "total spending this month", "what are my total expenses",
                "how much have i spent today", "how much money went out this week",
                "my spending last month", "what did i spend yesterday", "sum of all expenses", "spending today", "yesterday spending"
            ),
            Intent.CATEGORY_SPEND to listOf(
                "how much did i spend on food", "spending on transport", "show me my shopping expenses",
                "what did i spend on bills", "category breakdown for food", "expenses for groceries",
                "how much on entertainment", "details of my spending in this category", "spent on transport last month"
            ),
            Intent.TOP_CATEGORIES to listOf(
                "what do i spend the most on", "top categories", "biggest expense category",
                "where does my money go", "highest spending category", "breakdown of my spending",
                "which category costs the most", "what are my largest expenses"
            ),
            Intent.COMPARE_PERIODS to listOf(
                "compare this month with last month", "am i spending more than last month",
                "spending versus last week", "how does this month compare", "did my spending go up",
                "is my spending going down", "change in spending", "spending trend", "food vs last month", "this week vs last week", "more or less than before"
            ),
            Intent.ANOMALIES to listOf(
                "any unusual spending", "anything strange in my expenses", "find anomalies",
                "suspicious expenses", "unusually large purchase", "outlier transactions",
                "weird charges", "did i overspend on something", "unusual spending", "anything unusual", "abnormal expense"
            ),
            Intent.RECURRING to listOf(
                "what subscriptions do i have", "recurring payments", "which bills repeat every month",
                "regular charges", "subscription costs", "what do i pay every month",
                "repeating expenses", "monthly commitments", "my subscriptions", "show subscriptions", "memberships"
            ),
            Intent.FORECAST_BUDGET to listOf(
                "will i stay within budget", "how much budget is left", "predict my spending",
                "forecast end of month", "am i over budget", "how much can i spend per day",
                "projected spending next month", "how am i doing against my budget", "forecast", "forecast my spending", "can i afford to spend more"
            )
        )
    }
}
