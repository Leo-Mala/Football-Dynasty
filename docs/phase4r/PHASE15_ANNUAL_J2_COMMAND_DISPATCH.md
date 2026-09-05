# Fase 15 — dispatcher anual `best.a.J(1)`

Status: **IMPLEMENTED_NEEDS_REVALIDATION / substantive callees remain separate**

Corpus oficial: `Brasfoot.apk_Decompiler.com.zip` — SHA-256 `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465` — package `com.brasfoot.v2020` — versionCode `202632`.

SMALI é a autoridade executável desta caracterização.

## Evidência congelada

A caracterização executável já registrada em `PHASE15_ANNUAL_J2_M0_EVIDENCE.md` prova que o caminho anual `best.b.j2(1)` usa `best.a.x()` e chama `best.a.J(1)` somente quando a lista está não vazia.

`best.a.J(1)` percorre a lista na ordem original e despacha:

- `cw` → `best.a.q()`;
- `ds` → `best.a.s()`;
- `aj` → `best.b.p()`;
- `cD` → `best.a.p()` somente quando `best.b.Y1()` é verdadeiro;
- `dJ` → `best.a.r()`;
- `cS` → `best.a.n(false)`;
- `cO` → no-op no dispatcher;
- `cSempregado` → `best.a.n(true)`;
- strings desconhecidas → no-op no dispatcher.

Depois da iteração, a própria lista é sempre limpa com `clear()`. Não há RNG no dispatcher.

## Implementação moderna

`LegacyAnnualJ2CommandRules` congela apenas esse contrato de controle:

- preserva a ordem recebida;
- representa os sete efeitos chamáveis como intents explícitos;
- aplica o guard `Y1()` somente ao comando `cD`;
- preserva `cO` e desconhecidos como no-op;
- emite `CLEAR_COMMANDS` sempre ao final, inclusive para lista vazia ou composta apenas por no-ops.

A regra não executa `q/s/p/r/n` nem `best.b.p()`. Esses callees continuam independentes na matriz até que seus efeitos e equivalentes modernos sejam comprovados. Portanto esta implementação não inventa competição, calendário ou mutação esportiva.

## Regressões

`LegacyAnnualJ2CommandRulesTest` cobre:

1. a ordem completa dos comandos reconhecidos;
2. o guard `cD` com `Y1=false`;
3. `cO` e strings desconhecidas como no-op;
4. `clear()` one-shot mesmo quando a lista é vazia.

## Persistência

Nenhum estado novo foi criado. O dispatcher não justifica Room V15. A necessidade de persistência de qualquer efeito pertence ao callee substantivo correspondente e só pode ser promovida com evidência própria.

## Próximo passo

Confrontar `q()`, `s()`, `best.b.p()`, `p()`, `r()` e `n(boolean)` com os runtimes/stores existentes. Só compor `j2(1)` no runtime real quando todos os comandos alcançáveis estiverem classificados e suas mutações comprovadas.
