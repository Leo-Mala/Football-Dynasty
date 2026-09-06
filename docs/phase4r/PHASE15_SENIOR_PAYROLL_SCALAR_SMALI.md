# Fase 15 — scalar de folha senior `best.o.o()` / `best.o.n`

Status: **IMPLEMENTED_AND_TESTED / DURABLE OWNER STILL OPEN**

Fonte factual única: `Brasfoot.apk_Decompiler.com.zip`, SHA-256 `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`, package `com.brasfoot.v2020`, versionCode `202632`. O SMALI oficial é a autoridade executável.

## Correção de tipagem do estado senior

A reabertura byte a byte de `smali/best/o.smali` confirma as declarações reais:

- `M:Ljava/lang/Boolean;` — serializado;
- `N:D` — serializado;
- `n:I` — serializado;
- `d:Ljava/lang/Boolean;` — serializado;
- `j0:I` — explicitamente `transient`.

Portanto `j0` não deve ser materializado em Room como estado durável. `M` e `d` são `Boolean` legado, não inteiros/booleans primitivos inventados.

## Fórmula executável de `best.o.o()`

Quando `u0()` é nulo, o método retorna sem escrever `n`. Quando existe clube, o SMALI calcula o raw `n` com esta ordem exata:

1. escolhe um scalar por `club.V0()` + `club.O()`;
2. soma 50 somente quando `club.f0() > 20`;
3. aplica o ajuste por raw `player.g`;
4. executa `Math.round(scalar * 0.5)` e estreita o `long` para `int`;
5. multiplica `player.j * 2 * halfRounded` com aritmética JVM `int`;
6. quando raw `player.e >= 32`, subtrai `(e - 32) * 300`;
7. quando `player.c || player.d`, soma `player.j * 250`;
8. aplica piso 500;
9. quando `player.d`, executa `Math.round(value * 1.4)` e estreita para `int`;
10. quando `best.b.V1()` é verdadeiro, multiplica por 4;
11. grava o resultado em `best.o.n`.

A matriz inicial é:

| `club.V0()` | `club.O()` | scalar |
|---|---:|---:|
| true | 1 | 750 |
| true | 2 | 550 |
| true | 3 | 500 |
| true | 4/5 | 450 |
| true | outro | 350 |
| false | 1 | 600 |
| false | 2 | 500 |
| false | 3 | 450 |
| false | 4/5 | 400 |
| false | outro | 350 |

Ajuste raw `g`: `0 -> -70`, `1 -> -30`, `2 -> -40`, `4 -> -50`, demais -> `0`.

## Implementação moderna

`LegacySeniorPayrollScalarRules` congela somente essa projeção numérica. Os nomes ofuscados são preservados e nenhuma semântica esportiva externa é atribuída aos campos. Os testes cobrem:

- ambas as matrizes `V0/O`;
- threshold estrito `f0 > 20`;
- todos os ajustes `g`;
- boundary `e=31/32/33`;
- flags `c/d`, ordem do multiplicador `1.4` e multiplicador global `V1`;
- piso 500 antes dos multiplicadores finais;
- overflow JVM `Int`, sem clamp/saturação inventados.

## Consequência para persistência

A antiga pendência “fórmula de `best.o.n` desconhecida” está fechada. Isso **não** autoriza sozinho uma V15: `n` continua serializado no legado e a política de save/reopen precisa decidir se o moderno deve persistir o raw calculado ou recomputá-lo somente em boundaries em que `best.o.o()` é comprovadamente chamado.

O V14 `career_player_runtime` não possui owners explícitos para `M`, `N`, `n` ou `d`. Qualquer V15 deverá ser agrupada somente após o lifecycle completo desses estados e uma estratégia de upgrade comprovada, sem backfill esportivo inventado.

Room permanece V14.
