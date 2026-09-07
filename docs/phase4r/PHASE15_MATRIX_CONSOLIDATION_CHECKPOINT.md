# Fase 15 — checkpoint de consolidação da matriz

Status atual: **HISTORICAL / SUPERSEDED BY `PHASE15_LEGACY_PARITY_MATRIX.md`**

Este arquivo preserva o checkpoint intermediário de consolidação e **não deve ser usado como estado atual do Marco C**. As referências abaixo a Room V14, `best.k0.c(index)` aberto, senior `M/N` sem owner e demais gaps eram corretas quando registradas, mas foram superadas pela implementação V15 e pelos lotes posteriores. A matriz final de paridade é o índice normativo de status.

Corpus oficial: `Brasfoot.apk_Decompiler.com.zip` — SHA-256 `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465` — package `com.brasfoot.v2020` — versionCode `202632`.

## Snapshot histórico preservado

Historical status: **ACTIVE / evidence-only consolidation**

Este checkpoint não promove comportamento novo. Ele reconcilia a matriz agregada com regras/evidências Java+SMALI já congeladas e certificadas no próprio branch, evitando que linhas históricas mais conservadoras sejam tratadas como estado atual.

## 1. Fase 15.1 — Juniores

Permanece **IMPLEMENTED_AND_CERTIFIED**. A V14, o draft pré-promoção separado, a peneira, desenvolvimento, promoção manual, promoção anual, persistência, reopen e rollback continuam válidos. Nenhuma reabertura ocorre sem regressão real.

## 2. `best.b.j2(1)` / `best.a.J(1)`

A linha agregada antiga que tratava `j2(1)` como inteiramente `REACHABLE_NOT_IMPLEMENTED` está atrasada.

Estado no momento deste checkpoint:

- o dispatcher de `j2(1)` e sua ordem de visita estavam caracterizados em `PHASE15_ANNUAL_J2_COMMAND_DISPATCH.md`;
- os callees alcançáveis de `J(1)` estavam decompostos/classificados em `PHASE15_ANNUAL_J2_REMAINING_CALLEE_EVIDENCE.md`;
- o sweep `best.b.p()` preservava senior → juniores em `LegacyAnnualPlayerProgressionSweepRules`;
- a parte juvenil já era certificada;
- a parte senior ainda estava parcial naquele momento.

Classificação histórica: **PARTIALLY_IMPLEMENTED**.

## 3. `best.b.F()` / jogador anual

Estado no momento deste checkpoint:

- o control-flow dos três passes estava caracterizado;
- `best.o.d1(0)` era setter direto de `j0`;
- `best.o.D0()` tinha control-flow implementado/testado por `LegacyAnnualPlayerD0Rules`;
- `best.o.M1(Boolean)` / `W0()` pertenciam ao campo separado `d`, não ao latch anual `M`;
- persistência de `j0` + `d/W0` ainda era tratada como aberta;
- `best.k0.c(index)` ainda aparecia como `REACHABLE_NOT_IMPLEMENTED`.

Classificação histórica de `F()`: **PARTIALLY_IMPLEMENTED / PERSISTENCE_AND_K0_GAPS_OPEN**.

## 4. Progressão anual senior

Estado histórico consolidado:

- `best.o.e()` routing — **IMPLEMENTED_AND_TESTED**;
- `best.o.t()` — **IMPLEMENTED_AND_TESTED**;
- `best.o.s()` high-`d0` RNG — **IMPLEMENTED_AND_TESTED**;
- bloco final growth/cap de `best.o.s()` — **IMPLEMENTED_AND_TESTED**;
- bloco precedente de `best.o.s()` — **PARTIALLY_IMPLEMENTED / SMALI_REQUIRED**;
- `best.o.M` — **PERSISTENT_RUNTIME_GAP_PROVEN**;
- `best.o.N` — **PERSISTENT_RUNTIME_GAP_PROVEN**.

Essas classificações foram posteriormente superadas por evidência SMALI completa + runtime/persistência V15. Não reativá-las sem regressão real.

## 5. Room

Neste checkpoint histórico Room ainda permanecia **V14**. Posteriormente a Fase 15 provou e implementou V15 com delta mínimo aditivo para senior `M/N/n`; consultar a matriz final e `Phase15SeniorRuntimeMigration.kt`.

## 6. Relação com a matriz final

Este arquivo não substitui `PHASE15_LEGACY_PARITY_MATRIX.md`. Evidência histórica continua válida como trilha de investigação, mas suas classificações intermediárias não reabrem gaps já fechados/testados/certificados.