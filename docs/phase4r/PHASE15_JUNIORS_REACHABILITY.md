# Fase 15 — alcance e semântica executável de Juniores

Status: **CHARACTERIZED / IMPLEMENTATION IN PROGRESS**

Corpus oficial: `Brasfoot.apk_Decompiler.com.zip` — SHA-256 `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465` — package `com.brasfoot.v2020` — versionCode `202632`.

Regra de evidência: quando Java decompilado e SMALI divergem, o SMALI executável prevalece. Nenhuma regra abaixo foi inferida de fonte esportiva externa.

## 1. Superfície alcançável

`ActivityMainTeam` alcança `ActivityJuniores`. Na activity, os caminhos substantivos confirmados são:

- peneira: `ActivityJuniores.j()` → confirmação → `ActivityJuniores.h()` → `best.b.h2(best.c0)`;
- dispensa: confirmação → `ActivityJuniores.g()` → remoção do `best.p` selecionado de `c0.a0()`;
- promoção manual: `ActivityJuniores.k()` → confirmação → `ActivityJuniores.i()` → `best.t.e(FALSE, best.p, best.c0)`.

A lista `c0.a0()` é o estado juvenil legado. O objeto nela armazenado é `best.p implements Serializable`; ele ainda **não** é o jogador final `best.o`.

## 2. Peneira — `best.b.h2(c0)`

O SMALI executa seis tentativas nas posições exatas:

`[0, 1, 2, 3, 3, 4]`

Para cada tentativa:

1. realiza `nextInt(3)`;
2. somente o resultado `0` passa;
3. verifica novamente `c0.a0().size() < 18`;
4. chama imediatamente `best.p.d(target, position, null, 0, null, FALSE)`;
5. só então segue para a próxima tentativa.

Logo, os draws internos de `best.p.d(...)` são intercalados entre os seis draws de gate. Pré-sortear os seis gates mudaria a sequência RNG e é proibido na implementação moderna.

`ActivityJuniores.j()` verifica primeiro caixa e depois o teto de 18. Após uma confirmação válida, `ActivityJuniores.h()` cobra o custo mesmo se `h2()` retornar zero jogadores.

O lançamento financeiro é `c0.D(custo, 9)`. O SMALI de `c0.D` reduz o caixa e chama `best.m.d(valor, 9)`; `best.m.d` encaminha códigos não reconhecidos para o acumulador de outras despesas. O equivalente moderno caracterizado é `LegacyFinanceLedgerRule.addExpense(..., rawCode = 9)`, que incrementa `miscellaneousExpense`.

## 3. Promoção manual e dispensa

`ActivityJuniores.k()` bloqueia promoção quando `c0.Z().size() >= 30`. A promoção manual chama exatamente `best.t.e(FALSE, p, c0)`.

A dispensa remove apenas o `best.p` selecionado da lista juvenil e reorganiza a lista. Não há RNG nem lançamento financeiro nesse caminho.

A persistência moderna V14 mantém o `best.p` separado do jogador procedural final. `CareerJuniorManualPromotionStore.promote(...)` só chama o materializador no ponto comprovado `best.t.e(FALSE, p, c0)`, persiste jogador/membership e RNG na mesma transação e remove o draft apenas na rota manual.

## 4. Estado de `best.p` que precisa sobreviver a reopen

O `best.p` serializável mantém nome, booleano e campos inteiros, além do acumulador `double D`. O gerador `best.p.d(...)` chama rotinas que preenchem inclusive os campos expostos por `w()` e `u()` antes de retornar.

O `double D` não é descartável: o corpo executável de `best.p.b()` o atualiza ao longo do tempo e pode incrementar o campo `o`. Materializar imediatamente um `CareerProceduralPlayerEntity` na peneira seria semanticamente incorreto porque moveria para a peneira draws que no legado só ocorrem em `best.t.e(...)` na promoção.

A migration V14 resolve esse boundary com `career_junior_drafts`, mantendo o draft pré-promoção separado de `career_player_runtime` / `career_procedural_players` / `career_squad_memberships`.

## 5. Progressão — `best.p.b()`

O Java decompilado disponível para este método diverge do bytecode e não é usado como autoridade. O SMALI oficial mostra:

