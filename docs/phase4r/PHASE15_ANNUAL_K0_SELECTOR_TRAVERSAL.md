# Fase 15 — traversal interno de `best.k0.c(index)`

Status: **SELECTOR_TRAVERSAL_IMPLEMENTED_AND_TESTED / INNER_STATE_STILL_OPEN**

Corpus oficial: `Brasfoot.apk_Decompiler.com.zip` — SHA-256 `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465` — package `com.brasfoot.v2020` — versionCode `202632`.

SMALI é a autoridade executável. Este checkpoint promove somente evidência já congelada na auditoria da Fase 15; não inventa semântica para `components.n1` ou `best.h0`.

## Evidência executável já congelada

O caminho alcançável `best.b.F()` chama `best.k0.c(index)` para cada entry de competição. A auditoria SMALI já estabeleceu que `k0.c(index)`:

1. executa seu setup via `U()`;
2. cria estado `best.h0`;
3. percorre `components.n1` na sequência fixa de seletores:
   `[0, 1, 2, 2, 5, 6, 6, 3, 3, 4, 4]`;
4. seleciona jogadores por thresholds e alimenta coleções internas;
5. possui ao menos um ramo com efeito adicional de flag de jogador.

A sequência possui 11 chamadas e as duplicatas fazem parte do comportamento comprovado; não devem ser deduplicadas, ordenadas ou normalizadas.

## Implementação moderna

`LegacyAnnualTournamentEntryResetRules` congela exclusivamente a ordem/multiplicidade da travessia de seletores. A regra retorna ações ordenadas com `ordinal + selector` e não atribui significado esportivo não comprovado aos valores 0..6.

As regressões verificam:

- sequência exata `[0,1,2,2,5,6,6,3,3,4,4]`;
- exatamente 11 chamadas;
- ordinais contíguos 0..10;
- duplicatas preservadas exatamente nas posições comprovadas.

## Limites ainda abertos

Este checkpoint **não** promove `best.k0.c(index)` inteiro para implementado. Permanecem em investigação:

- thresholds e inputs completos de cada `components.n1`;
- identidade/cardinalidade das coleções em `best.h0`;
- ramo de flag adicional de jogador;
- quais desses estados sobrevivem ao save/load;
- equivalência ou ausência de equivalência no runtime moderno V14.

Portanto a classificação agregada passa de `REACHABLE_NOT_IMPLEMENTED` para **PARTIALLY_IMPLEMENTED / SELECTOR_TRAVERSAL_FROZEN**.

## Persistência

Room permanece em V14. Esta regra não introduz novo estado persistente, migration, backfill, default esportivo ou RNG.

Uma eventual V15 continua bloqueada até o lifecycle durável de `j0`, `d/W0`, `M`, `N` e dos estados relevantes de `k0.c(index)` estar provado de forma agregada.
