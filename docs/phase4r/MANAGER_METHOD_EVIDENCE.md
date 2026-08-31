# Manager method evidence — official Brasfoot 2026/27 corpus

Status: **ACTIVE / AUTHORITATIVE FOR MARCO B**

This file re-anchors manager-loop method evidence to the official Phase 4R corpus:

- archive: `Brasfoot.apk_Decompiler.com.zip`;
- SHA-256: `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`;
- package: `com.brasfoot.v2020`;
- versionCode: `202632`.

Older Phase 1 documents such as `docs/reverse-engineering/SMALI_RECOVERY.md` and `ACTIVITY_MAP.md` contain names/fingerprints from the superseded pre-Phase-4R corpus (`com.brasfoot.v2028`). They remain historical evidence but MUST NOT be used as the authoritative method identity for new Marco B migrations.

## Counting rule

`instructionCount` below counts non-empty SMALI method-body lines after excluding directives, labels, comments and annotation payload. `branchCount` counts `if-*`, `goto*`, `packed-switch` and `sparse-switch` opcodes. Java-facing names are recorded separately when jadx renamed a bytecode method.

## Current manager anchors

| Role | Java/decompiler method | SMALI method | Useful instructions | Branches | Method-body SHA-256 |
|---|---|---|---:|---:|---|
| stadium host | `ActivityEstadio.onCreate(Bundle)` | `ActivityEstadio.onCreate(Landroid/os/Bundle;)V` | 259 | 11 | `29e8d3e80bb8beeb1550eab6f90aa32d0e57232e6278579de6c3482992a1f546` |
| tactics host | `DialogTatics.onCreate(Bundle)` | `DialogTatics.onCreate(Landroid/os/Bundle;)V` | 171 | 19 | `3093ad86d6eb9eb40297d83743b5b1a579aa287b1783cbd0fefaea6256737a46` |
| lineup host | `ActivityEscalacao.B()` (jadx notes bytecode name `y`) | `ActivityEscalacao.y()V` | 212 | 22 | `78da9864100e7b428af7ed02c7bc82aa4fb86e6f795513a18e3eaabaadbba54a` |
| player search proposal | `ActivityProcura.t(best.o,best.c0,int)` | `ActivityProcura.t(Lbest/o;Lbest/c0;I)I` | 136 | 14 | `1eb6f3743cafb927c0406b1836ca9817878e017816b263b07d2e8267e94cc53f` |
| club-selection validation | `ActivityEscolhaTimes.i(String)` | `ActivityEscolhaTimes.i(Ljava/lang/String;)Z` | 38 | 9 | `b3665f27d3ff260e03e5c98f30358a8ba2d94a15a098f3ceffacc862ed528da4` |
| player-info host | `DialogIgrokInfo.onCreate(Bundle)` | `DialogIgrokInfo.onCreate(Landroid/os/Bundle;)V` | 530 | 28 | `f9b2a9a256bb4c1b68bd7c007b9d9f07f06374a7b5b1c4ac763ed1ee11551a0f` |
| team proposal | `ActivityTimes.s(best.o,best.c0,int)` | `ActivityTimes.s(Lbest/o;Lbest/c0;I)I` | 133 | 14 | `858d21bf2807c8f88a4bed88237e1e306af836bda466729b3da92a754785a644` |
| career-club hub | `ActivityMainTeam.onStart()` | `ActivityMainTeam.onStart()V` | 93 | 15 | `6a4f438b08bb525830944c16126eddbb3e897bccaeaadc60fe9534076764afb2` |
| saved tactics | `ActivitySavedTatics.g()` | `ActivitySavedTatics.g()V` | 103 | 8 | `ee05c30e22ca29607386aa69c695cec3b58f172804a4e1c76f54a2e4c35aa9be` |
| club invitation acceptance | `ActivityConvite.onClickAccept(View)` | `ActivityConvite.onClickAccept(Landroid/view/View;)V` | 39 | 8 | `fd4555c41205a03ce9d6f2a593cdf2342d99102371c930798224ddff1a2cea8e` |

## Phase 11 surface correction

