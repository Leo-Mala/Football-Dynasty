# Phase 4R — superseded baseline vs Brasfoot 2026/27

| Area | Superseded baseline | Official Phase 4R baseline | Classification | Action |
|---|---|---|---|---|
| Package | `com.brasfoot.v2028` | `com.brasfoot.v2020` | CHANGED_BREAKING | update documentation/evidence; keep modern app package unchanged |
| Version | `2.7` / code `69` | `Brasfoot.202632` / code `202632` | CHANGED_BREAKING | replace baseline markers |
| SDK metadata | min 19 / target 29 / compile 23 | min 23 / target 36 / compile 36 | CHANGED_COMPATIBLE | informational; modern Android config remains independent |
| Total decompiler files | 7,982 | 13,889 | CHANGED_BREAKING | new inventory is authoritative |
| Java files | 575 | 5,395 | CHANGED_BREAKING | rebuild class inventory |
| SMALI files | 575 | 5,489 | CHANGED_BREAKING | use SMALI for suspicious/truncated Java |
| `.ban` files | 2,689 | 1,687 | CHANGED_BREAKING | replace corpus expectations/fingerprints |
| `.ban` team/player serialization | `e.t` / `e.g`, UID 16 | `e.t` / `e.g`, UID 16 | UNCHANGED | compatibility shells reusable and corpus-tested |
| `.ban` players | 66,003 | 35,432 | CHANGED_BREAKING | new counts are authoritative |
| `.ban` juniors | 13,098 | 4,164 | CHANGED_BREAKING | new counts are authoritative |
| Active team assets | `resources/assets/teams` | `resources/assets/teams2026` plus `packs` | CHANGED_BREAKING | rebaseline paths/import source |
| Save metadata | `*.ai21` | `*.a26` | CHANGED_BREAKING | probe/reader docs updated |
| Career container | `*.s21` / `*.s121` | `*.s26` | CHANGED_BREAKING | do not claim old save compatibility |
| Career serializer | Kryo/legacy evidence | Kryo, registration not required | CHANGED_COMPATIBLE | retain isolated compatibility boundary; real fixture still required |
| `options.bcf` | Java serialization | Java serialization | UNCHANGED | reusable |
| Modern Kotlin/Compose layers | reconstruction target | reconstruction target | UNCHANGED | reuse |
| Room V1/V2 | modern persistence | modern persistence | UNCHANGED pending evidence | preserve; no V3 justified by corpus change alone |
| Seedable/persisted modern RNG | modern testability feature | legacy equivalence not proven | CHANGED_COMPATIBLE | preserve determinism but document it as modern behavior unless legacy parity is proven |

## Consequence

The new corpus is not a drop-in factual replacement: paths, quantities, version markers and save extensions changed materially. The modern architecture, Room schema and deterministic infrastructure do not need to be discarded because those components are implementation mechanisms rather than legacy sporting facts.

Historical Phase 1/2 documents that state the old corpus is authoritative are now superseded and must be read as historical evidence only.
