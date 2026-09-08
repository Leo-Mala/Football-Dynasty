# Fase 16 — auditoria de durabilidade da carreira

Status: **ACTIVE / MARCO D**

Baseline de integração certificado: `256689d0dfe6788c4c939607efab07b9ba341447` (`phase4/core-game-domain`).

FINAL_HEAD certificado da Fase 15: `3e53331a4fa0706f63340d42472034d535e2cfdd`.

Corpus oficial único: `Brasfoot.apk_Decompiler.com.zip` — SHA-256 `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465` — package `com.brasfoot.v2020` — versionCode `202632`.

## Objetivo

Provar que todo estado moderno necessário para continuar uma carreira sobrevive corretamente a:

`criar → jogar → salvar → fechar → reabrir → continuar → virar temporada`

com RNG atômico, isolamento entre carreiras, rollback/transação e sem inventar estado legado.

A Fase 16 **não reabre regras esportivas certificadas na Fase 15**. O foco é ownership, durabilidade, reconstrução segura e persistência.

## Política de schema

Room está em **V15** no início do Marco D.

Não criar V16 até uma lacuna persistente ser provada por reader/writer/lifecycle e até ficar demonstrado que o estado não pode ser reconstruído de forma determinística e sem perda a partir de owners V15 existentes.

Qualquer futura V16 deve ser mínima, aditiva, exportada e testada, sem `fallbackToDestructiveMigration`, `UPDATE`/backfill inventado ou default esportivo.

## Inventário inicial de ownership V15

| SUBSISTEMA / ESTADO | OWNER V15 | STATUS INICIAL | EVIDÊNCIA / AÇÃO |
|---|---|---|---|
| carreira core / calendário / temporada | `career_core_state` | `DURABLE_OWNER_PRESENT` | inclui season/calendar/day/transition state; exigir reopen e ciclos repetidos |
| RNG da carreira | `career_core_state.rngInitialSeed/rngInternalState/rngDraws` | `DURABLE_OWNER_PRESENT` | provar atomicidade junto a todas as mutações multi-store |
| jogador runtime senior | `career_player_runtime` | `DURABLE_OWNER_PRESENT` | inclui energia/overall/flags e V15 `legacyAnnualM/N/rawPayrollN`; exigir reopen/fail-closed |
| jogadores procedurais + membership | `career_procedural_players` + `career_squad_memberships` | `DURABLE_OWNER_PRESENT` | exigir continuidade e isolamento entre carreiras |
| estatísticas jogador×clube×temporada `best.e` | `career_player_club_season_stats` | `DURABLE_OWNER_PRESENT` | `c..h` persistidos; legado `i` é transient e continua omitido |
| partidas agendadas/resultados | `career_scheduled_matches` | `DURABLE_OWNER_PRESENT` | exigir reopen antes/depois de commit e idempotência |
| liga `konrent.t` mínima | `career_competitions` + standings + competition matches | `PARTIAL_DURABLE_PROJECTION` | round/table/match links existem; auditar agregados serializáveis derivados abaixo |
| mercado/contratos/comercial | transfer/commercial/membership owners existentes | `DURABLE_OWNER_PRESENT_NEEDS_CYCLE_TEST` | validar compra/venda/continuidade/rollback no ciclo completo |
| finanças/manager | `career_club_manager_runtime` + stores | `DURABLE_OWNER_PRESENT_NEEDS_ATOMICITY_AUDIT` | TransitionSeason já reseta ledger em transação local; auditar composição multi-store |
| empréstimos | `career_active_loans` | `DURABLE_OWNER_PRESENT` | exigir reopen/virada repetida |
| estádio | construction/runtime entities | `DURABLE_OWNER_PRESENT` | exigir reopen/rollback |
| tickets | club/manager ticket runtimes | `DURABLE_OWNER_PRESENT` | exigir continuidade |
| treinador | coach runtime + season records | `DURABLE_OWNER_PRESENT` | exigir troca/demissão/reopen |
| juniores | `career_junior_draft` + player/runtime owners | `DURABLE_OWNER_PRESENT_CERTIFIED_IN_15_1` | não reabrir sem regressão; incluir em ciclo agregado |
| `best.n.g` | nenhum Room owner | `INTENTIONALLY_TRANSIENT` | Fase 15 provou lifecycle estático/sessão e clear explícito; não persistir |
| senior `best.o.j0` | nenhum Room owner | `INTENTIONALLY_TRANSIENT` | campo legado é `transient`; não persistir só por compatibilidade |
| `best.k0` / `components.n1` / `best.h0` | nenhum owner agregado comprovado | `DURABILITY_GAP_NEEDS_OWNERSHIP_PROOF` | primeiro alvo do Marco D: provar reconstrução vs persistência mínima |
| `konrent.a0` / `konrent.f0` | competição/match projection parcial | `DURABILITY_GAP_NEEDS_RECONSTRUCTION_PROOF` | comparar todo estado serializado mutável com schedule/round owners atuais |
| legado `.a26/.s26` end-to-end | fixture real suficiente não provada neste checkpoint | `FIXTURE_LIMITED_NOT_CLAIMED` | não criar decoder falso-verde; manter limitação explícita até evidência real |

