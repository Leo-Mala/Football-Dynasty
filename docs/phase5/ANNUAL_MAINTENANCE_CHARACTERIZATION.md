# ANNUAL_MAINTENANCE_CHARACTERIZATION — Brasfoot 2026/27

## Fonte e política

Corpus oficial: `Brasfoot.apk_Decompiler.com.zip`.

SHA-256: `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`.

Esta caracterização compara diretamente `sources/best/b.java` com `smali/best/b.smali`. Onde o nome obfuscado não permite provar o significado esportivo, a documentação preserva o nome legado em vez de inventar semântica.

## `best.b.s()`

Java e SMALI coincidem estruturalmente:

1. percorre `this.h`;
2. limpa `s0()` de cada entrada;
3. chama `l()`;
4. quando `Q0()` é falso, chama `L0()`, `v()` e `w()`;
5. quando `y0()` existe, aplica `g(50)` e `h(50)`;
6. depois percorre `g1()` e chama `p()`.

Classificação: `JAVA_CONFIRMED_BY_SMALI` para chamadas, condições e ordem. O significado esportivo integral de `h`, `Q0`, `y0`, `g(50)` e `h(50)` permanece `SEMANTICS_PARTIAL`.

## `best.b.D()`

Java e SMALI coincidem na bifurcação estrutural:

- quando `F` existe e não está vazio, percorre `L0()` e chama `V()` seguido de `J()`;
- caso contrário, percorre `J0()`, consulta dados da temporada anterior (`J() - 1`), cria `konrent.h0` usando `F0()`, adiciona ao estado central `F` e chama `V()` no novo objeto.

Classificação: `JAVA_CONFIRMED_BY_SMALI`. A rotina manipula histórico/estado derivado por temporada, mas nomes esportivos mais específicos não são afirmados sem evidência adicional.

## `best.b.r()`

Java e SMALI confirmam:

1. para cada item de `J0()`, chama `o0()` e depois `J()`;
2. limpa para `false` a flag `a1` de todos os elementos encontrados em `z0()`;
3. para cada item de `J0()`, exceto o branch especial `F0() == 29 && Y1()`, reconstrói uma lista a partir de `z0().N0()`;
4. acrescenta elementos de `T0()` ordenados por `components.f3.A` apenas quando ainda não estão presentes;
5. grava a lista por `W(arrayList)`;
6. no branch `F0() == 29 && Y1()`, chama `e1(null)`.

Classificação: `JAVA_CONFIRMED_BY_SMALI` para o algoritmo e a exceção de código 29. O significado factual do código legado `29` permanece deliberadamente não nomeado.

## `best.b.o()`

Java e SMALI confirmam:

1. percorre `G0()` e chama `b0()`;
2. percorre novamente `G0()`;
3. em cada `k0()`, redefine `a1(false)`.

Classificação: `JAVA_CONFIRMED_BY_SMALI`. A rotina é um reset anual de uma segunda família de estruturas, sem atribuição esportiva não comprovada.

## `best.b.q()`

Java e SMALI coincidem na seguinte sequência:

1. percorre `this.g` e chama `d()`;
2. cria listas temporárias `D1`, `F1`, `E1`;
3. para cada item de `E0()`, limpa `F1/E1`, percorre `a0()` e chama `p.c(owner)`;
4. remove de `a0()` os itens acumulados em `F1` e adiciona os acumulados em `E1`;
5. limpa `F1/E1`;
6. percorre `this.f` e chama `Y0()`;
7. remove entradas de `this.f` cujo `u0()` seja `null`;
8. adiciona `D1` a `this.f`;
9. chama `n2()`;
10. define `D1 = null`;
11. chama `i2(true)`;
12. percorre `D0()` e chama `p()`;
13. finaliza com `components.y3.b()`.

Classificação: `JAVA_CONFIRMED_BY_SMALI` para estrutura, mutações diferidas e ordem. O significado de cada coleção permanece parcialmente obfuscado.

## `best.b.y1()`

Java e SMALI confirmam que o método obtém duas listas de índices por `best.a.j()` e `best.a.f(1)`, escolhe uma delas conforme `i0`, e materializa eventos via `best.a.l(index, type, 0)` usando tipos `2`, `5` e `3`.

Classificação: `JAVA_CONFIRMED_BY_SMALI` para seleção de listas, branch e códigos de evento. O significado nominal dos tipos `2/3/5` não é inferido.

## `best.b.Y3()`

Java e SMALI confirmam:

1. cria lista temporária de pares `best.b.c`;
2. incorpora elementos de `D0()` quando `T() > 0`, usando `T()` e `b0()`;
3. incorpora todos os pares de `i0()`;
4. ordena por `K1`;
5. define `C1 = 1`;
6. se houver mais de 200 elementos, define `C1` a partir do item de índice 199.

Classificação: `JAVA_CONFIRMED_BY_SMALI`. Isso prova uma seleção de limiar/ranking anual, sem nome esportivo adicional.

## `best.b.P0()`

Java e SMALI confirmam que a rotina varre o calendário `W()` a partir do índice atual `d` e seleciona o primeiro dia que simultaneamente:

- não esteja processado (`!I()`);
- tenha `E() > 0`;
- tenha ao menos um item em `A()`.

Ao encontrar, chama `T2(index)` e retorna o índice; se não encontrar, retorna `0`.

Este comportamento coincide com o contrato moderno já expresso em `LegacyCalendarRules.selectNextPlayableDay()`.

Classificação: `JAVA_CONFIRMED_BY_SMALI` e `MODERN_PARITY_PRESENT` para o predicado estrutural.

## `best.c0.l1()`

Java e SMALI confirmam que `l1()` apenas verifica o campo `Y`; se não for nulo, chama `Y.z()`.

Classificação: `JAVA_CONFIRMED_BY_SMALI`. O efeito transitivo de `best.m.z()` ainda deve ser caracterizado antes de portar essa manutenção para o domínio moderno.

## Orquestração moderna segura

A Fase 5 passa a versionar `LegacySeasonLifecycleOrder`, que representa apenas a ordem e os branches comprovados de `best.b.d()`. O modelo usa rótulos com nomes legados para os efeitos ainda obfuscados e não executa efeitos esportivos fictícios.

Testes cobrem:

- caminho padrão;
- branch `f0` (`w2` e `n`);
- branch `V0` (suprime `l()` e habilita `D1()`);
- branch `Y1()` e seleção positiva de `best.a.i(1)`.

Esse modelo é uma barreira contra reordenação acidental durante a reconstrução futura.

## Estado de paridade

Com esta etapa, a ordem de `best.b.d()` e as estruturas internas de `s()`, `D()`, `r()`, `o()`, `q()`, `y1()`, `Y3()`, `P0()` e `c0.l1()` estão confirmadas por Java + SMALI.

Ainda não é correto declarar paridade completa dos efeitos anuais porque permanecem pendentes, entre outros:

- efeito transitivo de `best.m.z()`;
- semântica detalhada das coleções obfuscadas manipuladas pelas rotinas acima;
- `w2()`, `n()`, `A1()`, `best.a.m()`, `a0.d()`, `a0.a()`, `D1()` e `components.y3.b()`;
- efeitos de carreira que precisem ser persistidos além do estado moderno já existente.

Nenhum dado esportivo externo foi usado ou alterado.
