# LEGACY_SEASON_LIFECYCLE — Brasfoot 2026/27

## Fonte

Corpus oficial: `Brasfoot.apk_Decompiler.com.zip` (`3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`).

Este documento registra somente comportamento observado no novo baseline. Os métodos críticos abaixo foram comparados diretamente com o SMALI correspondente.

## ActivityFimAno

`com.brasfoot.v2020.ActivityFimAno` apresenta o resumo de fim de temporada e dispara a continuidade.

### Java ↔ SMALI

- `d()` cria/executa a `AsyncTask` de continuidade;
- o método sintético `g()` apenas delega para `d()`;
- `e()` executa `best.b.z3(true)`, chama `best.n.n()` e encerra a Activity;
- `onBackPressed()` chama `d()`, portanto sair da tela segue a mesma tarefa de continuidade;
- o fluxo de `onCreate()` contém a manutenção/resumo de encerramento e a chamada de calendário observada no Java;
- o título usa a temporada atual do estado legado;
- `onStart()` pode avançar automaticamente conforme flags legadas.

O SMALI de `e()` confirma literalmente a ordem `z3(true) -> best.n.n() -> finish()`. O SMALI de `onBackPressed()` confirma a chamada a `d()`, e `d()` confirma a criação/execução da AsyncTask.

Classificação: `JAVA_CONFIRMED_BY_SMALI` para a cadeia de continuidade acima. Efeitos internos da AsyncTask e demais manutenção de `onCreate()` continuam sujeitos a caracterização específica antes de portabilidade semântica.

## best.n.n()

O método faz:

- se `best.b.E1()` for verdadeiro, chama `best.b.d()`;
- caso contrário, segue o roteador normal `best.n.i()`.

O SMALI confirma exatamente o branch `E1() -> d()` ou `i()`.

Isso confirma que `best.b.d()` é a entrada principal do processamento de novo ano quando a flag correspondente está ativa.

Classificação: `JAVA_CONFIRMED_BY_SMALI`.

## best.b.d()

O método `best.b.d()` é a orquestração de novo ano. Java e SMALI confirmam a seguinte ordem estrutural:

1. manutenção sobre cada item de `g1()` via `l1()`;
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
16. consulta condicional `D1()` quando `V0` é verdadeiro;
17. `P0()`;
18. limpeza de `M0` para `false`;
19. retorno ao roteador `best.n.n()`.

O SMALI confirma também que `w2()`, `s()`, `D()`, `r()`, `o()` e `q()` são chamadas internas/privadas, enquanto outras etapas são públicas/estáticas conforme o bytecode.

Os nomes obfuscados não autorizam atribuir semântica esportiva específica a cada chamada. Cada etapa deverá ser investigada individualmente antes de implementação moderna correspondente.

Classificação: `JAVA_CONFIRMED_BY_SMALI` para **ordem e chamadas**; `SEMANTICS_PARTIAL` para o significado esportivo dos métodos obfuscados.

## best.b.l() — reconstrução do calendário

Java e SMALI confirmam a seguinte regra estrutural em `best.b.l()`:

- executa `w1()`;
- limpa a coleção de calendário `G`;
- lê o modo legado (`c` no SMALI);
- se o valor for `22` (`0x16`), usa ano-base `2022` (`0x7e6`);
- caso contrário, usa ano-base `2026` (`0x7ea`);
- soma `(season - 1)` ao ano-base;
- chama `c(year)`;
- finaliza com `best.a.H()`.

Para a geração Brasfoot 2026/27, isso revalida diretamente a base moderna já utilizada em `LegacyCalendarRules.BASE_YEAR = 2026` para o caminho padrão.

A regra não prova que todo modo de jogo usa sempre 2026: existe explicitamente um branch legado para modo `22`. A implementação moderna não deve apagar essa diferença se esse modo vier a ser reconstruído.

Classificação: `JAVA_CONFIRMED_BY_SMALI` para a fórmula e os dois anos-base; `REVALIDATION_REQUIRED` para o significado funcional completo do modo `22`.

## Impacto no domínio moderno

- `CareerState`, `SeasonState`, `CareerCalendarState` e RNG persistível permanecem válidos;
- `LegacyCalendarRules.BASE_YEAR = 2026` está revalidado contra o baseline 2026/27 para o caminho padrão;
- `transitionSeason()` continua útil como projeção mínima, mas ainda não representa todos os efeitos colaterais de `best.b.d()`;
- nenhuma alteração de Room é justificada por esta caracterização inicial;
- não é correto declarar paridade completa de fim de temporada enquanto a semântica dos métodos obfuscados da sequência não for caracterizada.

## Próximas investigações

Prioridade de recuperação:

1. `best.b.w1()` e `best.b.c(year)` para construção do calendário;
2. `best.b.s()`, `D()`, `r()`, `o()` para manutenção central;
3. `g1().l1()` para efeitos por entidade;
4. `P0()`, `Y3()` e `q()` para reconstrução/limpeza pós-transição;
5. Java↔SMALI de qualquer método truncado ou contraditório encontrado nessas rotinas;
6. testes de ordem e invariantes somente após semântica suficiente ser comprovada.
