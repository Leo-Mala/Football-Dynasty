# Fase 15 — evidência do RNG do crescimento anual senior `best.o.s()`

Status: **RNG_BRANCH_IMPLEMENTED / full growth control flow pending**

Corpus oficial: `Brasfoot.apk_Decompiler.com.zip` — SHA-256 `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465` — package `com.brasfoot.v2020` — versionCode `202632`.

SMALI é a autoridade executável. O Java decompilado de `best.o.s()` é um stub `UnsupportedOperationException`, portanto a implementação usa exclusivamente o método real em `smali/best/o.smali`.

## Call path

`best.a.J(1)` comando `aj` → `best.b.p()` → sweep global de jogadores → `best.o.e()` → caminho de crescimento → `best.o.s()`.

O sweep senior antes dos juniores já está congelado em `LegacyAnnualPlayerProgressionSweepRules`; o caminho de declínio `best.o.t()` já possui regra pura e testes.

## Site RNG e condição exata

O trecho SMALI relevante ocorre depois de o target de crescimento já ter sido limitado pelo cap específico do clube. O registrador usado no guard contém `0x3c`, portanto a condição é exatamente:

`best.o.d0 >= 60`

Somente nesse caminho é construído `java.util.Random` e executado:

`nextInt(5)`

A ordem executável provada é:

1. se `d0 < 60`, não há draw e o target já limitado permanece inalterado;
2. se `d0 >= 60`, o APK consome **sempre exatamente um** `nextInt(5)`;
3. o draw é consumido mesmo quando `m` não está entre 7 e 10;
4. para `m == 7`, soma `5 + draw`;
5. para `m == 8`, soma `15 + draw`;
6. para `m == 9`, soma `25 + draw`;
7. para `m == 10`, soma `30 + draw`;
8. para qualquer outro `m`, o draw já consumido não altera o target;
9. depois dessa etapa, target acima de `100` é limitado a `100`.

Esse consumo aparentemente inútil para `m` fora de 7..10 é um quirk executável e precisa ser preservado porque altera o estado RNG das decisões seguintes.

## Implementação moderna

`LegacyAnnualRandomRules.bestOSGrowthDraw(RandomSource)` mantém o draw unitário `nextInt(5)`.

`LegacyAnnualRandomRules.bestOSApplyHighD0CapAdjustment(...)` congela o branch completo descrito acima usando o RNG stateful/persistível moderno e nomes neutros para os campos ofuscados ainda não promovidos a semântica esportiva.

Regressões em `LegacyAnnualRandomRulesTest` comprovam:

- `d0 == 59` não consome RNG;
- `d0 == 60` consome exatamente um draw;
- o draw também é consumido quando `m` não recebe bônus;
- bônus exatos `5/15/25/30 + draw` para `m=7/8/9/10`;
- clamp final em `100`;
- bound exato `5`;
- save/restore do `StatefulJavaRandomSource` reproduz exatamente o mesmo draw e snapshot posterior.

## Limite deliberado

Esta mudança **não declara `best.o.s()` completo**. Continuam abertos:

- a tradução integral do cálculo anterior do incremento fracionário de `N`;
- a tabela completa de caps por `club.J()/club.j0()` já visível no SMALI;
- composição do flag durável `best.o.M`;
- escrita transacional de `M/N` no runtime persistido;
- integração final com `best.o.e()`.

Portanto Room permanece V14 e nenhuma migration é autorizada por esta mudança isolada.

## Classificação atual

- `best.o.s(): d0>=60 → nextInt(5) → m bonus → clamp 100` — **IMPLEMENTED_AND_TESTED**;
- `best.o.s()` — **PARTIALLY_CHARACTERIZED / REACHABLE_NOT_IMPLEMENTED**;
- `best.o.M` — **persistent-runtime gap proven**;
- `best.o.N` — **durable-state gap proven**;
- Room V15 — **not yet authorized**.
