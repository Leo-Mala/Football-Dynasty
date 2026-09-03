# Phase 13 — final finance/club/stadium audit

Status: **FECHADA / ESTÁVEL**

Official corpus: `Brasfoot.apk_Decompiler.com.zip`  
Corpus SHA-256: `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`  
Certified implementation baseline before this closure record: `18c422f4be3df8802ca09163a5f1e113a6970a2a`.

This audit closes only Phase 13. It does not start or authorize Phase 14 work.

## Aggregate reachable-scope audit

### 1. Reachable revenues and expenses — IMPLEMENTED_AND_TESTED

`legacy financial callers (c0.B/c0.D, transfer/friendly/borrowing/competition paths)` -> exact `best.m.a(int,int)` / `best.m.d(int,int)` category routing -> legacy branch eligibility -> cash plus period-ledger mutation -> `CareerManagerRuntimeStore.commitFinanceState` / dedicated persisted stores -> domain tests plus Room integration/reopen tests.

Covered reachable material paths include ticket income, player-sale income, competition prizes, sponsor income, player-purchase expense, salary expense, borrowing charge, stadium expense, fines/miscellaneous routing where characterized, paid friendlies, borrowing and repayment. Unknown income categories remain ignored and unknown expense categories retain the legacy miscellaneous fallback.

### 2. Salaries and periodicity — IMPLEMENTED_AND_TESTED

`legacy calendar "ds" event` -> characterized day-of-month-two versus Sunday schedule + participating-calendar-month eligibility -> sum senior and youth commercial salary codes -> debit cash even below zero + long-valued salary ledger accumulator -> atomic persisted finance commit -> `CareerSalaryCalendarStoreTest`, including reopen, schedule branches and ineligible-month no-op.

No synthetic payroll, clamp, grace period or modernized salary cadence is introduced.

### 3. Cash / ledger / borrowing — IMPLEMENTED_AND_TESTED

`ActivityFinancas / best.m / best.c0` -> exact income/expense buckets, totals, borrow/repay rules and monthly borrowing charge -> raw division/eligibility conditions -> cash/debt/ledger mutation -> persisted manager finance state -> pure rule tests and `CareerFinanceBorrowingStoreTest`, including source ordering, negative cash, repayment miscellaneous routing and reopen.

Borrowed principal and precomputed monthly charge are intentionally separate from period income/expense accumulators, matching `best.m`.

### 4. Match / season financial effects — IMPLEMENTED_AND_TESTED

`legacy match construction + ticket path` -> persisted O()/manager/H/parent-a0 inputs and exact RNG order -> ticket cash/ledger mutation before match simulation -> atomic gameplay + finance + RNG persistence -> ticket runtime/integration tests, including `CareerMatchTicketRngOrderTest`.

`best.b.d() -> best.b.s() -> c0.p()` new-year path -> exact payroll-derived direct cash branch then fixed division sponsor credit -> cash and sponsor-ledger mutations under the recovered conditions -> `CareerSponsorPaymentStore` -> reopen coverage.

Resolved competition-winner prize callers -> exact competition/stage rule and legacy active/Q0 eligibility -> prize cash/ledger mutation -> `CareerCompetitionPrizeStore` -> reopen/fail-closed tests.

### 5. Stadium — IMPLEMENTED_AND_TESTED

`ActivityEstadio / legacy stadium rules` -> exact initial-sector/expansion/construction calculations and conditions -> stadium expense + construction state -> persisted stadium runtime/construction ownership -> completion sweep applies four capacity additions atomically and removes completed work -> stadium rule/store tests and Room reopen coverage.

Room V10 stores `ownerClubId` for newly created construction records. Migration V9->V10 is additive and leaves historical rows without provable ownership as `NULL`; completion then fails closed before mutation/deletion. There is no invented ownership backfill and no destructive migration.

### 6. Other reachable club-administration finance operations — IMPLEMENTED_AND_TESTED

The characterized paid-friendly and borrowing/repayment/monthly-charge administrative operations have production persisted stores and integration tests. Transfer financial effects remain tied to the Phase 12 transactional transfer runtime rather than duplicated as a Phase 13 subsystem. No additional reachable Phase 13 club-finance operation was proven missing in the aggregate audit.

## `best.m.z()` boundary

`best.m.z()` is reconstructed as `LegacyFinanceLedgerRule.resetPeriod` and characterized by tests: it clears period income/expense accumulators while preserving borrowed principal and monthly borrowing charge.

The final Phase 13 reachability audit found no proven legacy caller/call path that authorizes wiring this helper into an executable modern career lifecycle. Under `AGENTS.md`, method existence alone is not permission to create gameplay behavior. Therefore the method remains a characterized pure compatibility boundary and is classified **UNREACHABLE IN THE PROVEN PHASE-13 RUNTIME / NO EXECUTABLE CALLER PROVEN** rather than `REACHABLE_NOT_IMPLEMENTED`.

If a later corpus-wide Phase 15 audit discovers a concrete Java+SMALI caller, that new evidence must reopen the classification and wire the exact proven trigger. Phase 13 does not invent a periodic reset event now.

## Final classification

- reachable revenues/despesas: `IMPLEMENTED_AND_TESTED`;
- salaries/periodicity: `IMPLEMENTED_AND_TESTED`;
- cash/ledger/borrowing: `IMPLEMENTED_AND_TESTED`;
- match/season financial effects: `IMPLEMENTED_AND_TESTED`;
- stadium: `IMPLEMENTED_AND_TESTED`;
- other reachable club administrative finance operations: `IMPLEMENTED_AND_TESTED`;
- `best.m.z()` helper: `UNREACHABLE/PRESENTATION_ONLY` for executable Phase-13 scope because no caller is proven; pure behavior remains characterized.

No `IMPLEMENTED_NEEDS_FIX` or `REACHABLE_NOT_IMPLEMENTED` item remains in the proven Phase 13 scope.

## Persistence / schema

Current Room schema: V10.  
Latest Phase 13 migration: additive/fail-closed V9->V10 ownership field for stadium construction.  
`fallbackToDestructiveMigration`: forbidden and not introduced.  
Invented backfill: none.

## Closure gate

Phase 13 is considered closed/stable only while Phase 7 Validation, Phase 8 Validation and Phase 8 Final Certification are SUCCESS on the exact closure-record HEAD. Any material later Phase-13 runtime change invalidates this closure and requires exact-head recertification.
