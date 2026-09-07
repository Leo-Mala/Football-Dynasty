# Fase 15 — roteamento anual senior `best.o.e()`

Status: **CONTROL_FLOW_IMPLEMENTED / growth mutation still partial**

Corpus oficial: `Brasfoot.apk_Decompiler.com.zip` — SHA-256 `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465` — package `com.brasfoot.v2020` — versionCode `202632`.

SMALI é a autoridade executável. A evidência vem de `smali/best/o.smali` e do call path alcançável `best.a.J(1)` comando `aj` → `best.b.p()` → `best.o.e()`.

## Controle de fluxo congelado

`best.o.e()` executa exatamente este roteamento:

1. resolve o clube atual via `u0()`;
2. se o clube for nulo, retorna imediatamente;
3. nesse retorno antecipado, `M` **não** é limpo;
4. com clube presente e campo de idade `e < 32`, chama `s()`;
5. com clube presente e `e >= 32`, chama `t()`;
6. somente depois do caminho `s()` ou `t()`, grava `M = FALSE`.

O boundary de idade é portanto exato: `31` ainda cresce e `32` já entra no declínio.

## Implementação moderna

`LegacyAnnualSeniorProgressionRoutingRules.steps(...)` congela somente o roteamento do caller:

- sem clube → lista vazia;
- idade `< 32` → `APPLY_GROWTH`, `CLEAR_LEGACY_M`;
- idade `>= 32` → `APPLY_DECLINE`, `CLEAR_LEGACY_M`.

A regra não inventa efeitos de `s()` nem repete `t()`. `LegacyAnnualSeniorDeclineRules` continua sendo a projeção já testada do declínio, enquanto `best.o.s()` permanece parcialmente caracterizado.

## Regressões

`LegacyAnnualSeniorProgressionRoutingRulesTest` cobre:

- retorno sem clube sem progressão e sem clear de `M`;
- idade `31` chamando crescimento antes do clear;
- idade `32` chamando declínio antes do clear.

## Persistência

Nenhuma alteração Room é autorizada por este checkpoint. V14 permanece vigente. Os gaps duráveis de `M` e `N` continuam abertos para composição final da progressão senior; não há backfill ou default esportivo inventado.

## Classificação

- `best.o.e()` control flow — **IMPLEMENTED_AND_TESTED**;
- `best.o.t()` — **IMPLEMENTED_AND_TESTED**;
- `best.o.s()` high-d0 RNG branch — **IMPLEMENTED_AND_TESTED**;
- `best.o.s()` completo — **PARTIALLY_CHARACTERIZED / REACHABLE_NOT_IMPLEMENTED**;
- persistência `M/N` — **GAP_PROVEN / SCHEMA_CHANGE_NOT_YET_AUTHORIZED**.
