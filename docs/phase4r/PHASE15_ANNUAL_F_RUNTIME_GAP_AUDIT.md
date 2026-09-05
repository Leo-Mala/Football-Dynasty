# Fase 15 — auditoria de equivalência runtime de `best.b.F()`

Status: **D0 CONTROL FLOW IMPLEMENTED / K0 SELECTOR TRAVERSAL IMPLEMENTED / persistence mapping still open**

Corpus oficial: `Brasfoot.apk_Decompiler.com.zip` — SHA-256 `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465` — package `com.brasfoot.v2020` — versionCode `202632`.

SMALI é a autoridade executável. Esta atualização corrige uma ambiguidade anterior: `best.o.M1(Boolean)` não grava o campo anual `M`; ele grava o campo Boolean `d`, lido por `best.o.W0()`.

## 1. Estado de jogador exigido por `best.o.d1(0)` / `best.o.D0()`

O SMALI prova diretamente:

- `d1(int)` é setter direto de `j0`;
- `W0()` retorna o Boolean `d`;
- `M1(Boolean)` grava exatamente esse mesmo Boolean `d`;
- `D0()` incrementa `j0` antes de qualquer guard;
- se `d/W0` já é true, ou `j0 < 2`, ou não existe clube, ou idade `e >= 35`, não há latch novo;
- com `club.J() == 0`, os códigos `club.j0()` 1, 65 e 97 gravam `d=true` a partir de `j0 >= 2`;
- ainda com `club.J() == 0`, os códigos 104, 72 e 154 exigem `j0 >= 3`;
- com `club.J() != 0`, somente `club.j0() == 29` pode gravar `d=true`, exigindo `j0 >= 4`.

Portanto o estado de `D0()` está agora identificado como o par **`j0` + `d/W0`**. Ele é distinto do campo `M` usado pela progressão anual senior.

`LegacyAnnualPlayerD0Rules` congela esse controle de fluxo e os thresholds exatos sem atribuir significado esportivo não comprovado aos nomes ofuscados. As regressões cobrem incremento incondicional, guards, os três grupos de códigos e os thresholds 2/3/4.

O `CareerPlayerRuntimeEntity` V14 persiste atualmente idade, overall, market value, `star/worldTop`, hash/geração/ano, contrato/valor anterior, flags `legacyQ/X/Y/Z`, energia e lesão. Não há coluna já comprovada como equivalente a `j0` nem a `d/W0`.

Consequência: **o control-flow de `D0()` está implementado, mas sua composição no runtime persistido ainda depende do mapping semântico de `j0` e `d/W0`**. Não reutilizar `legacyQ/X/Y/Z` por semelhança nominal.

## 2. Campo `M` não pertence a `D0()`

A inspeção corpus-wide separa claramente os dois estados:

- `S()` retorna `M`;
- `s1(Boolean)` grava `M`;
- caminhos de escalação/substituição (`ActivityEscalacao`, `components.y3`, `best.s`) gravam `M=true`;
- `best.o.e()` lê `M` dentro da progressão e o limpa para false depois de `s()`/`t()` quando há clube.

Assim, documentos anteriores que tratavam `M1(TRUE)` como writer do campo `M` devem ser considerados superseded por esta evidência SMALI.

## 3. Estado de tournament exigido por `best.k0.c(index)`

A evidência SMALI já congelada prova que `k0.c(index)` não é um simples reset de rodada. Ele chama `U()`, cria `best.h0` e percorre `components.n1` na sequência fixa:

`[0, 1, 2, 2, 5, 6, 6, 3, 3, 4, 4]`.

Esse traversal contém exatamente 11 chamadas e as duplicatas fazem parte do comportamento executável. O moderno agora congela essa ordem e multiplicidade em `LegacyAnnualTournamentEntryResetRules`, com regressões específicas para sequência, ordinal e duplicatas.

Isso **não** fecha `best.k0.c(index)` inteiro. Permanecem sem promoção para gameplay:

- thresholds e inputs completos de cada `components.n1`;
- identidade e cardinalidade das coleções produzidas em `best.h0`;
- ramo adicional com efeito de flag de jogador;
- lifecycle/persistência dos estados internos produzidos pelo método.

O runtime moderno V14 de competição (`CareerCompetitionEntity`, `CareerCompetitionStandingEntity`, `CareerCompetitionMatchEntity`) persiste somente a projeção já comprovada de tipo/formato, rodada, standings e partidas. `CareerCompetitionStore` não contém estrutura já provada equivalente às coleções produzidas por `k0.c(index)` / `components.n1`.

Consequência: **`best.k0.c(index)` é PARTIALLY_IMPLEMENTED / SELECTOR_TRAVERSAL_FROZEN**, mas ainda não é seguro tratá-lo como equivalente ao avanço/reset de rodada existente nem inventar a semântica interna restante.

## 4. Decisão de persistência

Esta auditoria comprova ausência de equivalência V14 já mapeada, mas **não autoriza Room V15 ainda**.

Uma migration só poderá ser introduzida se a investigação provar suficientemente:

1. lifecycle durável e readers/writers de `j0`;
2. lifecycle durável e readers/writers de `d/W0`;
3. quais estados produzidos por `k0.c(index)` sobrevivem ao save/load e são necessários para continuidade;
4. cardinalidade, identidade e ordem dessas estruturas;
5. ausência de outra representação moderna já existente após o mapping completo.

Não criar defaults/backfills esportivos, não mapear por semelhança de nome e não usar destructive migration.

## 5. Classificação atualizada

- `best.b.F()` — **PARTIALLY_IMPLEMENTED / PERSISTENCE_AND_K0_GAPS_OPEN**;
- `best.o.d1(0)` — **CHARACTERIZED**;
- `best.o.D0()` — **CONTROL_FLOW_IMPLEMENTED / PERSISTENCE_MAPPING_OPEN**;
- `best.o.M1(Boolean)` / `best.o.W0()` — **CHARACTERIZED** como setter/getter do campo `d`;
- `best.o.M` — **SEPARATE_STATE**, pertencente à progressão/escalação e não ao latch `D0()`;
- `best.k0.c(index)` — **PARTIALLY_IMPLEMENTED / SELECTOR_TRAVERSAL_FROZEN**;
- Room V14 — **PRESERVAR** até a semântica completa justificar alteração.

## 6. Próxima investigação obrigatória

1. fechar corpus-wide todos os readers/writers de `j0` e `d/W0` e confrontar com runtime moderno;
2. continuar `best.k0.c(index)` a partir dos thresholds/inputs de `components.n1`, identidade/cardinalidade de `best.h0` e ramo de flag, sem refazer o traversal já certificado;
3. em paralelo, continuar a auditoria separada de `best.o.M/N` da progressão anual senior;
4. somente então implementar + persistir o delta comprovado.

## 7. Checkpoint de disponibilidade do corpus

O traversal acima é evidência já congelada e certificada no branch. Novos detalhes internos de `components.n1`/`best.h0` exigem reabertura do SMALI oficial. Na ausência temporária do ZIP bruto no ambiente de execução, nenhum threshold, cardinalidade, significado esportivo ou efeito persistente adicional deve ser promovido por inferência.
