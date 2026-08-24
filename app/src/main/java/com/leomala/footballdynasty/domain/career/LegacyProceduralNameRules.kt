package com.leomala.footballdynasty.domain.career

import com.leomala.footballdynasty.foundation.random.RandomSource

/** Exact list-selection behavior of legacy `best.u.c(int)` once its name assets are loaded. */
object LegacyProceduralNameRules {
    fun generate(
        random: RandomSource,
        names: List<String>,
        surnames: List<String>,
    ): String? {
        if (names.isEmpty()) return null
        require(names.size > 1) { "Legacy algorithm assumes at least two names" }

        var firstIndex = random.nextInt(names.size)
        if (names.size >= 1000 && random.nextInt(2) == 0) {
            firstIndex = random.nextInt(500)
        }
        if (firstIndex == 0) firstIndex = 1
        val first = names[firstIndex]

        return when (wordCount(first)) {
            1 -> {
                if (surnames.size <= 2) return first
                var secondIndex = random.nextInt(surnames.size)
                if (secondIndex == 0) secondIndex = 1
                val second = surnames[secondIndex]
                if (first == second) first else "$first $second"
            }

            2 -> {
                val combine = random.nextInt(2) == 0
                if (first.length > 12 || !combine || names.size <= 2) return first
                var secondIndex = random.nextInt(names.size)
                if (secondIndex == 0) secondIndex = 1
                val second = names[secondIndex]
                if (first == second || second.length > 6) first else "$first $second"
            }

            else -> first
        }
    }

    private fun wordCount(value: String): Int =
        value.trim().takeIf { it.isNotEmpty() }?.split(Regex("\\s+")).orEmpty().size
}
