# Fase 15 — auditoria de equivalência runtime de `best.b.F()`

Status: **PROVEN GAP / no schema change authorized yet**

Corpus oficial: `Brasfoot.apk_Decompiler.com.zip` — SHA-256 `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465` — package `com.brasfoot.v2020` — versionCode `202632`.

Esta auditoria parte da evidência SMALI já congelada em `PHASE15_ANNUAL_F_RESET_EVIDENCE.md` e confronta os estados exigidos pelos callees substantivos de `best.b.F()` com o runtime moderno persistido no exact baseline `22024a53954b177c18dea045365f700ab7fcc129`.

## 1. Estado de jogador exigido por `best.o.d1(0)` / `best.o.D0()`

A evidência executável já comprovada exige pelo menos:

- contador inteiro legado `j0`, zerado por `d1(0)` e incrementado por `D0()`;
- flag escrito por `M1(TRUE)` quando os guards/thresholds de `D0()` são satisfeitos.

O `CareerPlayerRuntimeEntity` V14 persiste atualmente:

- idade, overall e market value;
- `star` / `worldTop`;
- hash/geração/ano de criação;
- contrato/valor anterior;
- flags legadas `Q/X/Y/Z`;
- energia e lesão.

Não existe coluna explicitamente mapeada para `j0`, nem existe coluna já comprovada como equivalente semântico do flag escrito por `M1(TRUE)`.

Consequência: **não é seguro compor `o.D0()` no runtime real nem reutilizar uma flag existente por semelhança nominal**. Antes de qualquer migration, o corpus precisa fechar integralmente os thresholds de `D0()` e a identidade/leituras do campo gravado por `M1`.

## 2. Estado de tournament exigido por `best.k0.c(index)`

A evidência SMALI já congelada prova que `k0.c(index)` não é um simples reset de rodada. Ele chama `U()`, cria `best.h0`, percorre `components.n1` na sequência fixa `[0,1,2,2,5,6,6,3,3,4,4]`, seleciona jogadores por thresholds, adiciona estado a coleções internas e possui pelo menos um ramo com efeito adicional de flag de jogador.

O runtime moderno V14 de competição (`CareerCompetitionEntity`, `CareerCompetitionStandingEntity`, `CareerCompetitionMatchEntity`) persiste somente a projeção já comprovada de:

- tipo/formato da competição;
- rodada atual/total;
- `nRebaixados` quando provado;
- subtipo `x0()` quando provado;
- standings;
- ligação ordenada de partidas por rodada.

`CareerCompetitionStore` implementa esse subconjunto de liga e não contém estrutura já provada equivalente às coleções internas produzidas por `k0.c(index)` / `components.n1`.

Consequência: **não é seguro tratar `k0.c(index)` como equivalente ao avanço/reset de rodada existente**. A semântica interna precisa ser fechada primeiro no corpus.

## 3. Decisão de persistência

Esta auditoria comprova ausência de equivalência V14 já mapeada, mas **não autoriza Room V15 ainda**.

Uma migration só poderá ser introduzida se a investigação do corpus provar, de forma suficiente:

1. significado e lifecycle durável de `j0`;
2. significado e readers/writers do flag de `M1`;
3. quais estados produzidos por `k0.c(index)` sobrevivem ao save/load e são necessários para continuidade;
4. cardinalidade, identidade e ordem dessas estruturas;
5. ausência de outra representação moderna já existente após o mapping semântico completo.

Não criar defaults/backfills esportivos, não mapear por semelhança de nome e não usar destructive migration.

## 4. Classificação atualizada

- `best.b.F()` — **IMPLEMENTED_NEEDS_REVALIDATION** apenas no control-flow puro;
- `best.o.d1(0)` — **CHARACTERIZED**, porém sem slot persistente moderno provado para `j0`;
- `best.o.D0()` — **REACHABLE_NOT_IMPLEMENTED**; gap de estado moderno comprovado;
- `best.k0.c(index)` — **REACHABLE_NOT_IMPLEMENTED**; gap de estado tournament moderno comprovado;
- Room V14 — **PRESERVAR** até a semântica completa justificar alteração.

## 5. Próxima investigação obrigatória

1. reabrir `best/o.smali` e congelar `D0`, `W0`, `M1` e todos os readers do campo escrito por `M1`;
2. congelar os thresholds exatos de `club.J()` / `club.j0()` usados por `D0()`;
3. reabrir `best/k0.smali`, `components/n1.smali` e `best/h0.smali` e classificar cada estado criado por `k0.c(index)`;
4. confrontar somente então esse estado com Room/runtime moderno;
5. implementar + testar atomicamente apenas o delta comprovado.
