# FASE 6 — RELATÓRIO FINAL DE IMPLEMENTAÇÃO

## Baseline

- base certificada: `phase4/core-game-domain@343b115219824255d46ed538ef9519c3c92b33e4`;
- esse commit corresponde ao merge concluído da Fase 5;
- corpus oficial: `Brasfoot.apk_Decompiler.com.zip`;
- SHA-256: `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`;
- package legado: `com.brasfoot.v2020`;
- versionCode: `202632`.

## Objetivo efetivamente fechado

A Fase 6 reconstrói a fronteira determinística dos efeitos anuais profundos que a Fase 5 havia deixado semanticamente/aleatoriamente aberta.

Ela não copia a arquitetura legado. Em vez disso, transforma decisões comprovadas por Java/SMALI em regras Kotlin puras com `RandomSource` explícito e testes de caracterização.

## Implementação moderna adicionada

### `LegacyAnnualRandomRules`

- `best.a0.a()` — `nextInt(100) > 30`;
- `best.a0.i()` — `nextInt(100) > 25`;
- oito sites de `best.a0.j(...)`: thresholds `10,90,30,30,35,45,75,95`;
- `best.f` construtor — `>10`;
- `best.f.n()` — `<=60` apenas no branch que efetivamente alcança o sorteio;
- shuffle Fisher–Yates por `RandomSource`.

### `LegacyAnnualSelectionRules`

- `best.c0.M1()`;
- `best.c0.a1(...)`;
- `best.c0.Z0(...)`;
- thresholds `j0.f4269d0/f4272e0/f4275f0`;
- escolha de posição em `best.a0.j(...)`;
- filtro do jogador selecionável;
- roteamento modo 0/1 e `n/o` preservando consumo sequencial de RNG;
- short-circuit diferente entre construtor `best.f` modos 0 e 1;
- short-circuit de `best.f.n()` que evita RNG em `O0 && Q0`;
- ranges e filtros de `best.f.q(...)`;
- seleção de modo 2 por capacidade + `p0()`;
- fallback `best.f.p()`.

### `LegacyAnnualSquadFloorRules`

Reconstrói `best.a0.f() -> c0.n() -> f.e()/h()` no nível de regras comprovadas:

- execução apenas para `Q0()==false`;
- mínimos por posição `2/3/3/5/3`;
- uma tentativa por posição deficiente em `c0.n()`;
- faixa de clubes doadores;
- igualdade `J()` no pool global;
- faixa de overall `±5`, clamp `5..100`;
- exclusão de flags `O0/W0`;
- excedente mínimo do clube doador `3/4/4/6/4`.

### `LegacyAnnualA0IRules`

Reconstrói o método Java truncado `best.a0.i()` usando SMALI:

- filtro de clube por `Q0/J/p0`;
- filtro de jogador por `O()>50`, `W()<31`, `O0()==true`;
- gate `nextInt(100)>25` somente após filtros estruturais;
- `best.f` modo `2`;
- tentativa `n(false)` seguida de `o(false)` somente quando a primeira não resolve.

A facade usa `LegacyAnnualSelectionRules` como fonte única dos predicados compartilhados, evitando drift entre subsistemas.

### `LegacyAnnualA0OrchestrationRules`

- tier de chamadas de `best.a0.b(...)`: 1/2/3/4 pares de passagens conforme índice;
- primeiro loop de `best.a0.a()`;
- segundo loop e short-circuit antes do gate `>30`;
- ação de `best.a0.c()` (`NONE`, `CALL_H1`, `SET_S0_FALSE`).

### `LegacyAnnualPlayerMovementRules`

Modela estruturalmente a chamada anual comprovada:

`best.o.T1(destination, A0(), false, false, false)`.

Java + SMALI confirmam, entre outros efeitos:

- relink do jogador para o destino;
- resets `X=false` e `Z=false`;
- `Y` não alterado por esse call shape;
- cálculo percentual secundário igual a zero porque o primeiro booleano é `false`;
- chamadas de código `1` condicionadas a `A0()>0` e `Q0()`;
- argumento estrutural `180L`;
- `Q1()`;
- limpeza/removal da origem quando ela existe;
- inclusão no destino;
- efeitos extras quando origem e destino são ambos `Q0()`.

A Fase 6 congela esse plano, mas não grava uma versão parcial no Room enquanto todos os campos dinâmicos afetados por `T1` não estiverem representados com equivalência comprovada.

