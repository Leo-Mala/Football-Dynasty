# Fase 15 — evidência do declínio anual senior `best.o.t()`

Status: **CONTROL_FLOW_IMPLEMENTED / persistence composition pending**

Corpus oficial: `Brasfoot.apk_Decompiler.com.zip` — SHA-256 `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465` — package `com.brasfoot.v2020` — versionCode `202632`.

SMALI é a autoridade executável. O Java decompilado de `best.o.t()` foi usado apenas como apoio de leitura; a regra foi congelada a partir de `best/o.smali`.

## Call path

`best.a.J(1)` comando `aj` → `best.b.p()` → sweep global de jogadores → `best.o.e()` → quando `e >= 32`, `best.o.t()`.

O sweep senior → juniores já está congelado em `LegacyAnnualPlayerProgressionSweepRules`.

## Campos provados usados por `t()`

- `e` é o campo de idade exposto por `W()` e escrito por `u1(int)`;
- `j` é a força/overall exposta por `O()` e escrita por `p1(int)`;
- `N : double` é o acumulador fracionário exposto por `X()`;
- `u0()` fornece o clube atual;
- o método lê os campos do clube por `O()`, `f0()`, `R0()` e `p0()` sem exigir nova interpretação esportiva para preservar o controle de fluxo.

## Controle executável exato

1. inicia peso com `(age - 31).toDouble()`;
2. se `club.R0() == false`, substitui o tier local:
   - `club.p0() >= 4` → tier `1`;
   - senão `club.p0() >= 3` → tier `2`;
   - senão tier `3`;
3. se `club.f0() >= 20`, subtrai `2.0` do peso;
4. multiplica conforme `j`:
   - `1..50` → `0.8`;
   - `51..70` → `1.2`;
   - `71..100` → `1.5`;
   - fora dessas faixas → peso `0.0`;
5. somente se o peso final for estritamente `> 0.0`, divide por `50.0` e soma em `N`;
6. piso de `j` por tier:
   - `1` → `35`;
   - `2` → `25`;
   - `3` → `10`;
   - qualquer outro → `1`;
7. somente se `N > 1.0` **e** `j > piso`, reduz `j` em exatamente um e subtrai exatamente `1.0` de `N`;
8. não há loop para consumir múltiplos pontos acumulados na mesma chamada;
9. se o piso bloquear o declínio, `N > 1.0` permanece acumulado;
10. ao final, `j` é limitado inferiormente a `1` incondicionalmente.

Não existe RNG dentro de `t()`.

## Implementação moderna

`LegacyAnnualSeniorDeclineRules` congela esse método como regra pura, mantendo nomes neutros para os getters de clube ainda ofuscados. Nenhuma escrita em Room ocorre nessa regra.

`LegacyAnnualSeniorDeclineRulesTest` cobre:

- derivação de tier com `R0=false`;
- efeito de `f0 >= 20`;
- multiplicadores exatos das três faixas de overall;
- threshold estrito `N > 1.0`;
- piso que bloqueia queda sem consumir `N`;
- consumo de apenas um ponto por chamada;
- clamp final de overall em `1`.

## Persistência

Esta implementação reforça — e não resolve artificialmente — o gap V14 já provado: o acumulador `N` precisa sobreviver entre execuções anuais, mas ainda não existe coluna moderna equivalente explícita.

Room permanece V14 nesta etapa. A migration seguinte continua bloqueada até o delta persistente mínimo ser fechado junto com o lifecycle de `M` e os demais estados realmente necessários. Nenhum backfill esportivo, default inventado ou destructive migration é autorizado.

## Classificação atual

- `best.o.t()` — **CONTROL_FLOW_IMPLEMENTED / TESTED; persistence composition pending**;
- `best.o.e()` — **PARTIALLY_IMPLEMENTED** porque o caminho `s()` de crescimento ainda não foi congelado integralmente;
- `best.o.N` — **durable-state gap proven**;
- Room V15 — **not yet authorized**.
