# LEGACY_SEASON_LIFECYCLE — Brasfoot 2026/27

## Fonte

Corpus oficial: `Brasfoot.apk_Decompiler.com.zip` (`3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`).

Este documento registra somente comportamento observado no novo baseline.

## ActivityFimAno

`com.brasfoot.v2020.ActivityFimAno` apresenta o resumo de fim de temporada e dispara a continuidade.

### Evidência Java

- `g()` inicia uma tarefa assíncrona;
- `e()` executa `core.a.f13450b.z3(true)`, chama `best.n.n()` e encerra a Activity;
- `onBackPressed()` também chama `g()`, portanto sair da tela segue o mesmo caminho de continuidade;
- `onCreate()` chama `core.a.f13450b.l()` depois de montar o resumo;
- o título usa `core.a.f13450b.J()` para a temporada atual;
- `onStart()` pode avançar automaticamente conforme flags legadas.

Classificação: `JAVA_CONFIRMED`, com validação SMALI obrigatória antes de transportar efeitos colaterais além dos invariantes aqui listados.

## best.n.n()

O método observado é simples:

- se `core.a.f13450b.E1()` for verdadeiro, chama `core.a.f13450b.d()`;
- caso contrário, segue o roteador normal `best.n.i()`.

Isso confirma que `best.b.d()` é a entrada principal do processamento de novo ano quando a flag correspondente está ativa.

Classificação: `JAVA_CONFIRMED`.

## best.b.d()

O método `best.b.d()` é a orquestração de novo ano. A sequência Java observada inclui, na ordem:

1. manutenção sobre `g1().l1()`;
2. chamada condicional a `w2()`;
3. reconstrução do calendário por `l()` quando `V0` é falso;
4. `s()`;
5. chamada condicional a `n()`;
6. `D()`;
7. `r()`;
8. `o()`;
9. manutenção condicional via `best.a`;
10. `A1()` quando habilitado;
11. `best.a.m()`;
12. `Y3()`;
13. `q()`;
14. `a0.d()`;
15. `a0.a()`;
16. `P0()`;
17. limpeza de `M0`;
18. retorno ao roteador `best.n.n()`.

Os nomes obfuscados não autorizam atribuir semântica esportiva específica a cada chamada. Cada etapa deverá ser investigada individualmente antes de implementação moderna correspondente.

Classificação: `JAVA_PARTIAL`: a ordem é observável; a semântica de cada método obfuscado ainda requer caracterização.

## best.b.l() — reconstrução do calendário

O novo baseline contém a seguinte regra estrutural em `best.b.l()`:

- executa `w1()`;
- limpa a coleção de calendário `G`;
- chama `c((f4030c == 22 ? 2022 : 2026) + (season - 1))`;
- finaliza com `best.a.H()`.

Para a geração Brasfoot 2026/27, isso confirma a base moderna já utilizada em `LegacyCalendarRules.BASE_YEAR = 2026`.

A regra não prova que todo modo de jogo usa sempre 2026: existe explicitamente um branch legado para `f4030c == 22`. A implementação moderna não deve apagar essa diferença se esse modo vier a ser reconstruído.

Classificação: `JAVA_CONFIRMED` para a fórmula observada; `REVALIDATION_REQUIRED` para o significado completo de `f4030c`.

## Impacto no domínio moderno

- `CareerState`, `SeasonState`, `CareerCalendarState` e RNG persistível permanecem válidos;
- `LegacyCalendarRules.BASE_YEAR = 2026` está revalidado contra o baseline 2026/27;
- `transitionSeason()` continua útil como projeção mínima, mas ainda não representa todos os efeitos colaterais de `best.b.d()`;
- nenhuma alteração de Room é justificada por esta caracterização inicial;
- não é correto declarar paridade completa de fim de temporada enquanto os métodos obfuscados da sequência não forem caracterizados.

## Próximas investigações

Prioridade de recuperação:

1. `best.b.w1()` e `best.b.c(year)` para calendário;
2. `best.b.s()`, `D()`, `r()`, `o()` para manutenção central;
3. `g1().l1()` para efeitos por entidade;
4. `P0()`, `Y3()` e `q()` para reconstrução/limpeza pós-transição;
5. Java↔SMALI de qualquer método truncado ou contraditório;
6. testes de ordem e invariantes somente após semântica suficiente ser comprovada.
