# Fase 15 — `best.b.d4()` / retorno de empréstimo vencido

Status: **LEGACY LOAN LIFECYCLE MAPPED TO V14 / END MUTATION IMPLEMENTED**

Corpus oficial: `Brasfoot.apk_Decompiler.com.zip` — SHA-256 `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465` — package `com.brasfoot.v2020` — versionCode `202632`.

Autoridade: SMALI executável prevalece sobre Java decompilado.

## Correção da interpretação anterior

A leitura de `d4()` isoladamente mostrava uma fila `components.o2` cuja entrada vencida executava `player.U1(club)`, isto é:

`best.o.T1(club, 0, false, false, true)`.

Como essa chamada final usa a rota não-loan de `T1`, a nota anterior tratava `o2` conservadoramente como uma transferência diferida separada de `career_active_loans`.

A auditoria do writer oficial prova que essa conclusão provisória estava incompleta.

## Lifecycle completo comprovado

### 1. início em `best.o.q(targetClub)`

Antes de mover o jogador, `q()` cria:

`components.o2(player, player.u0(), best.b.h)`.

O segundo argumento é portanto o **clube de origem**. O construtor clona a data base, adiciona **319 dias** e registra a entrada na fila global.

Depois da criação do registro, `q()` chama imediatamente:

`T1(targetClub, 0, false, true, false)`.

Esse é o movimento de **início do empréstimo**. O jogador passa ao destino, enquanto `o2` preserva origem e vencimento.

Callers alcançáveis observados para `q(targetClub)` incluem `ActivityTimes`, `ActivityMainTeam`, `ActivityPaises`, `ActivityProcura` e `best.f`.

### 2. vencimento em `best.b.d4()`

`d4()` percorre os empréstimos serializados. Para cada entrada vencida com referências válidas, executa:

`player.U1(originClub)`.

No SMALI:

`U1(originClub)` = `T1(originClub, 0, false, false, true)`.

A entrada é removida após o retorno. Logo o ramo não-loan de `d4()` é a **operação de encerramento/retorno**, não evidência de uma categoria de fila diferente.

## Equivalência moderna

A V14 já possui `career_active_loans(careerId, playerId, originClubId, loanClubId, expiresAtEpochMillis)`, que normaliza exatamente o estado serializado de `components.o2`.

A camada moderna de empréstimos já caracteriza os gates de listagem/aceitação e o runtime de movimentação; o retorno no vencimento desemboca na mesma reconstrução de `T1` usada pelas demais movimentações.

`LegacyAnnualDeferredTransferExecutionRule` permanece útil como adapter estreito da chamada final comprovada:

- `transferValue = 0`;
- `legacySecondaryChargeFlag = false`;
- `loanMove = false`;
- `legacyNonFinancialMoveFlag = true`.

Ele representa somente `U1/T1` no retorno. **Não** deve ganhar uma tabela paralela de “deferred transfer”.

## Decisão de persistência

Não criar V15 para `d4()`/`o2`.

O mapping correto é:

`components.o2` → `career_active_loans`.

A conclusão anterior de “nenhuma entidade equivalente” é superada por esta evidência de writer + lifecycle completo. O schema permanece V14, sem backfill, default inventado ou destructive migration.

## Estado da paridade

A incógnita estrutural de `d4()` foi eliminada. Qualquer trabalho adicional nessa área deve ser apenas regressão de composição/end-to-end contra os stores modernos existentes; não criação de uma segunda persistência.

A Fase 15 deve continuar nos gaps anuais ainda materiais (`j2(1)`, `M0/F2(true)`, `F()` e composição `best.f`).
