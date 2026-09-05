# Fase 15 — gap de progressão anual de jogadores seniores

Status: **PARTIALLY_IMPLEMENTED / M+N persistence gaps proven**

Corpus oficial: `Brasfoot.apk_Decompiler.com.zip` — SHA-256 `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465` — package `com.brasfoot.v2020` — versionCode `202632`.

SMALI é a autoridade executável. Este documento consolida somente evidência já congelada no próprio branch; ele não inventa o trecho ainda não caracterizado de `best.o.s()`.

## 1. Call path alcançável

O comando anual `aj` em `best.a.J(1)` chama `best.b.p()`.

`best.b.p()` preserva esta ordem:

1. percorre a lista global `best.b.f` de jogadores e chama `best.o.e()` em cada um;
2. somente depois percorre os clubes/juniores e chama `best.p.b()` em cada draft.

O sweep senior → juniores está congelado em `LegacyAnnualPlayerProgressionSweepRules`. O segundo sweep permanece certificado pela Fase 15.1.

## 2. Routing de `best.o.e()`

O SMALI já congelado prova:

- se `u0()` (clube atual) é nulo, retorna sem executar progressão e sem limpar `M`;
- se campo `e < 32`, chama privado `s()`;
- caso contrário chama privado `t()`;
- após um desses caminhos grava diretamente `M = Boolean.FALSE`.

Esse routing foi promovido para `LegacyAnnualSeniorProgressionRoutingRules` e testado nos boundaries de clube ausente, idade 31 e idade 32.

Classificação do caller: **IMPLEMENTED_AND_TESTED**.

## 3. Identidade e durabilidade de `M` e `N`

A auditoria corpus-wide separa definitivamente os dois campos:

- `S()` é getter direto de `M`;
- `s1(Boolean)` é setter direto de `M`;
- `X()` é getter direto de `N : double`;
- `v1(double)` é setter direto de `N`;
- construtores inicializam `M=false` e `N=0.0`;
- materialização/promoção em `best.t` grava explicitamente `N=0.0`;
- `s()` e `t()` leem/escrevem `N`;
- `s()` lê `M` e `e()` o limpa depois da progressão quando existe clube.

`M1(Boolean)` **não** escreve `M`: ele escreve o campo separado `d`, lido por `W0()` e usado no lifecycle de `D0()`.

### `M`

Writers alcançáveis em escalação/substituição gravam `s1(TRUE)`. O moderno possui `selectedOrUsed` durante a partida, mas esse latch não atravessa o boundary final para persistência por jogador. Um save/reopen entre uso e progressão anual perderia a informação.

Classificação: **PERSISTENT_RUNTIME_GAP_PROVEN**.

### `N`

`N` é acumulador fracionário compartilhado por crescimento e declínio. O V14 não possui coluna equivalente e o valor não pode ser reconstruído do `overall` atual.

Classificação: **PERSISTENT_RUNTIME_GAP_PROVEN**.

## 4. Caminho de declínio `best.o.t()` (`e >= 32`)

O controle executável de `t()` já está congelado em `LegacyAnnualSeniorDeclineRules`.

A implementação/testes preservam:

- peso inicial derivado de idade;
- ajuste quando o getter de clube caracterizado atinge o threshold legado;
- multiplicadores por faixa de força;
- acumulação em `N`;
- pisos caracterizados;
- condição estrita `N > 1.0`;
- redução de no máximo um ponto por chamada;
- preservação do acumulador quando o piso bloqueia;
- clamp inferior de `j` em 1.

Classificação: **IMPLEMENTED_AND_TESTED**.

## 5. Caminho de crescimento `best.o.s()` (`e < 32`)

O Java decompilado não oferece corpo executável confiável; o método continua SMALI-required.

### Bloco RNG high-`d0`

Já está congelado que:

- `d0 < 60` → zero draws;
- `d0 >= 60` → exatamente um `nextInt(5)`;
- o draw acontece antes da decisão por `m` e é consumido mesmo para valores de `m` sem bônus;
- `m=7/8/9/10` adiciona respectivamente `5/15/25/30 + draw` ao cap previamente calculado;
- o cap resultante é limitado a 100.

