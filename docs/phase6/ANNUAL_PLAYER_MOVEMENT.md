# ANNUAL PLAYER MOVEMENT — Fase 6

## Fonte

Corpus oficial: `Brasfoot.apk_Decompiler.com.zip`  
SHA-256: `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`

A caracterização abaixo compara diretamente `sources/best/o.java` com `smali/best/o.smali` e considera apenas o formato de chamada realmente usado pelos caminhos anuais de `best.a0.i()` e `best.a0.j(...)`:

`player.T1(destination, player.A0(), false, false, false)`

Nenhum nome financeiro é atribuído a símbolos obfuscados sem prova adicional.

## Efeitos confirmados de `T1` para a chamada anual

Java e SMALI coincidem na seguinte sequência estrutural:

1. obtém o clube anterior por `u0()`;
2. quando o clube anterior ou o destino possui `Q0()==true`, marca `ActivityMainTeam.F=true`;
3. religa a referência do jogador ao destino por `K1(destination)`;
4. grava `X=false` e `Z=false`;
5. como os dois últimos booleanos da chamada anual são `false`, `Y` não é alterado por esses branches;
6. a rotina genérica contém um cálculo percentual adicional, mas ele depende do primeiro booleano (`z2`) ser verdadeiro; o caller anual passa `false`, logo o valor calculado é sempre `0` neste caminho;
7. se `A0()>0` e o clube anterior é `Q0`, executa `source.B(A0(), 1)`;
8. se `A0()>0` e o destino é `Q0`, executa `destination.D(A0(), 1)`;
9. chama `player.c(180L, true)` — a constante estrutural confirmada para este branch é `180`;
10. chama `Q1()` no jogador;
11. se havia clube anterior, limpa referências especiais quando apontavam para o jogador e remove o jogador por `source.f1(player)`;
12. adiciona o jogador ao destino por `destination.f(player)`;
13. quando origem e destino são ambos `Q0`, também executa `S1(TRUE)` no jogador e `source.E1(false)`.

O método legado não rejeita `A0()` negativo. As chamadas de código `1` são simplesmente condicionadas a `A0()>0`, enquanto a movimentação estrutural continua. A projeção moderna preserva esse comportamento e não adiciona uma validação que o APK não possuía.

Classificação: `JAVA_CONFIRMED_BY_SMALI`.

## Projeção moderna

`LegacyAnnualPlayerMovementRules.annualT1Plan(...)` representa apenas esses efeitos observáveis. O objeto resultante usa nomes estruturais (`sourceBCode1Amount`, `targetDCode1Amount`, `legacyDurationArgument`) em vez de inferir receita, despesa, taxa ou contrato específico onde o corpus ainda não justificou o nome.

A projeção prova ainda que, no caller anual:

- o cálculo percentual secundário do `T1` genérico é inalcançável porque `z2=false`;
- `Y` permanece fora das mutações dos branches `z3/z4` porque ambos são `false`;
- `K1(destination)` e `Q1()` são sempre chamados;
- a movimentação de membership é efetiva: remove da origem quando ela existe e adiciona ao destino;
- o efeito de código `1` só ocorre quando `A0()>0` e o respectivo clube é `Q0`.

## Por que a Fase 6 não grava esse plano diretamente no Room

A persistência moderna atual possui `players` + `squad_memberships`, mas ainda não materializa, com equivalência comprovada, todos os campos dinâmicos de `best.o` usados por `T1`, incluindo as flags `X/Y/Z`, a representação de `A0()`, o estado produzido por `c(180L,true)` e as estruturas chamadas por `B(...,1)` / `D(...,1)`.

Aplicar somente a troca de `squad_memberships` e ignorar os demais efeitos criaria uma falsa paridade. Criar Room V3 agora para campos ainda não mapeados também seria uma migration artificial.

Portanto:

- a regra estrutural está executável e coberta por testes;
- a mutação persistida completa fica bloqueada até o modelo moderno do jogador/efeitos associados representar todos os campos necessários;
- Room permanece V2;
- nenhum dado esportivo é inventado para preencher o que ainda não existe no modelo moderno.

## Fallback procedural `best.p.d(...)`

O caminho de manutenção mínima de elenco pode chegar a `best.p.d(...)` quando nenhum doador seguro é encontrado. O SMALI confirma que esse método é um gerador procedural amplo, com várias instâncias de `Random`, tabelas condicionais, atributos derivados e chamadas posteriores `D/e/h/g`.

Ele não é um simples “jogador vazio”. Materializá-lo parcialmente seria incorreto e poderia fabricar atributos que não seguem o APK.

Nesta fase ele permanece classificado como `PROCEDURAL_GENERATOR_REQUIRES_DEDICATED_RECONSTRUCTION`. O comportamento anterior ao fallback — mínimos de elenco, pools doadores, filtros, surplus e ordem — já está congelado em `LegacyAnnualSquadFloorRules`.

Nenhuma fonte externa foi utilizada.
