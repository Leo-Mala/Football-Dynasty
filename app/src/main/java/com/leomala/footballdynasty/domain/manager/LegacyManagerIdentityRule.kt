package com.leomala.footballdynasty.domain.manager

/** Ordered identity projection of one legacy `best.f0` manager in world `best.b.F0()`. */
data class LegacyManagerIdentityRef(
    val sourceOrdinal: Int,
    val legacyManagerId: Int,
)

data class LegacyManagerIdAllocation(
    val previousCounter: Int,
    val managerId: Int,
    val counterAfter: Int,
)

/**
 * Identity behavior proven by `best.f0(String)`, `best.b.O()`, `best.c0.G1/y0()` and `best.b.b1()`.
 *
 * Manager ids are allocated from a world counter, clubs store `-1` for no manager, and lazy club
 * resolution scans the ordered manager ArrayList and returns the first matching id. The latter is
 * intentionally not converted into a uniqueness assumption.
 */
object LegacyManagerIdentityRule {
    fun allocate(previousCounter: Int): LegacyManagerIdAllocation {
        val next = previousCounter + 1
        return LegacyManagerIdAllocation(previousCounter, next, next)
    }

    fun clubStoredManagerId(managerId: Int?): Int = managerId ?: -1

    fun resolveFirstOrdinal(
        managersInWorldOrder: List<LegacyManagerIdentityRef>,
        storedManagerId: Int,
    ): Int? = managersInWorldOrder.firstOrNull { it.legacyManagerId == storedManagerId }?.sourceOrdinal
}
