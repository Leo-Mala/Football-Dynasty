package com.leomala.footballdynasty.foundation.error

open class CareerIntegrityException(message: String) : IllegalStateException(message)

class InvalidCareerStateException(message: String) : CareerIntegrityException(message)

class SeasonBoundaryRequiredException(message: String) : CareerIntegrityException(message)

class UnsupportedLegacyBehaviorException(message: String) : UnsupportedOperationException(message)

class SimulationConflictException(message: String) : IllegalStateException(message)
