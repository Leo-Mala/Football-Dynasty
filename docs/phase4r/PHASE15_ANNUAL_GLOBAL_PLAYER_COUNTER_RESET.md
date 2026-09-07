# Fase 15 — reset anual global de `best.o.j0`

Status: **IMPLEMENTED_AND_TESTED / PERSISTENCE_MAPPING_OPEN**

Corpus oficial: `Brasfoot.apk_Decompiler.com.zip` — SHA-256 `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465` — package `com.brasfoot.v2020` — versionCode `202632`.

SMALI permanece a autoridade executável.

## Evidência congelada

No fluxo alcançável de `best.b.F()`, o segundo passe percorre a coleção global de jogadores e chama `best.o.d1(0)` em cada jogador. O SMALI de `best.o.d1(I)` é um setter direto do inteiro `j0`.

Portanto esse passe possui um efeito exato e isolável:

`j0 = 0`

Ele não grava `d/W0`, não depende de clube, idade, competição ou RNG e ocorre antes do terceiro passe de `F()`, no qual `best.o.D0()` volta a incrementar `j0` somente nos jogadores pertencentes a `z0()[0].h()` de cada competição.

## Implementação moderna

`LegacyAnnualPlayerD0Rules.resetGlobalCounter(...)` projeta exatamente `best.o.d1(0)` preservando todos os demais inputs e substituindo somente `legacyJ0` por zero.

A mesma regra continua contendo a projeção já certificada de `best.o.D0()`; não foi criada equivalência persistente artificial para `j0`/`d`.

## Regressões

`LegacyAnnualPlayerD0RulesTest` agora prova também:

- qualquer `legacyJ0` é zerado;
- `legacyW0`, clube, idade e códigos do clube permanecem inalterados;
- reset quando `j0 == 0` é idempotente;
- nenhuma aleatoriedade é consumida.

## Persistência

Room permanece V14. O runtime persistido ainda não possui campo comprovadamente equivalente a `j0`, portanto esta implementação fecha a regra pura mas não autoriza integração transacional final de `best.b.F()` nem Room V15.

Não criar default/backfill para `j0`. A eventual persistência continua dependente do lifecycle completo de `j0`, `d/W0`, `M`, `N` e dos estados ainda abertos de `best.k0.c(index)`.

## Classificação

- `best.o.d1(0)` → **IMPLEMENTED_AND_TESTED**;
- `best.o.D0()` control-flow → **IMPLEMENTED_AND_TESTED / PERSISTENCE_MAPPING_OPEN**;
- `best.b.F()` → **PARTIALLY_IMPLEMENTED / PERSISTENCE_AND_K0_GAPS_OPEN**;
- Room V14 → **PRESERVAR**.

A matriz agregada da Fase 15 deve refletir esta promoção no próximo checkpoint de consolidação, sem promover `F()` inteiro enquanto os gaps persistentes e `k0.c(index)` permanecerem abertos.
