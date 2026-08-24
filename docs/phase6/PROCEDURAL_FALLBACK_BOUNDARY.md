# PROCEDURAL FALLBACK BOUNDARY — Fase 6

## Por que existe um boundary separado

O caminho anual comprovado contém:

`best.a0.f()` → `best.c0.n()` → `best.f.e(...)`.

Quando `f.e(...)` não encontra um jogador existente seguro para mover, o APK executa:

`best.p.d(target, position, null, 0, null, FALSE)` → `best.t.e(false, generated, target)`.

Portanto a geração procedural é alcançável pelo ciclo anual, mas é um subsistema distinto da seleção/relocalização caracterizada na Fase 6. Ela cria estado novo e contém várias fontes adicionais de aleatoriedade.

A Fase 6 não deve fingir que essa geração já foi migrada apenas porque os filtros que a antecedem foram reconstruídos.

## Evidência de `best.p.d(...)`

O Java decompilado está truncado; o SMALI oficial foi inspecionado diretamente.

A rotina cria/reutiliza um objeto `best.p` e contém os seguintes `Random.nextInt(...)` próprios:

1. `nextInt(100) + 1` — participa da escolha de um valor legado `n` por distribuição dependente de `c0.f0()/p0()`;
2. `nextInt(4) + 16` — grava um valor legado `c` entre 16 e 19;
3. `nextInt(100) + 1` — escolhe um valor legado `e`, salvo quando a posição foi explicitamente fornecida pelo caller;
4. `nextInt(6)` — branch condicional de origem/país quando condições do clube e do objeto gerado são satisfeitas;
5. `nextInt(200)` — branch condicional mais profundo no caso legado `j0()==29`;
6. `nextInt(4) + 7` — branch condicional quando a origem gerada difere do `j0()` do clube;
7. `nextInt(2)` — grava outro campo binário do objeto gerado.

Após essas decisões, `p.d(...)` chama:

- `D(c0)`;
- `e(c0)`;
- `h()`;
- `g()`.

`p.h()` acrescenta `nextInt(5)` e `p.g()` acrescenta `nextInt(100) + 1`.

Assim, uma geração completa possui pelo menos seis draws não condicionais no encadeamento `d + h + g`, além de draws condicionais cujo número depende do estado do clube e dos resultados anteriores.

## Evidência de `best.t.e(...)`

`best.t.e(false, p, c0)` converte o objeto `best.p` em `best.o`, copia vários campos, liga-o ao clube e acrescenta aleatoriedade adicional:

1. `nextInt(5)` para ajuste de um valor derivado do nível/estado do clube e de `p.p()`;
2. quando `p.v() >= 9`, `nextInt(10)` adicional;
3. se `p.l()==true`, `nextInt(3)==1` pode ativar uma flag;
4. caso contrário, `nextInt(200)==1` pode ativar a mesma flag;
5. se esse branch não ativar, `nextInt(300)==1` pode ativar outra flag e também a primeira.

A rotina então adiciona o novo `best.o` ao clube e a uma coleção global distinta conforme o parâmetro booleano.

## Por que não foi materializado na Fase 6

A geração não é apenas um sorteio de posição. Ela depende de:

- campos ainda não representados de forma completa no modelo moderno;
- distribuições condicionais por estado do clube;
- origem/país legado;
- atributos derivados por `p.D(...)` e `p.e(...)`;
- conversão `p -> o` em `t.e(...)`;
- flags e atributos aleatórios adicionais;
- múltiplos draws condicionais cuja ordem precisa ser persistível para save/reopen.

Implementar somente parte disso criaria jogadores procedurais com atributos arbitrários e violaria a regra de não inventar conteúdo esportivo.

## Estado da Fase 6

A Fase 6 fecha o que antecede esse boundary:

- ordem anual;
- thresholds e short-circuit de RNG;
- seleção `best.a0.j`;
- predicados `c0.Z0/a1/M1`;
- seleção `best.f`;
- mínimos de composição `c0.n`;
- filtros de clube/jogador doador `f.e/h`;
- filtros e fallback `best.a0.i`;
- decisão de quando a geração procedural é necessária.

Nenhuma chamada direta a `Random()` foi introduzida no domínio moderno.

## Próxima fase recomendada

**FASE 7 — MOVIMENTAÇÃO DE ELENCO E GERAÇÃO PROCEDURAL LEGADA DETERMINÍSTICA**.

Objetivo recomendado:

1. criar snapshots modernos mínimos para os campos realmente usados por `T1`, `p.d`, `p.D`, `p.e` e `t.e`;
2. reconstruir `p.d(...)` integralmente pelo SMALI, inclusive ordem dos draws condicionais;
3. rotear todos os draws por `RandomSource` persistível;
4. reconstruir `t.e(...)` sem `Random()` implícito;
5. materializar transferência/relocalização em repository transacional sem quebrar `squad_memberships`;
6. gerar fallback somente a partir da lógica comprovada do corpus;
7. testar determinismo, save/reopen, ausência de duplicidade e floors de elenco;
8. manter Room V2 se os campos puderem ser derivados/representados sem schema novo; criar V3 somente se estado realmente persistente for necessário.