## Primeira lacuna em investigação — `best.k0/components.n1/best.h0`

A Fase 15 congelou funcionalmente `best.k0.c(index)` e provou que `best.k0`, `components.n1` e `best.h0` são serializáveis no legado. Isso por si só não autoriza novas tabelas: a Fase 16 precisa determinar se o estado serializado é derivável de owners modernos já persistidos.

### Evidência executável já recuperada

No corpus oficial:

- `best.k0.a(best.o)` obtém/cria o `components.n1` do jogador;
- lê o valor por partida do jogador (`best.o.y0()`), chama `components.n1.f(double)` e portanto acumula **soma**, **contagem** e **média**;
- escreve também o selector bruto via `components.n1.h(int)`;
- o único caller corpus-wide localizado de `best.k0.a(best.o)` está em `best.o.n(best.s,int,int)`, no fluxo de encerramento/avaliação da partida;
- `best.o.n(...)` também mantém o valor por partida e histórico correspondente antes de alimentar `k0`.

A persistência moderna V15 de partida grava runtime do jogador e os seis contadores serializáveis `best.e.c..h`, mas **não possui owner identificado para a acumulação `components.n1` soma/contagem/média/selector**.

Classificação provisória: **RECONSTRUCTION_NOT_PROVEN**. Próxima ação obrigatória: mapear integralmente writers/readers/reset de `k0.g/h/i`, identidade do `n1`, lifecycle de `h0` e verificar se o valor de partida equivalente a `best.o.y0()` já existe de forma reproduzível no runtime moderno. Só então decidir se V16 é necessária.

## Segunda lacuna em investigação — `konrent.a0/f0`

A Fase 15 congelou o bootstrap e os argumentos exatos de construção `a0/f0`. V15 persiste partidas agendadas, links de competição, rodada e ordinal, mas ainda não há prova agregada de que **todo** estado serializado mutável de `a0/f0` seja reconstruível dessas projeções.

Classificação provisória: **RECONSTRUCTION_NOT_PROVEN**. Auditar campos serializados, readers/writers, estado de rodada/fixture, flags e lifecycle antes de criar qualquer owner novo.

## Gates internos do Marco D

A Fase 16 só fecha após evidência para, no mínimo:

1. core + RNG save/reopen com fingerprint idêntico;
2. partida/competição save/reopen antes e depois da resolução;
3. elenco, membership, procedural players, mercado/contratos save/reopen;
4. finanças/manager/estádio/loans/tickets/coach save/reopen;
5. juniores no ciclo agregado sem regressão;
6. duas carreiras simultâneas sem vazamento de estado;
7. rollback em falhas de transação sem avanço parcial de gameplay ou RNG;
8. múltiplas viradas de temporada com continuidade;
9. toda lacuna `RECONSTRUCTION_NOT_PROVEN` resolvida como `RECONSTRUCTIBLE_AND_TESTED`, `DURABLE_OWNER_IMPLEMENTED_AND_TESTED` ou limitação explicitamente autorizada pelo roadmap;
10. migration V16 apenas se o delta persistente mínimo for comprovado;
11. FINAL_HEAD com auditoria agregada, 0 blockers/review threads e todos os workflows obrigatórios verdes no mesmo SHA.

## Não objetivos

- não redesenhar gameplay;
- não atualizar dados esportivos;
- não reabrir Fase 15.1 ou regras da Fase 15 sem regressão;
- não alegar compatibilidade completa de save legado sem fixture real suficiente;
- não usar instalação limpa/reset de carreira como solução de persistência.
