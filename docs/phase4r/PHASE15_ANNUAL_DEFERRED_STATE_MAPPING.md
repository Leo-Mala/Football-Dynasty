# Fase 15 — auditoria de estado diferido anual contra Room V14

Status: **SCHEMA AUDITED / WRITERS STILL OPEN / NO V15 JUSTIFIED**

Corpus oficial de referência: `Brasfoot.apk_Decompiler.com.zip` — SHA-256 `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465` — package `com.brasfoot.v2020` — versionCode `202632`.

Autoridade: SMALI executável prevalece sobre Java decompilado. Esta nota não adiciona regra esportiva nova; ela compara exclusivamente os payloads já caracterizados e congelados em `PHASE15_ANNUAL_NM_ROUTER_EVIDENCE.md` / `PHASE15_ANNUAL_D4_DEFERRED_TRANSFER.md` com o schema Room V14 atualmente exportado.

## Estado legado já comprovado

### `components.o2` / `best.b.d4()`

O payload já caracterizado contém jogador, clube e `Calendar` de vencimento. O construtor agenda +319 dias e a entrada vencida executa `player.U1(club)`, que no SMALI oficial é `T1(club, 0, false, false, true)`, antes da remoção da fila.

A mutação final já possui seam moderno isolado em `LegacyAnnualDeferredTransferExecutionRule`. O gap não é mais o `T1`: é a criação, ordenação, persistência e retomada da fila.

### `components.y1` / `best.b.e4()`

O payload já caracterizado contém `best.k`, `Calendar` e `int[4]`. No vencimento aplica cada slot positivo em ordem ascendente via `best.k.h(index,value)`, zera o slot aplicado e depois a entrada é removida. Não há RNG nesse payload.

O gap é provar todos os writers, identificar de forma inequívoca o owner moderno de `best.k` e decidir a representação durável sem inventar equivalência.

## Auditoria do schema Room V14

O `FootballDynastyDatabase` atual declara schema 14 e as entidades existentes. A exportação `14.json` foi auditada procurando uma estrutura capaz de preservar os campos e a semântica temporal dos dois payloads.

### Estruturas relacionadas que NÃO são equivalentes a `components.o2`

- `career_player_transfer_state` guarda códigos/flags de estado de transferência por jogador, mas não contém target club + vencimento de fila.
- `career_player_commercial` guarda estado comercial/pending sale, mas não contém a tupla comprovada jogador + clube + Calendar usada por `d4()`.
- `career_active_loans` contém jogador, clube de origem, clube de destino e `expiresAtEpochMillis`; porém sua semântica comprovada é empréstimo ativo. `d4()` executa explicitamente o ramo não-loan `T1(club,0,false,false,true)`. Reusar essa tabela apenas porque também possui vencimento misturaria estados legados distintos.
- `career_squad_memberships` representa propriedade/roster atual e não uma mudança futura agendada.

Conclusão: **V14 não contém uma entidade explícita equivalente à fila `components.o2`**.

### Estruturas relacionadas que NÃO são equivalentes a `components.y1`

- `career_competitions` / standings / matches representam competição e classificação, mas não armazenam uma fila temporal com quatro acumuladores pendentes.
- `career_stadium_constructions` também possui timestamp e quatro inteiros, mas esses campos são comprovadamente de construção de estádio (`addition0..3`) e não podem ser reutilizados por semelhança estrutural.
- nenhuma outra entidade V14 expõe simultaneamente owner compatível, vencimento e quatro slots mutáveis do payload `y1`.

Conclusão: **V14 não contém uma entidade explícita equivalente à fila `components.y1`**.

## Decisão de schema

A ausência de equivalentes explícitos NÃO autoriza criar V15 ainda. Para congelar um novo schema sem inventar estado faltam, no mínimo:

1. mapear todos os construtores/callers que escrevem `components.o2` e `components.y1` no corpus oficial;
2. provar identidade/ordem necessárias para persistência e retomada;
3. provar se entradas podem coexistir, ser duplicadas ou ser substituídas;
4. provar a âncora temporal usada pelos writers e o comportamento save/reopen;
5. para `y1`, provar a identidade moderna de `best.k` e o lifecycle dos quatro acumuladores.

Até isso estar demonstrado, Room permanece **V14**, sem backfill, defaults esportivos ou destructive migration.

## Próximo gate de implementação

- manter `LegacyAnnualDeferredTransferExecutionRule` como seam de mutação já isolado;
- manter `d4()` e `e4()` como `REACHABLE_NOT_IMPLEMENTED` no nível de fila/orquestração;
- não conectar `career_active_loans`, `career_stadium_constructions` ou outra tabela apenas por similaridade de campos;
- retomar implementação somente após evidência dos writers/callers oficiais permitir definir chave, payload e lifecycle durável sem inferência.
