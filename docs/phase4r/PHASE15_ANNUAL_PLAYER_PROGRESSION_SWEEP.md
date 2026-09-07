# Fase 15 — sweep anual de progressão senior → juniores

Status: **ORCHESTRATION_IMPLEMENTED / senior mutation still open**

Corpus oficial: `Brasfoot.apk_Decompiler.com.zip` — SHA-256 `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465` — package `com.brasfoot.v2020` — versionCode `202632`.

SMALI é a autoridade executável.

## Evidência alcançável

O comando anual `aj` em `best.a.J(1)` chama `best.b.p()`.

O SMALI de `best.b.p()` preserva dois sweeps completos, nesta ordem:

1. percorre a lista global de jogadores seniores e chama `best.o.e()` em cada jogador;
2. somente depois percorre clubes/juniores e chama `best.p.b()` em cada draft juvenil.

Não existe intercalação entre esses dois passes.

## Implementação moderna

`LegacyAnnualPlayerProgressionSweepRules` congela exclusivamente essa ordem executável:

- `ProgressSeniorPlayer(playerIndex)` para todos os seniores, em source order;
- depois `ProgressJuniorDraft(clubIndex, juniorIndex)` para todos os drafts, preservando ordem de clube e ordem interna.

A regra não executa nem aproxima `best.o.e()`. O lifecycle senior continua bloqueado até o mapa durável de `M/N` e o ramo de RNG implícito ficarem totalmente provados. A progressão juvenil `best.p.b()` continua coberta pela Fase 15.1 já certificada.

## Regressões

`LegacyAnnualPlayerProgressionSweepRulesTest` cobre:

- todos os seniores antes de qualquer junior;
- ordem dos clubes e dos drafts dentro de cada clube;
- coleções vazias;
- rejeição de contagens negativas.

## Persistência

Nenhuma alteração Room foi feita neste checkpoint. O schema permanece V14.

O gap já comprovado de `best.o.N : double` e `best.o.M : Boolean` continua documentado em `PHASE15_ANNUAL_SENIOR_PROGRESSION_GAP.md`; esta orquestração não autoriza V15 isoladamente.

## Classificação

- `best.b.p()` — **PARTIALLY_IMPLEMENTED**: ordem/orquestração dos sweeps implementada; sweep juvenil certificado; mutação senior pendente;
- `best.o.e()` — **REACHABLE_NOT_IMPLEMENTED**;
- `best.p.b()` — **IMPLEMENTED_AND_CERTIFIED** pela Fase 15.1.

## Próximo passo

Fechar corpus-wide o mapa de `best.o.M/N/e/j/m/d0/W0/O0`, confrontar com `CareerPlayerRuntimeEntity`/mapper/store e só então congelar qualquer extensão de persistência e regra anual senior.
