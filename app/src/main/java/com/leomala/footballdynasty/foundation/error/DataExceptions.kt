package com.leomala.footballdynasty.foundation.error

open class FootballDynastyDataException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

class LegacyFormatException(
    message: String,
    cause: Throwable? = null,
) : FootballDynastyDataException(message, cause)

class IntegrityMismatchException(
    message: String,
) : FootballDynastyDataException(message)

class ImportVersionException(
    message: String,
) : FootballDynastyDataException(message)

class UnsupportedLegacySaveException(
    message: String,
) : FootballDynastyDataException(message)

class DuplicateStableIdentityException(
    message: String,
) : FootballDynastyDataException(message)
