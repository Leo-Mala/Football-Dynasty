# Fase 15 — alcance e semântica executável de Juniores

Status: **CHARACTERIZED / RUNTIME IMPLEMENTED / EXACT-HEAD REVALIDATION PENDING**

Corpus oficial: `Brasfoot.apk_Decompiler.com.zip` — SHA-256 `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465` — package `com.brasfoot.v2020` — versionCode `202632`.

Regra de evidência: quando Java decompilado e SMALI divergem, o SMALI executável prevalece. Nenhuma regra abaixo foi inferida de fonte esportiva externa.

## 1. Superfície alcançável

`ActivityMainTeam` alcança `ActivityJuniores`. A auditoria completa da activity está em `PHASE15_JUNIOR_ACTIVITY_SURFACE.md`.

Os seams substantivos confirmados são:

- peneira: `ActivityJuniores.j()` → confirmação → `ActivityJuniores.h()` → `best.b.h2(best.c0)` + `c0.D(custo, 9)`;
- dispensa: confirmação → `ActivityJuniores.g()` → remoção do `best.p` selecionado de `c0.a0()`;
- promoção manual: `ActivityJuniores.k()` → confirmação → `ActivityJuniores.i()` → `best.t.e(FALSE, best.p, best.c0)`;
- lifecycle anual fora da activity: `best.b.q()` → `best.p.c(c0)` → opcional `best.t.e(TRUE, p, c0)`.

`onCreate`, `e`, `l`, listeners e as partes restantes de `f/j/k` são apresentação, seleção, validação ou roteamento para esses seams e não introduzem mutação esportiva adicional.

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

A dispensa remove apenas o `best.p` selecionado da lista juvenil e reorganiza a apresentação. Não há RNG nem lançamento financeiro nesse caminho.

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

As rotas `FALSE` e `TRUE` compartilham a materialização do jogador e os draws de RNG anteriores aos efeitos finais de listas. A divergência comprovada está no efeito final:

- `FALSE`: remove o draft do clube imediatamente e stageia o materializado em `D0`;
- `TRUE`: não remove o draft imediatamente, stageia o draft em `L1` e o materializado em `J1`.

A auditoria direta de `best.b.q()` fechou `L1/J1`: `F1/E1/D1` são buffers transitórios do lifecycle anual, não novo estado persistente. A geração do substituto ocorre imediatamente no ponto legado, mas sua inserção é diferida até o fim da iteração do snapshot original do clube. O append global dos materializados ocorre depois de todos os clubes. Ver `PHASE15_JUNIOR_ANNUAL_STAGING.md`.

`CareerJuniorAnnualLifecycleStore.run(...)` representa o estado durável final equivalente em uma transação Room/RNG, processa somente o snapshot original, atualiza contagem sênior imediatamente após promoção, preserva refresh/substituição na ordem caracterizada e não revisita substituto recém-gerado no mesmo ano.

## 7. Implementação moderna desta fatia

O Marco C já promoveu para código/persistência:

- `LegacyJuniorRuntimeRules` para disponibilidade, teto 18, seis gates, limite sênior 30, progressão e decisão anual;
- `LegacyJuniorDraftFieldRules` para campos completos do draft e efeitos finais `FALSE`/`TRUE`;
- `LegacyJuniorAnnualLifecycleRules` para ordem de staging anual;
- Room V14 com `CareerJuniorDraftEntity`, `CareerJuniorDraftDao`, `MIGRATION_13_14`, schema exportado e migration tests;
- `CareerJuniorRuntimeStore.runTrial(...)` com RNG + finanças raw 9 + drafts atômicos;
- dispensa persistida sem RNG/finanças;
- `CareerJuniorRuntimeStore.progressDevelopment(...)` preservando o acumulador fracionário;
- `CareerJuniorManualPromotionStore.promote(...)` com materialização tardia, RNG/jogador/membership/draft na mesma transação e rollback;
- `CareerJuniorAnnualLifecycleStore.run(...)` com idade, decisão, materialização TRUE, refresh/substituto, drafts/jogadores/memberships/RNG atômicos;
- regressões anuais de ordering, rollback e close/reopen do estado durável final.

O checkpoint `e2566a9e255275e591c06596519c54c1b0001848` foi certificado pelos três workflows obrigatórios antes da regressão adicional de close/reopen. O HEAD atual precisa ser novamente certificado por inteiro.

## 8. Critério restante para fechar Fase 15.1

A auditoria completa de `ActivityJuniores.smali` não encontrou seam esportivo adicional além dos já mapeados; o restante da classe é apresentação, seleção, validação e roteamento. Portanto não permanece função de gameplay desconhecida nessa superfície.

Para fechar **Fase 15.1 — Juniores**, resta:

- obter PASS dos três workflows obrigatórios no mesmo exact HEAD contendo a regressão de close/reopen e a documentação final;
- revalidar PR/head/reviews após esses gates;
- promover os status juvenis da matriz de `IMPLEMENTED_NEEDS_REVALIDATION` para `IMPLEMENTED_AND_CERTIFIED` somente nesse SHA certificado.

A implementação visual de adapter, Toasts, diálogos, seleção e refresh de tela pertence à fase de UI e não é tratada como regra esportiva/persistência na Fase 15.
