package com.leomala.footballdynasty.domain.career

/**
 * Thin executable projection of annual legacy `best.a.q()` (`cw`).
 *
 * The official executable contains exactly one substantive instruction in this method:
 * `core.a.b.O0().V()`. The tournament bootstrap itself remains an independently-audited boundary;
 * this rule freezes only the reachable dispatch contract and intentionally owns no RNG or state.
 */
object LegacyAnnualTournamentBootstrapRoutingRules {
    fun dispatch(runTournamentBootstrap: () -> Unit) {
        runTournamentBootstrap()
    }
}
