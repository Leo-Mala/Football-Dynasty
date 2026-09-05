# Fase 15 — gap de progressão anual de jogadores seniores

Status: **REACHABLE_NOT_IMPLEMENTED / N persistence gap proven**

Corpus oficial: `Brasfoot.apk_Decompiler.com.zip` — SHA-256 `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465` — package `com.brasfoot.v2020` — versionCode `202632`.

SMALI é a autoridade executável.

## 1. Call path alcançável

O comando anual `aj` em `best.a.J(1)` chama `best.b.p()`.

`best.b.p()` preserva esta ordem:

1. percorre a lista global `best.b.f` de jogadores e chama `best.o.e()` em cada um;
2. somente depois percorre os clubes/juniores e chama `best.p.b()` em cada draft.

O segundo sweep está certificado pela Fase 15.1. A ordem senior → juniores já está congelada em `LegacyAnnualPlayerProgressionSweepRules`.

## 2. `best.o.e()`

O SMALI mostra:

- se `u0()` (clube atual) é nulo, retorna sem executar progressão e sem limpar `M`;
- se campo `e < 32`, chama privado `s()`;
- caso contrário chama privado `t()`;
- após um desses caminhos grava diretamente `M = Boolean.FALSE`.

Logo `best.o.e()` é mutação anual de jogador, não presentation-only.

## 3. Identidade dos campos `M` e `N`

A auditoria corpus-wide fecha agora a identidade operacional desses dois campos:

- `S()` é getter direto de `M`;
- `s1(Boolean)` é setter direto de `M`;
- `X()` é getter direto de `N : double`;
- `v1(double)` é setter direto de `N`;
- construtores inicializam `M=false` e `N=0.0`;
- materialização/promoção em `best.t` grava explicitamente `N=0.0`;
- `s()` e `t()` leem/escrevem `N`;
- `s()` lê `M` e `e()` o limpa depois da progressão quando existe clube.

Também foi provado que `M1(Boolean)` **não** escreve `M`: `M1` escreve o campo `d`, cujo getter é `W0()`. Esse estado pertence ao lifecycle separado de `D0()`.

## 4. Lifecycle alcançável de `M`

O campo `M` não nasce exclusivamente na progressão. Writers externos alcançáveis incluem:

- `ActivityEscalacao` em caminho de seleção/escalação;
- `components.y3` em caminho de seleção relacionado;
- `best.s` em fluxo de substituição, marcando o jogador que entra.

Esses callers gravam `s1(TRUE)`. O campo também é lido em lógica de jogador, incluindo `best.o.r()`, e é consumido por `s()` antes de ser limpo por `e()`.

Consequência: `M` é estado de gameplay real relacionado ao uso/seleção do jogador. Porém **ainda não está provado que ele exige coluna Room própria**, porque sua equivalência pode ser derivável de estado moderno de escalação/partida já persistido. Essa equivalência deve ser investigada antes de criar schema novo.

## 5. Caminho `s()` para `e < 32`

O método calcula uma taxa fracionária de progressão usando estado do clube, idade/faixa do jogador e vários campos do próprio `best.o`.

A evidência executável mostra explicitamente:

- taxas base como `0.16`, `0.12`, `0.10`, `0.08`, `0.06`, `0.04`, `0.02` conforme faixas;
- modificadores adicionais/penalidades associados a estado do clube e campos do jogador;
- leitura de `M` antes de ele ser limpo por `e()`;
- acumulação no campo `N : double`;
- quando `N > 1.0` e os guards permitem, incremento do campo `j` e decremento de `N` em `1.0`;
- `j` é limitado superiormente a `100`;
- existe ramo com `new Random().nextInt(5)` quando o campo `d0` atinge o threshold comprovado, alterando o teto intermediário segundo o campo `m`.

O Java decompilado não é suficiente para `s()`; esse caminho continua SMALI-required. O RNG implícito não pode ser tratado como bit-for-bit equivalente à seed moderna sem prova.

## 6. Caminho `t()` para `e >= 32`

O caminho de envelhecimento/decréscimo usa o mesmo `N : double`:

- calcula uma fração a partir de idade, força `j`, clube/divisão e outros guards;
- soma a fração em `N`;
- quando `N > 1.0` e `j` está acima do piso calculado, decrementa `j` em um e subtrai `1.0` de `N`;
- limita `j` inferiormente a `1`.

Assim, `N` é relevante tanto para crescimento quanto para declínio e precisa sobreviver entre execuções anuais para preservar o threshold estrito legado.

## 7. Estado moderno V14

`CareerPlayerRuntimeEntity` V14 contém atualmente:

- `age`;
- `overall`;
- `marketValue`;
- flags `star/worldTop`;
- `legacyHash`, `legacyGeneratedO`, `legacyCreatedYear`;
- contrato e previous market value;
- flags `legacyQ/X/Y/Z`;
- `energy` e `injuryUntilEpochDay`.

`CareerPlayerRuntimeMapper` também não inicializa nenhum acumulador equivalente a `N`, e `CareerPlayerRuntimeStore` não possui operação anual equivalente a `best.o.e()`.

Não existe campo V14 explicitamente equivalente ao acumulador `N`. Esse é um **gap de estado durável comprovado**: `N` influencia a mutação futura de `j` e não pode ser reconstruído apenas do `overall` atual.

Para `M`, a ausência de coluna explícita é real, mas a necessidade de uma nova coluna ainda depende do confronto com o estado moderno de escalação/substituição.

## 8. Decisão de persistência

Esta evidência ainda **não congela Room V15**. Antes de alterar schema é obrigatório:

1. fechar o mapa executável dos campos `e`, `j`, `m`, `d0` usados por `s()/t()` e confrontar cada um com colunas modernas;
2. confrontar o lifecycle de `M` com a persistência moderna de escalação/substituição para decidir se é derivável ou precisa ser armazenado;
3. preservar `N` como requisito durável sem inventar backfill esportivo para saves V14;
4. congelar a política moderna para o ramo legado `new Random().nextInt(5)` usando `RandomSource`, preservando bound/draw order sem alegar equivalência de seed não demonstrada;
5. somente então implementar regra anual + migration aditiva, save/reopen e rollback se o delta persistente continuar comprovado.

Não usar default esportivo inventado, backfill inferido ou destructive migration.

## 9. Classificação

- `best.b.p()` — **PARTIALLY_IMPLEMENTED**: sweep/order implementado; junior certificado; mutação senior pendente;
- `best.o.e()` — **REACHABLE_NOT_IMPLEMENTED**;
- `best.o.s()` — **SMALI_REQUIRED / CHARACTERIZED_PARTIAL**;
- `best.o.t()` — **CHARACTERIZED**;
- `best.o.M` — **CHARACTERIZED lifecycle / persistence equivalence open**;
- `best.o.N` — **CHARACTERIZED / V14 durable-state gap proven**;
- `best.o.M1/W0` — estado separado `d`, não confundir com `M`.

## 10. Próximo passo

Fechar `e/j/m/d0` e o ramo RNG de `s()`, auditar a equivalência de `M` com o estado moderno de escalação, e só então congelar a extensão persistente mínima necessária para `N` e demais campos realmente ausentes.
