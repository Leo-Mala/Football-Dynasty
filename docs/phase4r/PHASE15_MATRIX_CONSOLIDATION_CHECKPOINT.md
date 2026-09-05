# Fase 15 — checkpoint de consolidação da matriz

Status: **ACTIVE / evidence-only consolidation**

Corpus oficial: `Brasfoot.apk_Decompiler.com.zip` — SHA-256 `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465` — package `com.brasfoot.v2020` — versionCode `202632`.

Este checkpoint não promove comportamento novo. Ele reconcilia a matriz agregada com regras/evidências Java+SMALI já congeladas e certificadas no próprio branch, evitando que linhas históricas mais conservadoras sejam tratadas como estado atual.

## 1. Fase 15.1 — Juniores

Permanece **IMPLEMENTED_AND_CERTIFIED**. A V14, o draft pré-promoção separado, a peneira, desenvolvimento, promoção manual, promoção anual, persistência, reopen e rollback continuam válidos. Nenhuma reabertura ocorre sem regressão real.

## 2. `best.b.j2(1)` / `best.a.J(1)`

A linha agregada antiga que tratava `j2(1)` como inteiramente `REACHABLE_NOT_IMPLEMENTED` está atrasada.

Estado atual comprovado no branch:

- o dispatcher de `j2(1)` e sua ordem de visita estão caracterizados em `PHASE15_ANNUAL_J2_COMMAND_DISPATCH.md`;
- os callees alcançáveis de `J(1)` foram decompostos/classificados em `PHASE15_ANNUAL_J2_REMAINING_CALLEE_EVIDENCE.md`;
- o sweep `best.b.p()` preserva senior → juniores em `LegacyAnnualPlayerProgressionSweepRules`;
- a parte juvenil do sweep é certificada;
- a parte senior permanece parcial somente porque `best.o.s()` ainda não está integralmente promovido.

Classificação consolidada: **PARTIALLY_IMPLEMENTED**, não `REACHABLE_NOT_IMPLEMENTED` global.

## 3. `best.b.F()` / jogador anual

A linha agregada antiga que tratava `F()` como totalmente não implementado também está atrasada.

Estado atual comprovado:

- o control-flow dos três passes foi caracterizado;
- `best.o.d1(0)` é setter direto de `j0`;
- `best.o.D0()` tem control-flow implementado/testado por `LegacyAnnualPlayerD0Rules`;
- `best.o.M1(Boolean)` / `W0()` pertencem ao campo separado `d`, não ao latch anual `M`;
- persistência de `j0` + `d/W0` ainda está aberta;
- `best.k0.c(index)` continua `REACHABLE_NOT_IMPLEMENTED` e impede classificar `F()` inteiro como completo.

Classificação consolidada de `F()`: **PARTIALLY_IMPLEMENTED / PERSISTENCE_AND_K0_GAPS_OPEN**.

## 4. Progressão anual senior

Estado consolidado:

- `best.o.e()` routing — **IMPLEMENTED_AND_TESTED**;
- `best.o.t()` — **IMPLEMENTED_AND_TESTED**;
- `best.o.s()` high-`d0` RNG — **IMPLEMENTED_AND_TESTED**;
- bloco final growth/cap de `best.o.s()` — **IMPLEMENTED_AND_TESTED**;
- bloco precedente de `best.o.s()` — **PARTIALLY_IMPLEMENTED / SMALI_REQUIRED**;
- `best.o.M` — **PERSISTENT_RUNTIME_GAP_PROVEN**;
- `best.o.N` — **PERSISTENT_RUNTIME_GAP_PROVEN**.

Não associar as taxas observadas `0.16/0.12/0.10/0.08/0.06/0.04/0.02` a branches sem reabrir o SMALI executável oficial. Não criar Room V15 enquanto esse mapa e os demais estados anuais não estiverem fechados.

## 5. Room

Room permanece **V14**. Este checkpoint não cria migration, backfill, default esportivo ou `fallbackToDestructiveMigration`.

A eventual V15 continua bloqueada até existir um delta persistente agregado e comprovado para, no mínimo, os estados ainda necessários de progressão senior e `F()/D0()/k0`.

## 6. Ação sobre a matriz agregada

Na próxima edição integral de `PHASE15_LEGACY_PARITY_MATRIX.md`, substituir as classificações históricas conflitantes acima pelas classificações consolidadas deste checkpoint. Não remover evidência histórica; marcar conclusões superadas como superseded quando necessário.

Este arquivo é um checkpoint de reconciliação, não um substituto permanente da matriz final do Marco C.