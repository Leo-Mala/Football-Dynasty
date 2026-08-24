# ANNUAL_MAINTENANCE_CHARACTERIZATION — Brasfoot 2026/27

## Fonte e política

Corpus oficial: `Brasfoot.apk_Decompiler.com.zip`.

SHA-256: `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`.

Esta caracterização compara diretamente Java decompilado e SMALI do novo baseline. Onde o nome obfuscado não permite provar o significado esportivo, a documentação preserva o nome legado em vez de inventar semântica.

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

## `best.b.w2()`

Java e SMALI confirmam três blocos condicionais. A rotina obtém objetos por `m(V0 ? 1 : 0)` e os religa entre pares de estruturas por `s0/p0`, `h0/w0` e `o0`. Não existe aleatoriedade ou I/O neste método.

Classificação: `JAVA_CONFIRMED_BY_SMALI`, efeito estrutural `RELINK_MODE_DEPENDENT_REFERENCES`. Os nomes esportivos dos objetos permanecem obfuscados.

## `best.b.n()`

Java e SMALI confirmam:

1. percorre `K0()` chamando `d1()`;
2. executa uma primeira passagem de manutenção `W()`/`V()` sobre referências opcionais;
3. executa uma segunda passagem `J()` sobre as mesmas famílias quando presentes.

Classificação: `JAVA_CONFIRMED_BY_SMALI`, efeito estrutural de manutenção/reset em duas passagens. Semântica esportiva específica não é inferida.

## `best.b.A1()`

Java e SMALI confirmam:

1. chama `k()` no estado central;
2. percorre `D` chamando `K0()`;
3. executa `V()` em várias estruturas `konrent.*` opcionais;
4. em seguida executa `J()` nessas mesmas famílias;
5. algumas famílias possuem uma chamada específica imediatamente antes de `J()` (`a0()`, `f0()` etc.).

Classificação: `JAVA_CONFIRMED_BY_SMALI`, efeito estrutural de manutenção anual em duas passagens. Os símbolos obfuscados não são renomeados.

## `best.a.m()`

Java e SMALI confirmam que, quando `N1()` está ativo, a rotina localiza posições do calendário por `e(...)`/`i(7)` e injeta eventos por `l(..., type, 0)`, usando os códigos de evento `8` e `6`. Os índices e branches dependem de `J1()` e de parâmetros constantes do baseline.

Classificação: `JAVA_CONFIRMED_BY_SMALI` para condições, índices e códigos. O significado nominal dos eventos `6/8` não é inferido.

## `components.y3.b()`

Java e SMALI confirmam que percorre `E0()` e, para cada entrada onde `R0()==false` e `Q0()==false`, chama `g1()`.

Classificação: `JAVA_CONFIRMED_BY_SMALI`, efeito estrutural de manutenção sobre entradas elegíveis.

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

## `best.c0.l1()` → `best.m.z()`

Java e SMALI confirmam que `best.c0.l1()` apenas verifica o campo `Y`; se ele existir, chama `Y.z()`.

A investigação transitiva de `best.m.z()` também está resolvida por Java + SMALI. O método é um reset puro, determinístico e sem I/O/aleatoriedade. Ele grava zero em dez campos numéricos do objeto `best.m`:

- seis campos `int`;
- quatro campos `long`;
- nenhum campo textual/coleção é alterado nesse método;
- nenhuma outra chamada é realizada.

Classificação de `c0.l1()`: `JAVA_CONFIRMED_BY_SMALI`.

Classificação de `m.z()`: `JAVA_CONFIRMED_BY_SMALI`, efeito estrutural `RESET_NUMERIC_COUNTERS_TO_ZERO`. Os nomes esportivos desses dez contadores não são inferidos.

## `best.a0.d()` e `best.a0.a()` — limite de determinismo

`best.a0.d()` é um orquestrador confirmado por Java + SMALI com ordem `g() -> h() -> f() -> c() -> i()`.

`best.a0.a()` também está confirmado e contém comportamento aleatório explícito: para determinados elementos de `F0()`, após checagens de presença/flags/limiar, instancia `new java.util.Random()` e executa `nextInt(100) > 30` antes de chamar `A().y()`.

A inspeção do SMALI de `best.a0` mostra ainda múltiplas outras instanciações de `java.util.Random`/`nextInt` dentro das rotinas profundas chamadas por este subsistema. Portanto:

- o legado não fornece aqui uma seed persistida reutilizável pela reconstrução moderna;
- não é correto executar esses efeitos usando `Random()` solto no domínio moderno;
- qualquer reconstrução funcional profunda deve rotear a aleatoriedade pelo `RandomSource` persistível/determinístico moderno, preservando distribuições somente quando estiverem completamente caracterizadas.

Classificação: `JAVA_CONFIRMED_BY_SMALI` para existência e posição da aleatoriedade; `DETERMINISTIC_MODERN_IMPLEMENTATION_DEFERRED` para a implementação profunda.

## Orquestração moderna segura

A Fase 5 versiona `LegacySeasonLifecycleOrder`, que representa apenas a ordem e os branches comprovados de `best.b.d()`. O modelo usa rótulos com nomes legados para os efeitos ainda obfuscados e não executa efeitos esportivos fictícios.

Testes cobrem:

- caminho padrão;
- branch `f0` (`w2` e `n`);
- branch `V0` (suprime `l()` e habilita `D1()`);
- branch `Y1()` e seleção positiva de `best.a.i(1)`.

Esse modelo é uma barreira contra reordenação acidental durante a reconstrução futura.

## Estado de paridade e fronteira da Fase 5

A Fase 5 fecha a **paridade estrutural do ciclo de temporada**: entrada pelo fim de ano, roteamento, ordem do orquestrador, reconstrução do calendário, branches principais, seleção do próximo dia jogável e catálogo dos efeitos anuais chamados diretamente pelo orquestrador estão caracterizados com Java + SMALI.

Ela não afirma paridade funcional profunda de todos os subsistemas anuais. Isso seria falso porque alguns efeitos permanecem semanticamente obfuscados e `best.a0` contém aleatoriedade não persistida no legado.

Esses efeitos profundos passam a ser backlog explícito da próxima fase, onde cada subsistema poderá ser reconstruído isoladamente com adapters e `RandomSource` moderno, sem contaminar o core com `java.util.Random()` irreproduzível.

Nenhum dado esportivo externo foi usado ou alterado.
