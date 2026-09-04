# Fase 15 — `best.b.d4()` / mutação de transferência diferida

Status: **T1 MUTATION IMPLEMENTED / QUEUE ORCHESTRATION STILL OPEN**

Corpus oficial: `Brasfoot.apk_Decompiler.com.zip` — SHA-256 `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465` — package `com.brasfoot.v2020` — versionCode `202632`.

Autoridade: SMALI executável prevalece sobre Java decompilado.

## Evidência já comprovada

`best.b.d4()` percorre a fila temporal `I` de `components.o2`. Cada entrada contém jogador, clube e `Calendar`; o construtor fixa vencimento em +319 dias. Quando a entrada vence, `d4()` chama `player.U1(club)` e remove a entrada processada.

No SMALI oficial, `best.o.U1(best.c0)` é apenas o wrapper para a chamada exata:

`best.o.T1(club, 0, false, false, true)`

Logo a mutação final não precisa de regra esportiva nova. O runtime moderno já possui a reconstrução de `T1` em `LegacyTransferExecutionRule` e a persistência atômica correspondente em `CareerManagerRuntimeStore.commitTransfer(...)`.

## Implementação desta etapa

`LegacyAnnualDeferredTransferExecutionRule` fixa exclusivamente os argumentos comprovados da chamada acima:

- `transferValue = 0`;
- `legacySecondaryChargeFlag = false`;
- `loanMove = false`;
- `legacyNonFinancialMoveFlag = true`.

O adapter deliberadamente começa **depois** de a fila ter selecionado um par jogador/clube vencido. Ele não cria a fila, não inventa writer, não decide vencimento e não cria persistência nova.

As regressões comparam o adapter diretamente com `LegacyTransferExecutionRule.plan(...)` nos argumentos exatos e congelam que o ramo é não-financeiro, não-loan, com valor zero e `rawY` definido como `false`.

## Auditoria do schema V14

A exportação Room V14 foi auditada contra o payload comprovado de `components.o2`. Não existe entidade explícita que preserve a semântica de uma transferência não-loan futura com jogador + clube alvo + vencimento.

Em particular, `career_active_loans` não é equivalente: apesar de possuir origem/destino e expiração, ela representa empréstimo ativo, enquanto `d4()` termina no tuple comprovado `T1(club,0,false,false,true)`. `career_player_transfer_state`, `career_player_commercial` e memberships também não contêm a fila temporal comprovada.

A auditoria completa de `o2` e `y1` contra V14 está em `PHASE15_ANNUAL_DEFERRED_STATE_MAPPING.md`.

Essa ausência de equivalente explícito **não é autorização para V15**. Os writers/callers oficiais ainda precisam ser mapeados para provar chave, cardinalidade, ordem, âncora temporal e comportamento save/reopen antes de congelar qualquer nova tabela.

## Gap restante

`best.b.d4()` como um todo permanece aberto até serem provados:

1. todos os writers alcançáveis de `components.o2`;
2. a ordem/semântica de manutenção da fila contra o calendário da carreira;
3. chave/cardinalidade e lifecycle de retomada necessários para uma eventual representação durável;
4. regressões de reopen/rollback da fila, caso persistência nova seja comprovada.

Não criar V15 apenas para representar o adapter de mutação. Room permanece V14 até nova evidência de estado durável necessário.
