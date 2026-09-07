# Fase 15 — roteamento e composição anual de `best.a.s()`

Status: **PARTIALLY_IMPLEMENTED / SENIOR RAW-N DURABLE MAPPING OPEN**

Corpus oficial: `Brasfoot.apk_Decompiler.com.zip` — SHA-256 `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465` — package `com.brasfoot.v2020` — versionCode `202632`.

SMALI é a autoridade executável. O ZIP bruto foi reaberto e o SHA-256 foi confirmado antes desta atualização.

## Evidência executável

`best.a.s()` percorre todos os clubes em source order, lê `Calendar.MONTH`, chama `club.Y0(month)` e, quando verdadeiro, chama `club.z()`.

`best.c0.z()` executa `E(q())`. `q()` acumula como JVM `long`, primeiro os seniores de `club.Z()` e depois os drafts de `club.M`.

A antiga pendência `m0()/u()` foi resolvida diretamente no SMALI oficial:

- `best.o.m0()I` executa somente `iget best.o.n:I` + `return`;
- `best.p.u()I` executa somente `iget best.p.i:I` + `return`;
- `q()` amplia cada `int` retornado para `long` e soma sem transformação adicional.

Portanto não existe fórmula salarial escondida nesses getters. A contribuição anual é exatamente o raw `best.o.n` para senior e o raw `best.p.i` para junior.

V14 já preserva `best.p.i` como `CareerJuniorDraftEntity.legacyI`. O equivalente durável completo de senior `best.o.n` ainda não está provado no runtime moderno e permanece gap de persistência/mapeamento; não foi criado default ou backfill.

`best.c0.E(value)` já está congelado por `LegacyFinanceRuntimeRule.applySalaryDebit(...)`: subtrai o mesmo `Long` do caixa e, quando o predicate caracterizado está ativo, encaminha o mesmo valor ao acumulador salarial do ledger.

## Implementação

`LegacyAnnualClubPayrollCompositionRules` agora expõe:

- `seniorContributionFromLegacyN(Int)`;
- `juniorContributionFromLegacyI(Int)`;
- `composeFromRawFields(...)`;
- a composição long existente, preservando ordem senior→junior e overflow JVM.

Isto fecha a semântica numérica de `best.o.m0()` e `best.p.u()` sem equiparar por inferência nenhum outro campo moderno ao `best.o.n`.

## Classificação

- `best.a.s()` routing → **IMPLEMENTED_AND_CERTIFIED**;
- `best.c0.q()` composição → **IMPLEMENTED_AND_CERTIFIED**;
- `best.o.m0()` → **IMPLEMENTED_AND_TESTED / RAW best.o.n PROVEN**;
- `best.p.u()` → **IMPLEMENTED_AND_TESTED / V14 legacyI MAPPED**;
- `best.c0.E(long)` → **IMPLEMENTED_AND_CERTIFIED**;
- `best.m.e(long)` → **IMPLEMENTED_AND_CERTIFIED**;
- senior `best.o.n` durable modern owner → **PERSISTENT_RUNTIME_GAP_PROVEN**;
- comando `ds` end-to-end → **PARTIALLY_IMPLEMENTED** até o raw senior `n` estar duravelmente disponível.

## Room

Room permanece V14. A descoberta de um raw senior scalar faltante será agrupada com os demais estados seniores persistentes antes de qualquer V15. Nenhuma migration, backfill, default ou destructive migration foi introduzida.