Esse boundary está implementado em `LegacyAnnualRandomRules` com `RandomSource` explícito e regressões de draw count/order.

Classificação: **IMPLEMENTED_AND_TESTED**.

### Bloco final de crescimento/cap

`LegacyAnnualSeniorGrowthFinalizationRules` congela o final já provado do método:

1. usa o cap previamente calculado;
2. aplica o ajuste high-`d0` acima;
3. avalia `N > 1.0` estrito;
4. somente com `j < 100` entra no ramo de crescimento/cap;
5. se `j < effectiveCap`, incrementa `j` exatamente uma vez e subtrai `1.0` de `N`;
6. se o cap impedir crescimento, grava `N = 1.0` exatamente;
7. `j >= 100` deixa `N` intacto;
8. clamp final de `j` em 100.

Classificação: **IMPLEMENTED_AND_TESTED**.

### Bloco precedente ainda aberto

Ainda não foi promovido o trecho de `s()` que deriva integralmente:

- a contribuição fracionária adicionada a `N`;
- todos os guards/modificadores que usam `M` e demais campos já mapeados;
- o cap de clube antes do ajuste high-`d0`.

Taxas observadas no SMALI já foram registradas (`0.16`, `0.12`, `0.10`, `0.08`, `0.06`, `0.04`, `0.02`), mas a associação completa dessas taxas a todos os branches ainda não está congelada em regra moderna. Não inferir esse mapa sem o corpus executável.

Classificação do método completo: **PARTIALLY_IMPLEMENTED**.

## 6. Estado moderno V14

`CareerPlayerRuntimeEntity` V14 cobre idade, overall, market value, contrato, flags já mapeados, energia e lesão, mas não possui equivalentes persistentes explícitos para `M` e `N`.

Room permanece em **V14**.

Não existe autorização para criar V15 enquanto o bloco precedente de `best.o.s()` e os demais gaps persistentes do lifecycle anual ainda não estiverem fechados em um mapa agregado.

## 7. Decisão de persistência

A próxima migration deve permanecer bloqueada até que seja possível definir o delta mínimo sem inventar estado histórico para saves V14.

Requisitos antes de qualquer V15:

1. fechar o restante executável de `best.o.s()`;
2. fechar o mapa agregado dos estados persistentes ainda abertos de `F()/D0()`;
3. definir como representar `M` e `N` em saves migrados sem backfill esportivo inventado;
4. preservar atomicidade entre mutação de jogador, RNG e carreira;
5. adicionar migration explícita, schema exportado, 14→15, save/reopen e rollback somente quando esse delta estiver provado.

`fallbackToDestructiveMigration`, reset de save e defaults esportivos inferidos continuam proibidos.

## 8. Classificação atual

- `best.b.p()` — **PARTIALLY_IMPLEMENTED**: sweep/order implementado; junior certificado; mutação senior ainda incompleta;
- `best.o.e()` routing — **IMPLEMENTED_AND_TESTED**;
- `best.o.s()` completo — **PARTIALLY_IMPLEMENTED / SMALI_REQUIRED**;
- `best.o.s()` high-`d0` RNG — **IMPLEMENTED_AND_TESTED**;
- `best.o.s()` final growth/cap — **IMPLEMENTED_AND_TESTED**;
- `best.o.t()` — **IMPLEMENTED_AND_TESTED**;
- `best.o.M` — **PERSISTENT_RUNTIME_GAP_PROVEN**;
- `best.o.N` — **PERSISTENT_RUNTIME_GAP_PROVEN**;
- `best.o.M1/W0` — estado separado `d`, pertencente ao lifecycle de `D0()`.

## 9. Próximo passo

Reabrir `smali/best/o.smali` do corpus oficial e congelar, sem inferência, o bloco precedente de `best.o.s()` que calcula incremento fracionário e cap. Só depois consolidar `M/N` com os demais estados anuais e decidir se/como Room V15 deve ser criada.