## Engenharia reversa concluída nesta fase

Foram confrontados diretamente Java e SMALI de:

- `best.a0`;
- `best.f`;
- `best.c0`;
- `best.o.T1(...)`;
- `best.p.d(...)` no boundary de fallback;
- `best.t.e(...)` no boundary de conversão procedural.

A busca de bytecode confirmou que `best.f.d(best.o,best.c0,int)` não possui caller comprovado no corpus e, portanto, não é necessário para o caminho anual desta fase.

`best.f.e(...)`, por outro lado, possui caller anual comprovado por `c0.n()` e foi recuperado do SMALI.

## Determinismo

O domínio moderno continua proibindo:

- `java.util.Random` direto;
- `kotlin.random.Random` direto;
- `ThreadLocalRandom`;
- `Math.random()`.

Toda regra aleatória reconstruída recebe `RandomSource`.

Testes preservam:

- draw count;
- short-circuit;
- thresholds;
- repetibilidade;
- snapshot/restore do RNG;
- shuffle determinístico;
- filtros de seleção;
- limites de elenco;
- seleção profunda de `best.a0.j()/best.f`;
- plano estrutural anual de `T1`.

## Persistência / Room

Room permanece **V2**.

Nenhum estado novo persistente foi demonstrado como necessário para as regras puras desta fase. O estado RNG da carreira já está representado por `career_core_state`.

Não foi introduzido `fallbackToDestructiveMigration`.

## Integridade esportiva

Nenhum jogador, clube, rating, atributo, elenco ou competição factual foi atualizado por fonte externa.

O único corpus esportivo de referência permanece o ZIP Brasfoot 2026/27 fixado na Fase 4R.

A Fase 6 não cria jogadores procedurais parciais ou inventados.

## Boundary explícito — geração procedural

Quando `f.e(...)` não encontra doador, o legado executa `best.p.d(...) -> best.t.e(...)`.

Esse encadeamento contém várias fontes adicionais de RNG, construção de atributos, origem, flags e conversão de objeto. Ele foi auditado o suficiente para provar que é um subsistema próprio, mas não foi parcialmente copiado para o domínio moderno.

Detalhes: `docs/phase6/PROCEDURAL_FALLBACK_BOUNDARY.md`.

Isso não é tratado como falha escondida da Fase 6: é a fronteira técnica que define a Fase 7.

## Gate final obrigatório

Antes de merge, o **head exato deste relatório e de todo o código da Fase 6** deve obter `Phase 6 Validation = SUCCESS`.

O workflow exige, no mesmo head:

- guard contra RNG cru;
- `gradlew help`;
- KSP + Room schema V2;
- `testDebugUnitTest`;
- `LegacyCareerCalendarCharacterizationTest`;
- `LegacySeasonLifecycleOrderTest`;
- `LegacyAnnualRandomRulesTest`;
- `LegacyAnnualSelectionRulesTest`;
- `LegacyAnnualSquadFloorRulesTest`;
- `LegacyAnnualA0IRulesTest`;
- `LegacyAnnualA0OrchestrationRulesTest`;
- `LegacyAnnualJAndBestFDeepTest`;
- `LegacyAnnualPlayerMovementRulesTest`;
- benchmark/evidência de performance existente;
- integridade SHA-256 da fixture Brasfoot 2026;
- `assembleDebug`;
- schemas Room V1/V2;
- ausência de destructive migration.

O resultado factual do gate final deve ser lido diretamente dos checks do PR #5. Este documento não será alterado apenas para copiar o número do run, evitando mudar o head depois da certificação.

## Política de revisão com Codex indisponível

O limite mensal de Codex do proprietário está esgotado. A Fase 6 não declara `Codex approved`.

A revisão final é substituída, de forma explícita e não enganosa, por:

- auditoria independente do diff;
- comparação Java ↔ SMALI;
- testes de caracterização;
- GitHub Actions no head exato;
- revisão de Room, RNG, integridade e escopo.

Codex indisponível não será mascarado como sucesso de Codex.

## Próxima fase

**FASE 7 — MOVIMENTAÇÃO DE ELENCO E GERAÇÃO PROCEDURAL LEGADA DETERMINÍSTICA**.

A Fase 7 deve partir somente do merge certificado desta fase e reconstruir integralmente `T1`, `best.p.d`, `p.D`, `p.e` e `best.t.e`, com RNG persistível e repository transacional, sem inventar jogadores ou atributos.
