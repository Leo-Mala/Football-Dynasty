# BEST_F_CONTROL_FLOW — Fase 6

## Fonte

Corpus oficial: `Brasfoot.apk_Decompiler.com.zip`.

SHA-256: `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`.

Arquivos confrontados:

- `sources/best/f.java`;
- `smali/best/f.smali`;
- `smali/best/a0.smali` para confirmar alcançabilidade anual.

Nenhum significado esportivo é atribuído a campos obfuscados apenas pelo nome. A caracterização abaixo preserva os símbolos do legado e descreve somente controle, filtros, mutações e chamadas comprovados.

## Alcançabilidade pelo ciclo anual

`best.a0` cria `best.f` em caminhos do subsistema anual, chama `n(false)` ou `o(false)`, consulta `g()` e, quando o resultado existe, chama `best.o.T1(best.c0, A0(), false, false, false)`.

O SMALI confirma também um fallback: quando a primeira tentativa `n(false)` não produz `g()`, uma nova instância de `best.f` pode executar `o(false)` e aplicar o mesmo `T1(...)` se houver resultado.

Classificação: `SMALI_CONFIRMED_ANNUAL_REACHABILITY`.

## Construtor — consumo de RNG não equivalente entre modos 0 e 1

### Modo 0

O bytecode calcula primeiro:

`legacyJ0 == 29 && subjectO > 50 && nextInt(100) > 10`

Somente depois lê `O0()` e `W0()`. Portanto, quando `legacyJ0 == 29 && subjectO > 50`, um draw é consumido mesmo se `O0()` ou `W0()` posteriormente fizerem o branch resultar verdadeiro por outra condição.

A expansão do grupo principal ocorre quando `legacyJ == 0` ou o branch calculado resulta verdadeiro.

### Modo 1

O branch é diferente: `O0() || W0()` torna o resultado verdadeiro antes de avaliar o predicado aleatório. Assim, nesses casos o RNG não é consumido. Apenas quando ambas as flags são falsas podem `legacyJ0 == 29`, `subjectO > 50` e `nextInt(100) > 10` ser avaliados.

### Modo 2

O conjunto principal `p()==0` é incorporado sem RNG nesse trecho.

Classificação: `JAVA_CONFIRMED_BY_SMALI`, incluindo a ordem de short-circuit e consumo de RNG.

A projeção moderna correspondente é `LegacyAnnualRandomRules.bestFConstructorExpandsPrimaryGroup(...)`.

## `best.f.n(boolean)` — roteamento e draw condicional

Java e SMALI confirmam a mesma ordem:

1. se `subjectO <= 30`, o caminho primário é escolhido sem RNG;
2. se `currentQ0 == false`, o caminho primário também é escolhido sem RNG;
3. somente quando `subjectO > 30 && currentQ0 == true` ocorre `nextInt(100) <= 60`;
4. depois disso, `subjectO0 && currentQ0` força o caminho alternativo;
5. portanto esse override pode ocorrer **depois** de um draw já consumido.

Caminho primário:

- tenta `g` por `q(...)` quando disponível;
- se ainda não houver resultado e `currentP0 > 2`, tenta `h`;
- se nada for escolhido, termina em `p()`.

Caminho alternativo:

- se ainda não houver resultado e `subjectO0`, tenta `i`;
- depois, quando `currentP0 > 2`, tenta `h`;
- depois tenta `g` quando disponível;
- se nada for escolhido, termina em `p()`.

A projeção moderna `bestFNRoute(...)` preserva a decisão e, principalmente, o número exato de draws produzido por essa ordem.

Classificação: `JAVA_CONFIRMED_BY_SMALI`.

## `best.f.q(...)` — intervalo e candidatos

O intervalo inicial é:

- `min = currentO - 1`;
- `max = currentO + 1`;
- se `currentO == 1`, `min = 1`.

No modo `1`:

- se `currentJ != 0 || currentP0 < 4 || subjectO < 40`, intervalo `1..2`;
- caso contrário, intervalo `1..1`.

Para `subjectO <= 20`, o loop redefine o intervalo por grupo para `0..groupA0`.

Um `best.c0` entra na lista temporária somente quando, ao mesmo tempo:

- `O()` está dentro do intervalo;
- não é o `current`;
- `Q0()` é falso;
- `Z().size() < 30`.

Para cada candidato elegível o legado chama `D0(true)` antes de adicioná-lo à lista temporária. Depois a lista é embaralhada.

Seleção após shuffle:

- modo `1`: primeiro candidato que satisfaz `a1(subject, !currentQ0)`;
- modo `2`: primeiro com `!M1()` e `p0() >= 4`;
- demais modos: primeiro que satisfaz `Z0(subject, !currentQ0)`.

Finalmente `f(selectedOrNull, legacyB)` grava o resultado inclusive quando nulo.

A projeção moderna atualmente congela apenas o cálculo de intervalo e o filtro estrutural, sem transportar métodos obfuscados `a1/Z0/M1` para o domínio antes de caracterizá-los.

Classificação: `JAVA_CONFIRMED_BY_SMALI`, efeito parcial implementado com barreira contra inferência.

## `best.f.p()` — fallback

`p()` constrói um pool a partir de `E0()` com os filtros:

- `R0() == false`;
- `Q0() == false`;
- `Z().size() < 30`.

O pool é embaralhado e a rotina retorna o primeiro item que satisfaz `Z0(subject, !currentQ0)`, gravando-o por `f(...)`. Se nenhum item satisfizer, retorna `null`.

A projeção moderna congela o filtro do pool e o shuffle determinístico, mas não inventa a semântica de `Z0`.

Classificação: `JAVA_CONFIRMED_BY_SMALI`.

## Efeito observável posterior em `best.a0`

Quando `best.f.g()` retorna um objeto não nulo, o caller anual executa:

`best.o.T1(selectedC0, best.o.A0(), false, false, false)`.

Isso prova que a seleção de `best.f` não é apenas cálculo: ela alimenta uma mutação no objeto `best.o`. O significado nominal de `T1` deve ser fechado separadamente por seu corpo Java/SMALI antes de ser transportado como regra esportiva moderna.

Classificação: `CALL_AND_ARGUMENTS_CONFIRMED`; `SPORTING_SEMANTICS_PENDING`.

## Invariantes modernos adicionados

`LegacyAnnualRandomRulesTest` agora protege:

- diferença de consumo de RNG entre construtor modo 0 e modo 1;
- ausência de draw quando os qualificadores do gate não são alcançados;
- modo 2 sem RNG nesse trecho;
- `n()` sem draw para `O <= 30` ou `Q0 == false`;
- override `O0 && Q0` após draw quando o gate já foi alcançado;
- limite exato `<= 60` versus `> 60`;
- ranges de `q(...)`;
- filtro de candidato de `q(...)`;
- filtro de fallback de `p()`;
- shuffle determinístico e restauração de estado RNG.

## Fronteira restante

Antes de afirmar paridade funcional profunda de `best.f`, ainda é necessário caracterizar:

- `best.c0.Z0(...)`;
- `best.c0.a1(...)`;
- `best.c0.M1()` no contexto do modo 2;
- `best.o.T1(...)` e as mutações transitivas relevantes;
- os métodos `best.f.d(...)` e `best.f.e(...)`, cujo Java está truncado e exige SMALI caso sejam comprovadamente necessários no caminho anual.

Até essa fronteira ser fechada, o domínio moderno preserva somente efeitos e decisões comprovados, sem atribuir nomes esportivos arbitrários.
