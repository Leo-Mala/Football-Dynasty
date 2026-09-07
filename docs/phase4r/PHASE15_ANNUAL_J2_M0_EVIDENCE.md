# Fase 15 — evidência executável de `j2(1)`, `best.a.J(1)` e `M0/E1`

Status: **CHARACTERIZED / runtime composition pending**

Corpus oficial: `Brasfoot.apk_Decompiler.com.zip` — SHA-256 `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465` — package `com.brasfoot.v2020` — versionCode `202632`.

SMALI é a autoridade executável nesta caracterização.

## 1. `best.b.j2(1)`

O SMALI de `best.b.j2(I)` tem dois ramos. Para o argumento anual comprovado `1`, percorre `G[0..d]`. Para cada `best.a`:

1. obtém `best.a.x()`;
2. só continua se a lista for não vazia;
3. chama exatamente `best.a.J(1)`;
4. continua para a próxima competição.

Não há RNG no dispatcher `j2`.

O ramo `j2(0)` usa `best.a.w()` e `best.a.J(0)`; ele não é confundido com o caminho anual atual.

## 2. `best.a.w()` e `best.a.x()`

O SMALI prova que:

- `w()` retorna o campo `b : ArrayList<String>`;
- `x()` retorna o campo `c : ArrayList<String>`.

Portanto `j2(1)` opera exclusivamente sobre a fila/lista `c` de cada `best.a`.

## 3. `best.a.J(1)`

`J(I)` escolhe a lista de comandos por argumento:

- `0` → campo `b` (`w()`);
- diferente de `0` → campo `c` (`x()`).

No caminho anual `J(1)`, o método percorre a lista `c` na ordem existente e despacha cada string exatamente assim:

- `"cw"` → `best.a.q()`;
- `"ds"` → `best.a.s()`;
- `"aj"` → `best.b.p()`;
- `"cD"` → somente quando `best.b.Y1()` é verdadeiro, `best.a.p()`;
- `"dJ"` → `best.a.r()`;
- `"cS"` → `best.a.n(false)`;
- `"cO"` → nenhum efeito no próprio dispatcher; segue para o próximo item;
- `"cSempregado"` → `best.a.n(true)`.

Strings não reconhecidas também não produzem mutação no dispatcher.

Após percorrer a lista, `J(1)` chama `clear()` na própria lista `c`. Esse clear é parte do comportamento alcançável e deve ser preservado pelo equivalente moderno; não é permitido reprocessar esses comandos numa próxima passagem anual.

Não há RNG dentro de `J(1)` propriamente dito. Callees podem ter seus próprios efeitos e devem permanecer classificados separadamente.

## 4. Efeitos já visíveis dos callees de `J(1)`

A inspeção SMALI desta rodada confirma pelo menos os seguintes boundaries, sem promover ainda equivalência moderna inteira:

- `q()` chama `best.b.O0().V()`;
- `s()` percorre todos os clubes e, para clubes em que `Y0(best.a.D())` é verdadeiro, chama `best.c0.z()`;
- `r()` percorre todos os clubes e, quando `club.T()` existe e `club.T().o() > 0`, chama `club.D(value, 4)`;
- `n(boolean)` percorre `best.b.H0()` e condiciona o processamento de `best.f0` ao flag recebido e ao estado `f0.K()` / `f0.y()`; quando selecionado, chama `best.b.A(f0,false)` e grava o resultado na lista estática `best.n.g`;
- `p()` contém mutações substanciais de competição/tournament e não deve ser reduzido a presentation-only.

Assim, `j2(1)` deixa de ser `UNKNOWN`: seu dispatcher está completamente caracterizado, mas a composição moderna continua bloqueada até cada command code alcançável ter equivalência moderna comprovada.

## 5. `M0`, `E1()` e `F2(boolean)`

A auditoria corpus-wide de todas as referências SMALI ao campo `best.b.M0:Z` encontrou somente `best/b.smali`.

O getter é executavelmente inequívoco:

```text
best.b.E1() -> iget-boolean M0 -> return
```

O setter é igualmente direto:

```text
best.b.F2(value) -> iput-boolean value, M0
```

Portanto `M0` **é exatamente o backing field do getter `E1()`**. Não é correto tratá-los como estados distintos.

Além do valor inicial do construtor, o fluxo anual contém escrita explícita `M0=false` no final de `best.b.d()` antes de chamar `best.n.n()`.

`best.n.n()` lê `E1()`:

- `E1()==false` → `best.n.i()`;
- `E1()==true` → `best.b.d()`.

Separadamente, `best.n.m()` captura `P0()` no início e, quando o snapshot original é `0`, chama `F2(true)`, isto é, grava `M0=true`. Depois lê `E1()` (o mesmo campo `M0`) para decidir seus ramos finais em conjunto com `V0`.

Isso fecha a identidade funcional do antigo blocker `M0/F2/E1`: trata-se de um flag de roteamento do lifecycle anual, não de um novo subsistema persistente independente.

## 6. Consequência para persistência

Esta evidência **não justifica Room V15** por si só.

Antes de adicionar qualquer campo persistente moderno, é necessário provar se o equivalente moderno do roteamento anual precisa sobreviver a um save/reopen exatamente no meio desse boundary. A simples existência do campo serializado legado não autoriza schema novo quando o moderno pode representar o mesmo efeito como estado de transição já atômico.

Nenhum backfill/default esportivo deve ser inventado.

## 7. Classificação recomendada para a matriz

- `best.b.j2(1)` → `CHARACTERIZED` no dispatcher; runtime composition pending;
- `best.a.J(1)` → `CHARACTERIZED`; command dispatch e clear final comprovados;
- `best.b.F2(true)` / `M0` / `E1()` → `CHARACTERIZED`; identidade do flag fechada;
- callees `q/s/p/r/n` → manter classificação individual até equivalência moderna/testes serem provados.

## 8. Próximo passo

1. mapear cada code alcançável da lista `best.a.x()` para writers/call paths oficiais;
2. confrontar `q/s/p/r/n` com os stores/rules modernos já existentes;
3. implementar somente os gaps reais, preservando ordem da lista e `clear()` após processamento;
4. adicionar regressões de ordem, comando desconhecido/no-op e clear one-shot;
5. depois fechar os callees de `best.b.F()` e a composição `best.f` antes de concluir a Fase 15.
