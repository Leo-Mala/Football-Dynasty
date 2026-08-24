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

## best.b.w1() — incremento de temporada

Java e SMALI confirmam que `w1()` faz somente:

- lê o campo de temporada `b`;
- incrementa em `1`;
- grava novamente o campo `b`.

Isso confirma que o incremento ocorre antes da reconstrução do calendário em `l()`.

Classificação: `JAVA_CONFIRMED_BY_SMALI`.

## best.b.c(year) — materialização do calendário anual

Java e SMALI confirmam que `c(int year)`:

1. cria um `java.util.Calendar` em `1º de janeiro` do ano solicitado;
2. percorre um dia por vez enquanto o ano permanecer igual ao argumento;
3. cria um objeto de dia legado `best.a` para cada data e o adiciona ao estado central;
4. durante janeiro, detecta o primeiro domingo (`Calendar.DAY_OF_WEEK == 1`);
5. salva o índice desse primeiro domingo via `T2(index)`;
6. avança por `Calendar.DAY_OF_MONTH + 1` até terminar o ano;
7. chama `y1()` ao final.

Isto confirma diretamente os invariantes modernos já existentes de:

- 365/366 dias conforme o ano;
- início de temporada no primeiro domingo de janeiro;
- transição 2026 -> 2027 -> 2028 com tratamento natural de ano bissexto.

Classificação: `JAVA_CONFIRMED_BY_SMALI` para construção diária e seleção do primeiro domingo; a semântica interna de `y1()` permanece em caracterização.

## Manutenções posteriores já delimitadas

A leitura Java permitiu delimitar, sem ainda transportar semântica para o domínio moderno:

- `s()` percorre uma coleção de entidades, limpa estado anual e executa resets/manutenções condicionais; depois chama `p()` sobre itens de `g1()`;
- `D()` atualiza uma coleção histórica quando existente ou a cria a partir de competições/temporada anterior;
- `r()` executa manutenção sobre competições/tabelas, limpa flags anuais e reconstrói listas ordenadas de clubes/participantes;
- `o()` executa manutenção equivalente sobre outra família de competições e limpa flags anuais;
- `q()` executa manutenção de coleções, remoções/adições diferidas, limpeza de entradas inválidas e rotinas finais de reorganização.

Essas descrições são deliberadamente estruturais. Os nomes/objetos obfuscados ainda não permitem afirmar com segurança todos os efeitos esportivos de cada método.

Classificação: `JAVA_PARTIAL_SEMANTICS`; confirmação SMALI pontual será exigida antes de modelar cada efeito no domínio.

## Impacto no domínio moderno

- `CareerState`, `SeasonState`, `CareerCalendarState` e RNG persistível permanecem válidos;
- `LegacyCalendarRules.BASE_YEAR = 2026` está revalidado contra o baseline 2026/27 para o caminho padrão;
- a implementação de `transitionSeason()` está alinhada com `w1() + l() + c(year)` nos invariantes de temporada, ano, quantidade de dias e primeiro domingo;
- `transitionSeason()` ainda não representa todos os efeitos colaterais anuais de `best.b.d()`;
- nenhuma alteração de Room é justificada por esta caracterização inicial;
- não é correto declarar paridade completa de fim de temporada enquanto a semântica dos resets/manutenções anuais não for caracterizada.

## Próximas investigações

Prioridade de recuperação:

1. `y1()` e os efeitos finais de calendário;
2. `best.b.s()`, `D()`, `r()`, `o()` com Java↔SMALI para identificar invariantes anuais seguros;
3. `g1().l1()` para efeitos por entidade;
4. `P0()`, `Y3()` e `q()` para reconstrução/limpeza pós-transição;
5. Java↔SMALI de qualquer método truncado ou contraditório encontrado nessas rotinas;
6. testes de ordem e invariantes somente após semântica suficiente ser comprovada.
