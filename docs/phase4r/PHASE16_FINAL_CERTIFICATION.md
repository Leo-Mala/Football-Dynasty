# Fase 16 — certificação final do Marco D

Status: **FINAL_HEAD_CANDIDATE / RECERTIFICATION_REQUIRED**

Este documento consolida o fechamento técnico da Fase 16 sem substituir a auditoria histórica em `PHASE16_DURABILITY_AUDIT.md`.

## Baseline e escopo

- branch de integração: `phase4/core-game-domain`;
- baseline do Marco D: `256689d0dfe6788c4c939607efab07b9ba341447`;
- branch do Marco D: `milestone-d/durable-career`;
- Draft PR: #17;
- corpus oficial único: `Brasfoot.apk_Decompiler.com.zip`;
- SHA-256 do corpus: `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`;
- package legado: `com.brasfoot.v2020`;
- versionCode legado: `202632`.

A Fase 16 é o Marco D do roadmap e trata exclusivamente de durabilidade completa da carreira, sem reabrir regras esportivas já certificadas.

## Candidato funcional certificado antes deste documento

O candidato funcional imediatamente anterior a este commit documental é:

`575dcdae57ac1befd4cd86c9be505878cae82821`

Nesse exact SHA, os três workflows obrigatórios terminaram em SUCCESS:

- Phase 7 Validation #1055 — run `34175831186`;
- Phase 8 Validation #867 — run `34175831166`;
- Phase 8 Final Certification #678 — run `34175831194`.

A criação deste documento produz um novo HEAD e, portanto, a certificação final do Marco D deve ser repetida no novo exact SHA. Runs antigos não certificam o novo HEAD.

## Room / schema final do Marco D

O schema corrente é **V17**. Esse número de schema pertence à Fase 16 e não representa a Fase 17 do roadmap.

O delta V17 agrupa somente estado legado comprovadamente não reconstruível após save/reopen:

- snapshots serializados de competição/rodada equivalentes a `best.h0/k0.h/i`;
- histórico anual de nota por partida equivalente a `best.o.V/components.s2`;
- winner explícito de desempate equivalente a `best.s.N0()/A0()`;
- stable insertion order de `best.k0.g`;
- índice legado real da competição;
- `legacyGroupCountA0` proveniente de `LoadLigaOptions.nGrupos`.

`components.z2` permanece **TRANSIENT** e não possui owner Room. O buffer existe somente durante o fechamento da rodada e é descartado após produzir o snapshot durável comprovado.

Também permanecem transient e fora do Room os estados já provados como tais, incluindo `best.o.j0`, `best.n.g` e `n2`.

As migrations permanecem explícitas, aditivas e não destrutivas. Não existe `fallbackToDestructiveMigration`, backfill esportivo inventado ou default de gameplay criado para preencher saves antigos.

## Fechamento de `konrent.t.l0()`

O último elo funcional comprovado da V17 foi integrado no boundary atômico de fechamento de rodada:

1. ratings individuais são avaliados preservando a ordem comprovada;
2. o aggregate de competição respeita `ratingY0 > 0`, competição presente e `competition.E() == 1`;
3. `konrent.t.a0(player)` é representado por captura transient equivalente a `components.z2`;
4. o último jogo da rodada produz o snapshot durável equivalente a `best.h0`;
5. o comparator preserva média decrescente e, em empate, count decrescente;
6. efeitos laterais comprovados, incluindo star/annual history, são aplicados na ordem testada;
7. o buffer transient é limpo após o snapshot;
8. gameplay persistido e RNG da carreira continuam atômicos no commit de partida.

RNG implícito legado recuperado para esse fluxo permanece separado do RNG persistido da carreira; nenhum stream foi fundido sem prova de equivalência.

## Evidência de durabilidade coberta

O conjunto do Marco D cobre por implementação e regressão:

- core da carreira + RNG save/reopen;
- partida agendada, resolução e commit atômico;
- standings/competição/rodada e snapshots persistentes necessários;
- estado de jogador senior e procedural;
- memberships/elencos;
- contratos/mercado e movimentações persistentes já integradas pelos marcos anteriores;
- finanças/manager;
- estádio, empréstimos, tickets e treinador;
- juniores V14 sem reabertura da Fase 15.1;
- histórico anual de ratings;
- desempate com winner explícito;
- isolamento entre carreiras;
- rollback sem avanço parcial de gameplay/RNG;
- save → close → reopen → continue;
- migrations históricas atravessando o schema corrente V17;
- migration 16→17 e schema `17.json` gerado pelo tooling Room real.

## Compatibilidade legado `.a26/.s26`

Continua **não declarada end-to-end** porque não há fixture real suficiente comprovada para sustentar essa afirmação. Essa limitação é intencional e segue o roadmap: não existe decoder falso-verde nem default inventado para simular compatibilidade.

## Auditoria final do PR

Antes de marcar Ready, o exact HEAD final deve satisfazer simultaneamente:

- PR #17 ainda apontando para `milestone-d/durable-career` sobre `phase4/core-game-domain`;
- mergeable e sem conflitos;
- zero review threads materiais;
- ausência de alteração factual esportiva;
- ausência de gameplay/regra/competição inventada;
- ausência de raw RNG moderno proibido fora dos boundaries autorizados;
- ausência de destructive migration;
- schema V17 e migrations verificadas;
- testes unitários com zero failures/errors/skips exigidos pelos gates;
- corpus/fixture integrity verde;
- `assembleDebug` verde;
- Phase 7 Validation, Phase 8 Validation e Phase 8 Final Certification SUCCESS no **mesmo exact SHA**.

## Gate do Marco D

O critério do roadmap permanece:

`criar → jogar → salvar → fechar → reabrir → continuar → virar temporada`

com fingerprints/determinismo, isolamento e rollback validados.

Quando a recertificação do commit documental final terminar verde no mesmo exact SHA, o Marco D pode ser classificado como **IMPLEMENTED_AND_CERTIFIED / READY_FOR_INTEGRATION**, mantendo a restrição explícita de não fazer merge em `main` enquanto a instrução do usuário assim determinar.

A Fase 17 / Marco E só pode começar após o fechamento formal e a integração permitida do Marco D.