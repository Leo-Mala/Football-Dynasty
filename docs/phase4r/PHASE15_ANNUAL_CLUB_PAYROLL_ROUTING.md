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

A reconstrução financeira já existente prova ainda que `best.m.e(long)` é o caminho separado de salário long-valued e corresponde a `LegacyFinanceLedgerRule.addSalaryExpense(...)`, preservando valores acima de `Int.MAX_VALUE` sem truncamento.

Não há RNG em `s()`, `Y0()`, `z()`, `q()` ou `E(long)`.

## Implementação moderna

`LegacyAnnualClubPayrollRoutingRules.plan(...)` congela o scan/source order/predicate mensal já provado.

`LegacyAnnualClubPayrollCompositionRules.compose(...)` congela a composição numérica comprovada de `q()` sem converter códigos salariais modernos:

- acumula todos os valores senior antes dos junior;
- usa aritmética `Long` JVM sem clamp/saturação inventados;
- retorna subtotais apenas para observabilidade de teste; o efeito legado é o `total`;
- não cria RNG, ordenação ou deduplicação.

`LegacyFinanceRuntimeRule.applySalaryDebit(...)` agora congela o efeito puro completo de `best.c0.E(long)` depois que `q()` já produziu o total:

- subtrai o mesmo `Long` do caixa moderno, mapeado para o campo legado `best.c0.n`;
- quando o predicate caracterizado `Q0()` está ativo, encaminha exatamente o mesmo `Long` para `LegacyFinanceLedgerRule.addSalaryExpense(...)`, equivalente já reconstruído de `best.m.e(long)`;
- quando `Q0()` está inativo, o caixa ainda é debitado e o ledger permanece intacto;
- preserva overflow JVM `Long` tanto no caixa quanto no acumulador salarial;
- não cria regra de insuficiência de caixa, clamp, categoria alternativa ou RNG.

O runtime moderno já possui `CareerClubFinanceInputResolver.resolvePayroll(...)`, que resolve os códigos salariais dos membros SENIOR/JUNIOR a partir dos rosters persistidos e falha fechado quando o estado comercial está ausente. O mesmo resolver representa o predicate `Q0()` pelo estado ativo já materializado do runtime do clube. Isso fecha o efeito financeiro do callee `E(long)`, mas **não prova ainda** que o campo moderno `salario` seja numericamente igual ao retorno de `best.o.m0()` / `best.p.u()`.

## Regressões

`LegacyAnnualClubPayrollRoutingRulesTest` cobre o routing do caller.

`LegacyAnnualClubPayrollCompositionRulesTest` cobre:

- listas vazias;
- soma senior seguida da junior;
- subtotais independentes;
- overflow JVM `long` sem clamp moderno;
- continuidade do acumulador senior para junior.

`LegacyFinanceRuntimeRuleTest` cobre agora `E(long)` com:

- valor acima de `Int.MAX_VALUE` preservado integralmente no caixa e ledger;
- `Q0()==false` debitando caixa sem escrever salário no ledger;
- overflow JVM `Long` preservado nos dois acumuladores.

## Persistência

Room permanece V14. O caixa e o acumulador `salaryExpense` já existem em `career_club_manager_runtime`; nenhuma coluna nova é necessária para representar `E(long)`. Nenhuma migration, backfill, default ou destructive migration é autorizada por este bloco.

## Classificação

- `best.a.s()` routing → **IMPLEMENTED_AND_TESTED**;
- `best.c0.Y0(month)` caller predicate usage → **IMPLEMENTED_AND_TESTED AS ROUTING INPUT**;
- `best.c0.q()` composição de contribuições já calculadas → **IMPLEMENTED_AND_TESTED**;
- source roster SENIOR/JUNIOR moderno → **MAPPED** via `CareerClubFinanceInputResolver`;
- `best.c0.E(long)` efeito cash + ledger → **IMPLEMENTED_AND_TESTED**;
- `best.m.e(long)` → **IMPLEMENTED_AND_TESTED** via `LegacyFinanceLedgerRule.addSalaryExpense`;
- `best.o.m0()` / `best.p.u()` ↔ códigos modernos `salario` → **SMALI_REQUIRED / NOT_INFERRED**;
- comando anual `ds` end-to-end → **PARTIALLY_IMPLEMENTED**.

O blocker financeiro foi reduzido ao mapeamento numérico das duas contribuições de jogador. Assim que `best.o.m0()` e `best.p.u()` forem provados contra o SMALI oficial, `q()` e `E(long)` já possuem composição moderna pronta sem necessidade de novo estado persistente.
