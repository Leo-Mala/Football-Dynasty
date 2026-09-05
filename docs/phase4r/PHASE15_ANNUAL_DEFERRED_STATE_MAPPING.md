# Fase 15 — auditoria de estado diferido anual contra Room V14

Status: **LEGACY QUEUES MAPPED TO EXISTING V14 STATE / NO V15 REQUIRED FOR `o2` OR `y1`**

Corpus oficial de referência: `Brasfoot.apk_Decompiler.com.zip` — SHA-256 `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465` — package `com.brasfoot.v2020` — versionCode `202632`.

Autoridade: SMALI executável prevalece sobre Java decompilado. Esta nota corrige a conclusão provisória anterior da própria Fase 15 depois de ampliar a auditoria para os writers/callers reais dos payloads `components.o2` e `components.y1`.

## `components.o2` é o registro serializado de empréstimo ativo

A leitura isolada de `best.b.d4()` mostrava apenas a metade final do lifecycle: entrada vencida → `player.U1(club)` → remoção da fila. Isso havia levado provisoriamente à conclusão conservadora de que a fila não era equivalente a `career_active_loans` porque `U1()` termina no ramo não-loan de `T1`.

A auditoria do writer oficial resolve a ambiguidade.

### Writer / início do empréstimo

`best.o.q(targetClub)` executa, nesta ordem:

1. cria `components.o2(thisPlayer, thisPlayer.u0(), best.b.h)`; portanto captura o **clube de origem antes da mudança**;
2. o construtor de `o2` clona a data base, adiciona **319 dias** e registra a própria entrada na fila global;
3. imediatamente executa `T1(targetClub, 0, false, true, false)`.

O quarto argumento `true` é a rota de empréstimo caracterizada de `T1`: o jogador é movido imediatamente ao clube de empréstimo, sem fluxo financeiro de transferência definitiva, e o registro `o2` preserva origem + vencimento para retorno futuro.

Callers alcançáveis de `q(targetClub)` observados no corpus incluem `ActivityTimes`, `ActivityMainTeam`, `ActivityPaises`, `ActivityProcura` e `best.f`.

### Vencimento / retorno

`best.b.d4()` percorre a fila `I`. Quando a data de `o2` vence, chama `player.U1(originClub)` e remove a entrada. No SMALI oficial:

`U1(originClub)` = `T1(originClub, 0, false, false, true)`.

Portanto o ramo não-loan observado em `d4()` **não representa uma segunda categoria de fila**; ele é a operação de retorno que encerra o empréstimo iniciado por `q()`.

### Equivalência moderna V14

A entidade V14 `career_active_loans` guarda exatamente o estado durável normalizado necessário para o mesmo lifecycle:

- `careerId`;
- `playerId`;
- `originClubId`;
- `loanClubId`;
- `expiresAtEpochMillis`.

O runtime moderno já separa corretamente a decisão/validação do empréstimo, a movimentação de início e o retorno no vencimento. O adapter `LegacyAnnualDeferredTransferExecutionRule` continua válido como congelamento dos argumentos exatos da mutação final `U1/T1`; ele não exige uma segunda fila.

Conclusão corrigida: **`components.o2` ↔ `career_active_loans` é uma equivalência de lifecycle comprovada, não mera similaridade estrutural. Não criar V15 para `o2`.**

## `components.y1` é construção de estádio pendente

A auditoria dos writers também resolve o owner e o significado dos quatro acumuladores de `y1`.

### Payload e writer

`components.y1` é serializável e contém:

- `best.k` — estádio;
- `Calendar` — data de conclusão;
- `int[4]` — quatro incrementos pendentes.

O writer alcançável observado está em `ActivityEstadio`: ele cria `y1`, associa o estádio corrente, grava a data calculada, grava o vetor de quatro incrementos e adiciona a entrada à fila `best.b.g0()`. No mesmo fluxo, o custo é debitado com código financeiro bruto **7**.

O cálculo oficial preserva:

- custo por categorias + custo fixo `100000`;
- duração por crescimento total: `<1000 → 15 dias`, `<10000 → 20`, `<30000 → 30`, caso contrário `40` dias.

### Vencimento

`best.b.e4()` processa as entradas vencidas. `components.y1.a()` percorre índices 0..3; para cada valor positivo chama `best.k.h(index,value)` e zera o slot. A entrada é então removida. Não há RNG nesse payload.

### Equivalência moderna V14

O runtime moderno já normaliza esse mesmo estado em `career_stadium_constructions`, com:

- carreira/clube;
- quatro capacidades planejadas/incrementos;
- timestamp de conclusão.

`LegacyStadiumConstructionRule` preserva custos, limiares de duração e aplicação dos quatro incrementos. `CareerStadiumConstructionRuntimeStore` persiste a construção e o débito, e `CareerStadiumConstructionCompletionStore` aplica construções vencidas e remove a entrada em transação.

Conclusão corrigida: **`components.y1` ↔ `career_stadium_constructions` é equivalência de lifecycle comprovada. Não criar V15 para `y1`.**

## Decisão de schema

Room permanece **V14**. A investigação dos writers remove, em vez de criar, a justificativa para uma migration V15 nesses dois gaps:

- `o2` usa `career_active_loans`;
- `y1` usa `career_stadium_constructions`.

Não adicionar tabela paralela, backfill esportivo, defaults inventados nem `fallbackToDestructiveMigration`.

## Impacto na Fase 15

Os gaps anuais `d4()` e `e4()` deixam de ser blockers de schema. O trabalho restante do lifecycle anual deve avançar para `j2(1)`, papel de `M0/F2(true)`, `F()` e composição da seleção `best.f`, mantendo regressões/reopen/rollback nos boundaries modernos já existentes.
