package com.leomala.footballdynasty.domain.manager

/**
 * Exact manager-name validation from `ActivityEscolhaTimes.i(String)` in the official legacy
 * corpus.
 *
 * The legacy code does not trim or normalize the input. Kotlin `String.length` preserves the same
 * UTF-16 code-unit length semantics used by Java `String.length()`.
 */
enum class LegacyManagerNameValidationResult {
    EMPTY,
    TOO_SHORT,
    TOO_LONG,
    ACCEPTED,
}

object LegacyManagerNameValidationRule {
    const val minimumLength: Int = 2
    const val maximumLength: Int = 35

    fun evaluate(name: String?): LegacyManagerNameValidationResult = when {
        name == null || name.isEmpty() -> LegacyManagerNameValidationResult.EMPTY
        name.length < minimumLength -> LegacyManagerNameValidationResult.TOO_SHORT
        name.length > maximumLength -> LegacyManagerNameValidationResult.TOO_LONG
        else -> LegacyManagerNameValidationResult.ACCEPTED
    }
}
