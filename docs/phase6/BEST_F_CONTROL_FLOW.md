# BEST_F_CONTROL_FLOW — Fase 6

## Fonte

Corpus oficial: `Brasfoot.apk_Decompiler.com.zip`  
SHA-256: `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`

Este documento registra o fluxo de controle de `best.f` necessário ao ciclo anual, confrontando Java e SMALI e mantendo símbolos obfuscados quando seu nome esportivo não é comprovável.

## Construtor

O construtor recebe o jogador/objeto sujeito, um valor estrutural, flags e `mode`.

### Mode 0

O bytecode preserva a ordem de avaliação:

1. testa `current.j0()==29`;
2. testa `subject.O()>50`;
3. somente então executa `new Random().nextInt(100)>10`;
4. o resultado aleatório participa de um branch posterior com `subject.O0()`/`subject.W0()`.

Isso significa que um branch estrutural pode ainda consumir RNG antes das flags posteriores serem consideradas. A projeção moderna mantém esse draw count.

### Mode 1

`subject.O0()` ou `subject.W0()` podem tornar o branch verdadeiro antes da condição com RNG; nesse caso o sorteio é short-circuited. Caso contrário, o mesmo gate `j0()==29 && O()>50 && nextInt(100)>10` é alcançado.

### Mode 2

O grupo primário é usado sem gate de construtor. A seleção posterior aplica o predicado comprovado `!M1() && p0()>=4`.

## `n(boolean)`

O topo do método contém a ternária:

`(subject.O0() && current.Q0()) ? false : subject.O() <= 30 || !current.Q0() || new Random().nextInt(100) <= 60`

Portanto:

- `subject.O0() && current.Q0()` → rota alternativa, **0 draws**;
- `subject.O() <= 30` → rota primária, **0 draws**;
- `current.Q0()==false` → rota primária, **0 draws**;
- apenas `!subject.O0() && subject.O()>30 && current.Q0()` alcança `nextInt(100)<=60` e consome **1 draw**.

### Rota primária

1. tenta `q(primary, flag)` quando primary existe;
2. se ainda não encontrou destino e `current.p0()>2`, tenta `q(secondary, flag)`.

### Rota alternativa

1. se ainda sem destino e `subject.O0()`, tenta `q(allP0, flag)`;
2. se ainda sem destino e `current.p0()>2`, tenta `q(secondary, flag)`;
3. se ainda sem destino e primary existe, tenta `q(primary, flag)`.

Ao final, se `f4186e` continua nulo, chama `p()`; caso contrário retorna a seleção de `q`.

## `o(boolean)`

Na versão oficial atual o corpo é estruturalmente simples:

`return p();`

O parâmetro booleano não altera esse corpo.

## `q(group, boolean)`

Para cada grupo, calcula inicialmente:

- `min = current.O()-1`;
- `max = current.O()+1`;
- `current.O()==1` força `min=1`.

Mode 1:

- se `current.J()!=0 || current.p0()<4 || subject.O()<40`, usa `1..2`;
- caso contrário usa `1..1`.

Se `subject.O()<=20`, o intervalo é sobrescrito para `0..group.A0()`.

Candidatos precisam:

- estar no intervalo;
- ser diferentes do current;
- ter `Q0()==false`;
- roster `<30`.

Depois do shuffle:

- mode 1 usa `a1(...)`;
- mode 2 usa `!M1() && p0()>=4`;
- demais modos usam `Z0(...)`.

O primeiro candidato aprovado é armazenado como destino e retornado.

## `p()`

Fallback global:

1. varre `E0()`;
2. mantém apenas `R0()==false`, `Q0()==false`, roster `<30`;
3. embaralha a lista;
4. escolhe o primeiro candidato que passa `Z0(...)`.

## `e(...)` e `h(...)` no caminho anual

`best.f.e(...)` é alcançado por `best.c0.n()` e, por isso, foi recuperado do SMALI. Ele monta pools de clubes doadores e usa `h(...)` para procurar um jogador seguro segundo posição, overall, flags e excedente mínimo do doador.

Quando um jogador existente é encontrado, o legado executa `T1(target, A0(), false, false, false)`.

Quando não há doador, o caminho chega ao gerador procedural `best.p.d(...)`, que permanece uma fronteira separada porque exige reconstrução completa antes de produzir atributos modernos.

## `d(...)`

A busca de callers Java + bytecode não encontrou invocação de `best.f.d(best.o,best.c0,int)` fora de sua própria definição no corpus atual.

Classificação para Fase 6:

`DECOMPILED_STUB_NOT_REQUIRED_FOR_PHASE6_ANNUAL_PATH`.

Isso não declara o método irrelevante para todo o APK; apenas impede expansão de escopo sem caller anual comprovado.

## Implementação moderna

As decisões comprovadas estão divididas em:

- `LegacyAnnualRandomRules` — bounds/thresholds/shuffle;
- `LegacyAnnualSelectionRules` — predicados, ranges, roteamento e short-circuit;
- `LegacyAnnualSquadFloorRules` — manutenção mínima/donor selection;
- `LegacyAnnualA0IRules` — facade do método SMALI-only `a0.i()`;
- `LegacyAnnualPlayerMovementRules` — plano estrutural da chamada anual `T1`.

Nenhuma dessas regras usa RNG cru no domínio moderno.
