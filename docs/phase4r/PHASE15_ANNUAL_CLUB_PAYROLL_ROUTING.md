# Fase 15 — roteamento e composição anual de `best.a.s()`

Status: **PARTIALLY_IMPLEMENTED / LEGACY_VALUE_MAPPING_OPEN**

Corpus oficial: `Brasfoot.apk_Decompiler.com.zip` — SHA-256 `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465` — package `com.brasfoot.v2020` — versionCode `202632`.

SMALI é a autoridade executável. Esta implementação usa somente evidência já congelada em `PHASE15_ANNUAL_J2_REMAINING_CALLEE_EVIDENCE.md`; não aproxima `best.o.m0()` nem `best.p.u()`.

## Evidência congelada

`best.a.s()` percorre todos os clubes globais em source order. Para cada clube lê o `Calendar.MONTH` legado, chama `club.Y0(month)` e, quando verdadeiro, chama `club.z()`.

`best.c0.z()` executa exatamente `E(q())`. O SMALI prova que `best.c0.q()` acumula como JVM `long`, nesta ordem:

1. cada valor já calculado por `best.o.m0()` para os seniores de `club.Z()`;
2. cada valor já calculado por `best.p.u()` para os drafts juniores de `club.M`.

`best.c0.E(value)` subtrai o valor de `best.c0.n` e, quando `Y != null && Q0()`, encaminha o mesmo valor para `Y.e(value)`.

Não há RNG em `s()`, `Y0()`, `z()`, `q()` ou `E(long)`.

## Implementação moderna

`LegacyAnnualClubPayrollRoutingRules.plan(...)` congela o scan/source order/predicate mensal já provado.

`LegacyAnnualClubPayrollCompositionRules.compose(...)` agora congela a composição numérica comprovada de `q()` sem converter códigos salariais modernos:

- acumula todos os valores senior antes dos junior;
- usa aritmética `Long` JVM sem clamp/saturação inventados;
- retorna subtotais apenas para observabilidade de teste; o efeito legado é o `total`;
- não cria RNG, ordenação ou deduplicação.

O runtime moderno já possui `CareerClubFinanceInputResolver.resolvePayroll(...)`, que resolve os códigos salariais dos membros SENIOR/JUNIOR a partir dos rosters persistidos e falha fechado quando o estado comercial está ausente. Isso fecha a origem estrutural das listas, mas **não prova ainda** que o campo moderno `salario` seja numericamente igual ao retorno de `best.o.m0()` / `best.p.u()`.

## Regressões

`LegacyAnnualClubPayrollRoutingRulesTest` cobre o routing do caller.

`LegacyAnnualClubPayrollCompositionRulesTest` cobre:

- listas vazias;
- soma senior seguida da junior;
- subtotais independentes;
- overflow JVM `long` sem clamp moderno;
- continuidade do acumulador senior para junior.

## Persistência

Room permanece V14. A composição é pura e não autoriza migration, backfill, default ou destructive migration.

## Classificação

- `best.a.s()` routing → **IMPLEMENTED_AND_TESTED**;
- `best.c0.Y0(month)` caller predicate usage → **IMPLEMENTED_AND_TESTED AS ROUTING INPUT**;
- `best.c0.q()` composição de contribuições já calculadas → **IMPLEMENTED_AND_TESTED**;
- source roster SENIOR/JUNIOR moderno → **MAPPED** via `CareerClubFinanceInputResolver`;
- `best.o.m0()` / `best.p.u()` ↔ códigos modernos `salario` → **SMALI_REQUIRED / NOT_INFERRED**;
- `best.c0.E(long)` transação/ledger moderna → **MAPPING_REQUIRED**;
- comando anual `ds` end-to-end → **PARTIALLY_IMPLEMENTED**.

O blocker financeiro foi reduzido: não é mais necessário reimplementar a soma de `q()`. O próximo passo é provar as duas conversões de contribuição e o equivalente de `E(long)`; só então compor `ds` transacionalmente.
