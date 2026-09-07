# Fase 15.1 — staging anual de Juniores (`L1/J1`)

Status: **SMALI CHARACTERIZED / READY FOR PERSISTED RUNTIME INTEGRATION**

Corpus oficial: `Brasfoot.apk_Decompiler.com.zip` — SHA-256 `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465` — package `com.brasfoot.v2020` — versionCode `202632`.

Esta evidência foi reaberta diretamente no arquivo oficial e o SHA-256 da cópia materializada foi validado antes da leitura. SMALI é a autoridade.

## Evidência executável

### `best.p.c(c0)`

O método incrementa `p.c` antes de qualquer decisão. Quando a promoção anual é qualificada, executa `best.t.e(TRUE, p, c0)` e somente depois verifica `c0.B0() < 10`; se verdadeiro, chama imediatamente `best.p.d(c0, -1, null, 0, null, TRUE)` para gerar o substituto. Quando não qualificado e `Q0 == FALSE`, chama `best.p.d(c0, -1, this, 0, null, TRUE)` sobre o próprio draft.

Consequência: a geração do substituto consome seu RNG antes de o lifecycle visitar o próximo draft original do clube. A inserção do substituto na lista do clube, porém, é diferida.

### `best.t.e(TRUE, p, c0)`

Depois da materialização compartilhada pelas rotas manual/anual, a rota TRUE executa, nesta ordem final:

1. adiciona o draft `p` a `core.a.b.l1()`;
2. chama `c0.f(materialized)` — o jogador passa a compor imediatamente o estado do clube e invalida o cache derivado de elenco;
3. adiciona o jogador materializado a `core.a.b.j1()`.

A rota TRUE não remove `p` diretamente de `c0.a0()` durante a iteração.

### `best.b.q()`

No início do lifecycle anual, `D1`, `F1` e `E1` são listas novas. Para cada clube:

1. limpa `F1` e `E1`;
2. itera a lista original `c0.a0()` e chama `p.c(c0)` em cada draft;
3. somente depois que o iterator termina, executa `c0.a0().removeAll(F1)`;
4. em seguida executa `c0.a0().addAll(E1)`.

Depois de todos os clubes, a lista global de jogadores recebe `addAll(D1)` e `D1` deixa de existir.

Os accessors comprovam os nomes ofuscados:

- `l1()` -> `F1`, lista de `best.p`;
- `k1()` -> `E1`, lista de `best.p`;
- `j1()` -> `D1`, lista de `best.o`.

`best.p.d(..., null, ..., TRUE)` adiciona o novo draft a `k1()/E1`; quando recebe `this` como terceiro argumento, reutiliza o próprio objeto e não cria/stageia um segundo draft.

## Conclusão arquitetural permitida pela evidência

`F1/E1/D1` são **buffers transitórios do lifecycle anual**, criados e consumidos dentro de `best.b.q()`. Eles não representam um novo estado esportivo durável que justifique uma tabela Room ou `rosterKind` próprio.

A implementação moderna deve persistir atomicamente o estado final equivalente, preservando a ordem observável:

`idade → materialização → stage remoção → exposição ao clube → stage global → geração imediata do substituto → próximo draft original → remove F1 → adiciona E1 → após todos os clubes adiciona D1 global`.

Para refresh sem promoção:

`idade → best.p.d(..., this, ..., TRUE)`

ocorre imediatamente no draft existente e não produz staging de lista.

## Regra executável moderna

`LegacyJuniorAnnualLifecycleRules` congela essa ordem em passos explícitos e `LegacyJuniorAnnualLifecycleRulesTest` protege:

- ordem TRUE ao promover;
- geração imediata do substituto versus inserção diferida;
- aplicação F1/E1 após a iteração do clube;
- append D1 somente após todos os clubes;
- refresh imediato do próprio draft;
- incremento de idade mesmo quando a ação anual é `NONE`.

## Próximo boundary

O próximo passo seguro é compor essa ordem com `CareerManagerProgressionRandomStore`, `CareerJuniorDraftDao` e `CareerPlayerRuntimeStore` em uma transação anual. Essa composição deve:

- iterar somente o snapshot original de drafts por clube;
- atualizar os counts seniores imediatamente após cada promoção para as decisões posteriores do mesmo clube;
- chamar o gerador `best.p.d` no ponto exato de refresh/substituição;
- não iterar no mesmo ano um substituto recém-gerado;
- remover promovidos e anexar substitutos somente após a iteração original do clube;
- manter RNG + drafts + jogadores + memberships atômicos;
- testar determinismo, reopen e rollback.

Nenhuma migration adicional foi provada por esta investigação; Room permanece V14.
