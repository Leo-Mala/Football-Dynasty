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

O lançamento financeiro é `c0.D(custo, 9)`. O SMALI de `c0.D` reduz o caixa e chama `best.m.d(valor, 9)`; `best.m.d` encaminha códigos não reconhecidos para o acumulador de outras despesas. O equivalente moderno já caracterizado é `LegacyFinanceLedgerRule.addExpense(..., rawCode = 9)`, que incrementa `miscellaneousExpense`.

## 3. Promoção manual e dispensa

`ActivityJuniores.k()` bloqueia promoção quando `c0.Z().size() >= 30`. A promoção manual chama exatamente `best.t.e(FALSE, p, c0)`.

A dispensa remove apenas o `best.p` selecionado da lista juvenil e reorganiza a lista. Não há RNG nem lançamento financeiro nesse caminho.

O projeto já contém caracterização do materializador `best.t.e(FALSE, p, target)` em `LegacyProceduralMaterializationRules`, mas ainda não havia um estado durável pré-promoção para o `best.p`; portanto isso não constitui uma implementação completa da funcionalidade de Juniores.

## 4. Estado de `best.p` que precisa sobreviver a reopen

O `best.p` serializável mantém nome, booleano e campos inteiros, além do acumulador `double D`. O gerador `best.p.d(...)` chama rotinas que preenchem inclusive os campos expostos por `w()` e `u()` antes de retornar.

O `double D` não é descartável: o corpo executável de `best.p.b()` o atualiza ao longo do tempo e pode incrementar o campo `o`. Portanto materializar imediatamente um `CareerProceduralPlayerEntity` na peneira seria semanticamente incorreto: isso moveria para a peneira os draws que no legado só ocorrem em `best.t.e(...)` na promoção.

A persistência moderna precisa, em etapa própria, guardar o draft juvenil pré-promoção e somente criar o jogador procedural final durante a promoção.

## 5. Progressão — `best.p.b()`

O Java decompilado disponível para este método diverge do bytecode e não é usado como autoridade. O SMALI oficial mostra:

- jogadores com idade maior que 20 não são alterados pelo método;
- incremento base por idade: `<=17: 0.5`, `18: 0.375`, `19: 0.35`, `20: 0.125`;
- bônus pelo campo `n`: `<=3: 0.03`, `<=6: 0.04`, `<=8: 0.07`, `9: 0.10`, `10: 0.11`;
- soma ao acumulador `D`;
- somente se `D > 1.0` e `o < 100`, incrementa `o` e subtrai `1.0` de `D`.

A comparação é estritamente `>`, não `>=`.

## 6. Envelhecimento anual — `best.p.c(c0)`

O SMALI também prevalece sobre uma decompilação Java divergente. O método:

1. incrementa idade incondicionalmente;
2. antes de 20 anos, não executa outra ação;
3. a partir de 20, compara `n` contra limiares por `c0.p0()` iguais a `[1,4,5,6,6,6]`;
4. também exige que a quantidade sênior da posição seja menor que `[3,5,5,8,6]` para posições `0..4`;
5. se qualificado e `c0.A0() < 30`, chama **`best.t.e(TRUE, p, c0)`**;
6. após essa promoção, se `c0.B0() < 10`, gera/stageia um substituto com `best.p.d(target, -1, null, 0, null, TRUE)`;
7. se não qualificado e `c0.Q0()` for falso, regenera o próprio draft via `best.p.d(target, -1, this, 0, null, TRUE)`.

A flag `TRUE` do fluxo anual é diferente da flag `FALSE` da promoção manual. A caracterização moderna existente de `LegacyProceduralMaterializationRules` documenta a rota `FALSE`; portanto a materialização anual `TRUE` continua aberta e não deve ser substituída pela versão manual por conveniência.

## 7. Implementação moderna adicionada neste corte

`LegacyJuniorRuntimeRules` promove para código executável puro somente o que já está provado pelo bytecode:

- ordem da pré-condição de peneira;
- teto juvenil 18;
- categoria financeira crua 9;
- seis gates `[0,1,2,3,3,4]` com RNG intercalado ao callback de `p.d`;
- teto 30 da promoção manual;
- progressão exata de `best.p.b()`;
- decisão anual de `best.p.c(c0)`, distinguindo a rota `TRUE` da promoção anual.

O objeto não persiste nem materializa jogadores. Essa separação é intencional: primeiro certificamos regras/RNG; depois a migration aditiva persistirá o estado `best.p` sem antecipar RNG de promoção.

## 8. Próximo gate desta fatia

Antes de classificar Juniores como implementado, ainda são obrigatórios:

- caracterizar os campos completos pré-promoção (`best.p.D/f/e/h/g`), inclusive os valores que precisam sobreviver a reopen;
- caracterizar `best.t.e(TRUE, p, c0)` separadamente da rota manual `FALSE`;
- adicionar persistência Room aditiva e não destrutiva para o draft juvenil;
- implementar peneira/dispensa/promoção como transações atômicas com estado RNG persistido;
- integrar progressão/envelhecimento ao lifecycle anual no ponto comprovado;
- regressões de reopen, rollback e ordem RNG;
- gates completos no exato FINAL_HEAD da Fase 15.
