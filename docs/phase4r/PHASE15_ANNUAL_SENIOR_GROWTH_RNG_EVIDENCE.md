# Fase 15 — evidência do RNG do crescimento anual senior `best.o.s()`

Status: **RNG_BOUNDARY_IMPLEMENTED / full growth control flow pending**

Corpus oficial: `Brasfoot.apk_Decompiler.com.zip` — SHA-256 `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465` — package `com.brasfoot.v2020` — versionCode `202632`.

SMALI é a autoridade executável. A caracterização anterior de `best.o.s()` provou um site explícito `new java.util.Random().nextInt(5)` no caminho alcançável de crescimento anual senior.

## Call path

`best.a.J(1)` comando `aj` → `best.b.p()` → sweep global de jogadores → `best.o.e()` → caminho de crescimento → `best.o.s()`.

O sweep senior antes dos juniores já está congelado em `LegacyAnnualPlayerProgressionSweepRules`; o caminho de declínio `best.o.t()` já possui regra pura e testes.

## RNG provado nesta etapa

O site de `best.o.s()` consome uma chamada inteira com bound exato `5`. Esta etapa congela somente esse fato executável:

`nextInt(5)`

Não foi atribuída semântica esportiva ao valor retornado nem foi congelada uma condição de branch que ainda não esteja integralmente provada pelo SMALI.

## Implementação moderna

`LegacyAnnualRandomRules.bestOSGrowthDraw(RandomSource)` expõe a chamada como boundary explícito e persistível. O objetivo é impedir consumo oculto de RNG e permitir que a composição futura de `best.o.s()` preserve ordem de draws dentro do RNG stateful da carreira.

Regressões adicionadas em `LegacyAnnualRandomRulesTest` comprovam:

- bound `5`;
- exatamente um draw por chamada;
- valor retornado preservado;
- save/restore do `StatefulJavaRandomSource` reproduz exatamente o mesmo draw e o mesmo snapshot posterior.

## Limite deliberado

Esta mudança **não declara `best.o.s()` implementado**. Continuam abertos:

- ordem completa dos guards e mutações do crescimento;
- papel preciso do flag `best.o.M` no caminho de crescimento;
- composição do acumulador durável `best.o.N`;
- condição exata em torno do site `nextInt(5)`;
- escrita transacional no runtime persistido.

Portanto Room permanece V14 e nenhuma migration é autorizada por esta mudança isolada.

## Classificação atual

- site RNG `best.o.s(): nextInt(5)` — **IMPLEMENTED_AND_TESTED as explicit RNG boundary**;
- `best.o.s()` — **PARTIALLY_CHARACTERIZED / REACHABLE_NOT_IMPLEMENTED**;
- `best.o.N` — **durable-state gap proven**;
- Room V15 — **not yet authorized**.