The official corpus class is `ActivityEscalacao`, not the historical `ActivityEscala`. It still uses layout `activity_escala`. The decompiled Java method that failed is `B()`, with jadx explicitly recording that the underlying bytecode method is `y`; the corresponding SMALI body is therefore `ActivityEscalacao.y()V`.

`DialogTatics` and `ActivitySavedTatics` retain their class names, but the current recovered saved-tactics body is `g()V`, not historical `sa()`.

## Phase 14 surface correction

The official `ActivityEscolhaTimes` validation stub is `i(String): boolean`; the historical `E(String)` name belongs to the superseded corpus. `ActivityMainTeam.onStart()` remains reachable, but its current body fingerprint is 93/15 under the counting rule above.

## Phase 11 characterized subpaths

The following paths are now semantically characterized directly from the official corpus and have executable modern rules:

- `ActivityEscalacao.U/V/W`: bench reorder, starter↔bench swap and starter↔starter reorder, including the legacy `Q()` snapshot-write counts 0/1/2.
- `best.o.r1()`: derives the runtime subrole code `F` from position plus the preserved `cr1/cr2` codes. Position codes outside the handled 0–4 range return without overwriting the previous value.
- `DialogTatics.e(String,best.o)`: `bEscanteios` excludes only position code 0; `fNove` admits position code 4 plus position code 3 with subrole code 1; all other keys use the whole roster. Candidate order is captured before the separate roster sort.
- `components.f3.s`: the sort used by `DialogTatics.e` orders position ascending, subrole ascending, skill descending, then star=true first; exact ties remain stable.
- `ActivitySavedTatics.g()`: rejects only exact empty names or names longer than 30 Java/Kotlin UTF-16 code units; otherwise copies the current player/slot parallel lists, stores null player IDs as `-1`, prefixes the typed name with the formation label, appends the snapshot, clears the input and refreshes the saved-tactics list. A short parallel slot list or invalid formation index is not silently repaired.
- `ActivitySavedTatics.e/b/f`: an empty saved list disables the spinner and clears its adapter; otherwise the display-name list is rebuilt and the last entry selected. Delete/load reject only an empty list or `index >= size`; a negative index is not clamped and preserves the legacy failure. Successful load returns result code `-1`, extra `idTaticaSalva=<index>` and finishes.
- `ActivityEscalacao.onActivityResult`: only request code `101` plus result `-1` consumes `idTaticaSalva`; it uses the same upper-bound-only list guard before forwarding the selected snapshot into the lineup load path.
- `DialogTatics.onCreate`: a null static club reference finishes immediately. Otherwise it loads raw option slots `S[1]`, `S[3]`, `S[2]`, checkbox `T`, wires the four player selectors, refreshes their labels, and disables every mutating control when `Q0()==false`.
- `DialogTatics.j()`: when `Q0()==false` it is a complete no-op. Otherwise the three radio groups write only known selections into `S[1]`, `S[2]`, `S[3]`, and checkbox `T` is overwritten with the UI state. Back/close commits through this path before finishing.
- `DialogTatics.k(String,best.o)`: exact keys `cap`, `bFaltas`, `bEscanteios`, `fNove` assign captain, free-kick, corner and false-nine references respectively; unknown keys do not assign.
- `best.c0.F0/G0/H0/I0`: a special-player reference is kept when the player still belongs to the club. Otherwise it is cleared unless legacy `T0()` fallback is active and that same player object still exists in `Z()`.
- `v2`: initial picker selection uses reference identity against the current special player, otherwise selects index 0 only when the candidate list is non-empty. Confirmation is a no-op for index `<0` or `>=size`; a valid selection calls `DialogTatics.k()` and closes the picker.

`ActivitySavedTatics.g()` and the `DialogTatics` host are now semantically characterized end-to-end and may leave the fail-closed state. The remaining Phase 11 host blocked on gameplay semantics is `ActivityEscalacao.B()` / SMALI `y()V`, including the complete application of a saved formation snapshot.

## Behavioral rule

Structural recovery does not unlock gameplay semantics. A method may only be promoted to runtime after Java↔SMALI characterization of its inputs, branches, state mutations, ordering and reachable callers. Existing runtime rules already characterized from the current corpus remain valid; this file corrects their host/method provenance so future work cannot silently regress to the old baseline.
