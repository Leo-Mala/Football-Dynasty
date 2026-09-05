# Fase 15 — gap de progressão anual de jogadores seniores

Status: **REACHABLE_NOT_IMPLEMENTED / persistence gap proven**

Corpus oficial: `Brasfoot.apk_Decompiler.com.zip` — SHA-256 `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465` — package `com.brasfoot.v2020` — versionCode `202632`.

SMALI é a autoridade executável.

## 1. Call path alcançável

O comando anual `aj` em `best.a.J(1)` chama `best.b.p()`.

`best.b.p()` preserva esta ordem:

1. percorre a lista global `best.b.f` de jogadores e chama `best.o.e()` em cada um;
2. somente depois percorre os clubes/juniores e chama `best.p.b()` em cada draft.

O segundo sweep está certificado pela Fase 15.1. Este documento trata do primeiro sweep, que permanecia sem equivalência moderna comprovada.

## 2. `best.o.e()`

O SMALI mostra:

- se `u0()` (clube atual) é nulo, retorna sem executar evolução;
- se campo `e < 32`, chama privado `s()`;
- caso contrário chama privado `t()`;
- depois do caminho executado grava sempre `M = Boolean.FALSE`.

Logo `best.o.e()` é mutação anual de jogador, não presentation-only.

## 3. Caminho `s()` para `e < 32`

O método calcula uma taxa fracionária de progressão usando estado do clube, idade/faixa do jogador e vários flags/atributos persistidos do próprio `best.o`.

A evidência executável mostra explicitamente:

- taxas base como `0.16`, `0.12`, `0.10`, `0.08`, `0.06`, `0.04`, `0.02` conforme faixas;
- modificadores adicionais/penalidades associados a estado do clube e campos do jogador;
- leitura de `M` antes de ele ser limpo por `e()`;
- acumulação no campo `N : double`;
- quando `N > 1.0` e os guards permitem, incremento do campo `j` e decremento de `N` em `1.0`;
- `j` é limitado superiormente a `100`;
- existe ramo com `new Random().nextInt(5)` quando o campo `d0` atinge o threshold comprovado, alterando o teto intermediário segundo o campo `m`.

Portanto o caminho jovem/sênior inicial possui **acumulador fracionário durável `N`** e também contém RNG implícito do APK em condição alcançável. O RNG não pode ser antecipadamente conectado à seed stateful moderna como se houvesse paridade comprovada.

## 4. Caminho `t()` para `e >= 32`

O caminho de envelhecimento/decréscimo também usa o mesmo acumulador `N : double`:

- calcula uma fração a partir de idade, força `j`, clube/divisão e outros guards;
- soma a fração em `N`;
- quando `N > 1.0` e `j` está acima do piso calculado, decrementa `j` em um e subtrai `1.0` de `N`;
- limita `j` inferiormente a `1`.

Assim, `N` é relevante tanto para crescimento quanto para declínio e precisa sobreviver entre execuções anuais para preservar o threshold estrito legado.

## 5. Estado moderno V14

`CareerPlayerRuntimeEntity` V14 contém atualmente:

- `age`;
- `overall`;
- `marketValue`;
- flags `star/worldTop`;
- `legacyHash`, `legacyGeneratedO`, `legacyCreatedYear`;
- contrato e previous market value;
- flags `legacyQ/X/Y/Z`;
- `energy` e `injuryUntilEpochDay`.

Não existe campo equivalente explícito para:

- o acumulador fracionário legado `best.o.N : double`;
- o flag legado `best.o.M : Boolean`, que influencia `s()` e é limpo por `e()`.

`CareerPlayerRuntimeStore` igualmente não possui operação anual que leia/persista esses dois estados e aplique `best.o.e()`.

## 6. Consequência de persistência

Diferente dos antigos falsos candidatos `components.o2/y1`, aqui existe um **gap de estado durável real**: o acumulador `N` influencia a mutação futura de `j` e não pode ser reconstruído apenas do `overall` atual.

Porém esta evidência ainda **não congela Room V15**. Antes de alterar schema é obrigatório:

1. fechar o mapa executável dos campos `e`, `j`, `m`, `d0`, `M`, `N`, `W0/O0` usados por `s()/t()`;
2. confirmar qual deles já corresponde semanticamente a colunas V14 e qual está realmente ausente;
3. caracterizar a origem/lifecycle do flag `M` e todos os seus writers/readers;
4. congelar a política de RNG implícito do ramo `new Random().nextInt(5)` sem alegar equivalência de seed não demonstrada;
5. somente então decidir migration aditiva, teste de save/reopen e rollback.

Não usar default esportivo inventado, backfill inferido ou destructive migration.

## 7. Classificação

- `best.b.p()` — `PARTIALLY_IMPLEMENTED`: sweep junior certificado; sweep senior pendente;
- `best.o.e()` — `REACHABLE_NOT_IMPLEMENTED`;
- `best.o.s()` — `SMALI_REQUIRED / CHARACTERIZED_PARTIAL`, RNG implícito + estado fracionário comprovados;
- `best.o.t()` — `CHARACTERIZED`, estado fracionário `N` comprovado;
- persistência do acumulador `N` — **gap V14 comprovado**, schema action ainda bloqueada por mapeamento completo de campos.

## 8. Próximo passo

Executar uma auditoria corpus-wide dos getters/setters e writers de `best.o.M/N/e/j/m/d0`, confrontar com `CareerPlayerRuntimeMapper`/`CareerPlayerRuntimeEntity`, e só então implementar a regra anual + persistência atômica se todos os campos estiverem inequívocos.
