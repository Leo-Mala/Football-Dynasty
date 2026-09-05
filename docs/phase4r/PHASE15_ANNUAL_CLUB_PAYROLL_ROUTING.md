# Fase 15 — roteamento anual de `best.a.s()`

Status: **IMPLEMENTED_AND_TESTED / FINANCIAL_CALLEE_OPEN**

Corpus oficial: `Brasfoot.apk_Decompiler.com.zip` — SHA-256 `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465` — package `com.brasfoot.v2020` — versionCode `202632`.

SMALI é a autoridade executável. Esta implementação usa a evidência já congelada em `PHASE15_ANNUAL_J2_REMAINING_CALLEE_EVIDENCE.md` e não aproxima o cálculo financeiro interno de `best.c0.z()/q()/E(long)`.

## Evidência congelada

`best.a.s()` percorre todos os clubes globais em source order. Para cada clube:

1. lê `best.a.D()`, equivalente ao `Calendar.MONTH` do calendário da competição;
2. chama `club.Y0(month)`;
3. somente quando `Y0(month)` retorna `true`, chama `club.z()`.

`club.z()` é mutação financeira substantiva e executa `club.E(club.q())`. O cálculo `q()` soma valores de seniores e drafts juniores, e `E(long)` reduz o campo financeiro legado e pode registrar o valor no ledger quando o predicate legado correspondente está ativo. Esses callees permanecem boundary separado nesta rodada.

Não há RNG no corpo de `best.a.s()` nem no roteamento `D() -> Y0(month) -> z()`.

## Implementação moderna

`LegacyAnnualClubPayrollRoutingRules.plan(...)` congela somente o comportamento já provado do caller:

- scan completo da lista global de clubes;
- preservação da source order;
- mesmo mês legado para todas as avaliações da execução;
- clubes cujo predicate `Y0(month)` é falso não geram chamada;
- cada clube elegível produz exatamente uma chamada correspondente a `z()`;
- nenhuma deduplicação, ordenação ou síntese de clube é introduzida.

A regra não calcula salários, não reduz caixa e não cria equivalência nova para `best.c0.n`, `best.m.e(long)` ou os valores retornados por `best.o.m0()` / `best.p.u()`.

## Regressões

`LegacyAnnualClubPayrollRoutingRulesTest` cobre:

- lista global vazia;
- filtragem exata pelo resultado de `Y0(month)`;
- preservação da source order;
- propagação do mesmo `Calendar.MONTH` legado;
- ausência de deduplicação para clubes elegíveis adjacentes.

## Estado moderno relevante

`CareerClubFinanceInputResolver` já demonstra que o runtime moderno possui acesso estruturado aos rosters senior/junior e ao estado financeiro necessário para continuar a investigação do callee, mas essa disponibilidade não autoriza assumir que os códigos modernos equivalem numericamente a `best.o.m0()` / `best.p.u()` sem fechar essa prova.

## Persistência

Room permanece V14. Esta regra pura não cria estado persistente e não autoriza migration, backfill, default ou destructive migration.

## Classificação

- `best.a.s()` routing → **IMPLEMENTED_AND_TESTED**;
- `best.c0.Y0(month)` caller predicate usage → **IMPLEMENTED_AND_TESTED AS ROUTING INPUT**;
- `best.c0.z()` → **CHARACTERIZED / CALLEE_IMPLEMENTATION_OPEN**;
- `best.c0.q()` / `best.c0.E(long)` → **CHARACTERIZED / MODERN_VALUE_MAPPING_REQUIRED**;
- comando anual `ds` end-to-end → **PARTIALLY_IMPLEMENTED**.

A Fase 15 permanece aberta até o cálculo financeiro interno e sua composição transacional com o runtime moderno serem provados e testados sem alterar dados esportivos ou inventar equivalências.