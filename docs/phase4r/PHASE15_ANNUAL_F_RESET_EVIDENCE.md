# Fase 15 — evidência executável de `best.b.F()`

Status: **CHARACTERIZED / pure control-flow frozen / callee runtime composition pending**

Corpus oficial: `Brasfoot.apk_Decompiler.com.zip` — SHA-256 `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465` — package `com.brasfoot.v2020` — versionCode `202632`.

SMALI é a autoridade executável nesta caracterização.

## 1. Alcance

`best.n.m()` chama `best.b.F()` no caminho final em que `E1()==true` conforme o roteador anual já congelado em `LegacyAnnualNMRoutingRules`.

Esta rodada reabriu diretamente `smali/best/b.smali`, `smali/best/k0.smali` e `smali/best/o.smali` do corpus oficial e validou o ZIP pelo SHA-256 acima antes da inspeção.

## 2. Corpo executável de `best.b.F()`

O SMALI executa exatamente três passes e não contém RNG próprio.

### Passe 1 — todas as entradas de cada competição/tournament

Para cada índice externo `i` de `best.b.J0()`:

1. obtém `J0()[i].z0()`;
2. para cada índice interno `j` dessa lista;
3. obtém `z0()[j]` como `konrent.t` / `best.k0`;
4. chama **`k0.c(j)`**.

A peculiaridade importante é que o argumento passado a `c` é o próprio índice interno `j`, não o índice externo da competição.

### Passe 2 — todos os jogadores globais

Para cada jogador em `best.b.D0()` chama:

`best.o.d1(0)`

O SMALI de `best.o.d1(I)` é um setter direto do campo inteiro `j0`. Portanto este passe zera `j0` para todos os jogadores da coleção global.

### Passe 3 — somente `z0()[0]` de cada competição

Para cada índice externo `i` de `best.b.J0()`:

1. acessa **sempre** `J0()[i].z0().get(0)`;
2. obtém a lista `h()` desse primeiro `konrent.t`;
3. percorre todos os jogadores dessa lista;
4. chama `best.o.D0()` em cada jogador.

Isso é uma quirk executável do legado: o terceiro passe **não** percorre todas as entradas `z0()`; usa somente a primeira entrada de cada competição.

## 3. Callees substantivos

### `best.o.d1(0)`

Comprovado como setter direto:

`j0 = 0`.

### `best.o.D0()`

O SMALI:

1. incrementa `j0` em 1;
2. se `W0()` já é verdadeiro, termina;
3. exige `j0 >= 2`, clube não nulo e idade `< 35`;
4. então aplica thresholds adicionais por `club.J()` e `club.j0()`;
5. quando os thresholds são satisfeitos, grava `M1(TRUE)`.

Portanto `D0()` é mutação real de estado de jogador e não pode ser classificada como presentation-only.

### `best.k0.c(index)`

Também é substantivo. O SMALI chama `U()`, cria `best.h0`, percorre a coleção interna `components.n1` por uma ordem fixa de códigos `[0,1,2,2,5,6,6,3,3,4,4]`, seleciona jogadores conforme thresholds e adiciona estado a coleções do tournament. Há efeitos adicionais de flag de jogador em um ramo específico. Sua composição moderna permanece separada até o mapping de `best.k0`/`components.n1` estar comprovado.

## 4. Implementação moderna desta rodada

Foi adicionada `LegacyAnnualFResetRules`, regra pura que congela somente o controle de fluxo comprovado:

- `CallTournamentReset(competitionIndex, entryIndex, argument=entryIndex)` para todo `z0()[j]`;
- `ResetGlobalPlayerCounter(playerIndex, value=0)` para todo jogador global;
- `ProgressFirstEntryPlayer(competitionIndex, playerIndex)` somente para os jogadores de `z0()[0].h()`.

A regra não executa `k0.c` nem `o.D0` e não inventa equivalência persistente. Ela preserva a separação entre caracterização de orquestração e mutações substantivas.

## 5. Regressões

`LegacyAnnualFResetRulesTest` congela:

- ordem integral dos três passes;
- argumento de `k0.c` igual ao índice interno;
- third-pass restrito à primeira entrada `z0()[0]`;
- coleções vazias;
- consistência entre as projeções de competição fornecidas ao planner.

## 6. Persistência

Esta evidência não exige Room V15 por si só. `j0` e os demais estados de jogador/tournament precisam primeiro ser confrontados com os runtimes modernos existentes. Nenhum backfill/default esportivo é autorizado por esta caracterização.

## 7. Classificação

- `best.b.F()` control-flow → **IMPLEMENTED_NEEDS_REVALIDATION** via `LegacyAnnualFResetRules`;
- `best.o.d1(0)` → **CHARACTERIZED**, setter `j0=0` comprovado;
- `best.o.D0()` → **REACHABLE_NOT_IMPLEMENTED** até equivalência moderna de `j0/M1` ser provada;
- `best.k0.c(index)` → **REACHABLE_NOT_IMPLEMENTED** até mapping moderno do estado tournament/`components.n1` ser provado.

A função `best.b.F()` inteira ainda não deve ser promovida a `IMPLEMENTED_AND_CERTIFIED` enquanto esses callees substantivos não estiverem compostos e testados no runtime real.

## 8. Próximo passo

1. localizar o estado moderno equivalente a `best.o.j0` e ao flag escrito por `M1(TRUE)`;
2. mapear `best.k0.c(index)` contra os stores/rules modernos de competição;
3. compor os três passes atomicamente somente depois dessas equivalências;
4. atualizar a matriz agregada;
5. continuar os callees de `best.a.J(1)` e a composição final de `best.f`.