- jogadores com idade maior que 20 não são alterados pelo método;
- incremento base por idade: `<=17: 0.5`, `18: 0.375`, `19: 0.35`, `20: 0.125`;
- bônus pelo campo `n`: `<=3: 0.03`, `<=6: 0.04`, `<=8: 0.07`, `9: 0.10`, `10: 0.11`;
- soma ao acumulador `D`;
- somente se `D > 1.0` e `o < 100`, incrementa `o` e subtrai `1.0` de `D`.

A comparação é estritamente `>`, não `>=`. `CareerJuniorRuntimeStore.progressDevelopment(...)` persiste `o` e `D` sem alterar os demais campos do draft.

## 6. Envelhecimento anual — `best.p.c(c0)`

O SMALI também prevalece sobre uma decompilação Java divergente. O método:

1. incrementa idade incondicionalmente;
2. antes de 20 anos, não executa outra ação;
3. a partir de 20, compara `n` contra limiares por `c0.p0()` iguais a `[1,4,5,6,6,6]`;
4. também exige que a quantidade sênior da posição seja menor que `[3,5,5,8,6]` para posições `0..4`;
5. se qualificado e `c0.A0() < 30`, chama **`best.t.e(TRUE, p, c0)`**;
6. após essa promoção, se `c0.B0() < 10`, gera/stageia um substituto com `best.p.d(target, -1, null, 0, null, TRUE)`;
7. se não qualificado e `c0.Q0()` for falso, regenera o próprio draft via `best.p.d(target, -1, this, 0, null, TRUE)`.

A caracterização promovida no commit `17c18a566bd064d026d1ce29604f484af4841ea0` fechou a antiga dúvida sobre o corpo comum de `best.t.e(...)`: as rotas `FALSE` e `TRUE` compartilham a materialização do jogador e os draws de RNG anteriores aos efeitos finais de listas. A divergência comprovada está no efeito final:

- `FALSE`: remove o draft do clube imediatamente e stageia o materializado em `D0`;
- `TRUE`: não remove o draft imediatamente, stageia o draft em `L1` e o materializado em `J1`.

Portanto o problema restante **não é** caracterizar novamente campos/RNG do materializador `TRUE`. O gap real é mapear e persistir os efeitos anuais `L1/J1`, a posterior remoção/substituição e a geração do substituto sem inventar equivalência de roster moderna.

## 7. Implementação moderna já existente neste marco

O Marco C já promoveu para código/persistência:

- `LegacyJuniorRuntimeRules` para disponibilidade, teto 18, seis gates, limite sênior 30, progressão e decisão anual;
- `LegacyJuniorDraftFieldRules` para campos completos do draft e efeitos finais `FALSE`/`TRUE`;
- Room V14 com `CareerJuniorDraftEntity`, `CareerJuniorDraftDao`, `MIGRATION_13_14`, schema exportado e migration tests;
- `CareerJuniorRuntimeStore.runTrial(...)` com RNG + finanças raw 9 + drafts atômicos;
- dispensa persistida sem RNG/finanças;
- `CareerJuniorRuntimeStore.progressDevelopment(...)` preservando o acumulador fracionário;
- `CareerJuniorManualPromotionStore.promote(...)` com materialização tardia, RNG/jogador/membership/draft na mesma transação e rollback caracterizado.

O checkpoint `0b3ca8ce6f7e5fea2c9fe0f11b1be71ab2297880` foi certificado pelos três workflows obrigatórios. Isso não fecha `ActivityJuniores`, porque o lifecycle anual `TRUE` continua materialmente aberto.

## 8. Próximo gate desta fatia

Antes de classificar Juniores como implementado, ainda são obrigatórios:

- mapear o staging legado `L1/J1` para estado moderno sem confundir `rosterKind` com estado transitório anual;
- integrar idade + decisão + `best.t.e(TRUE,...)` como operação atômica de carreira/RNG;
- preservar a ordem do substituto `best.p.d(..., TRUE)` quando `B0 < 10`;
- preservar a rota de refresh do próprio draft quando `Q0 == FALSE` sem promoção;
- adicionar regressões determinísticas de reopen, rollback e ordem RNG para a rota anual completa;
- atualizar a matriz agregada somente após esse boundary estar comprovado;
- gates completos no exato FINAL_HEAD da Fase 15.

`ActivityJuniores` permanece `UNKNOWN_NEEDS_INVESTIGATION` até o fluxo alcançável inteiro estar classificado e testado.
