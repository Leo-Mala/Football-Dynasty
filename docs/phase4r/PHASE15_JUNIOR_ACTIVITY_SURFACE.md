# Fase 15.1 — classificação da superfície `ActivityJuniores`

Status: **SMALI CHARACTERIZED / SUBSTANTIVE SEAMS MAPPED**

Corpus oficial validado nesta auditoria: `Brasfoot.apk_Decompiler.com.zip` — SHA-256 `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465` — package `com.brasfoot.v2020` — versionCode `202632`.

A cópia materializada foi validada pelo SHA-256 antes da leitura. O SMALI é a autoridade executável; o Java da activity foi usado somente como cross-check quando consistente.

## Superfície completa da activity

O SMALI contém os métodos da activity `f`, `h`, `j`, `k`, `e`, `g`, `i`, `l` e `onCreate`, além de listeners sintéticos. Não há outro método substantivo escondido nessa classe.

### `onCreate(Bundle)` — apresentação/ligação

- obtém o clube atual via `ActivityMainTeam.D()`;
- cria o adapter sobre `c0.a0()`;
- instala seleção e três listeners de botões;
- seleciona o primeiro item quando a lista não está vazia;
- chama `e()` para atualizar o cabeçalho.

Não contém RNG, finanças, criação/remoção/promoção de jogador nem mutação esportiva própria.

### `e()` — apresentação

Atualiza somente o `TextView` com a quantidade de juniores e a forma singular/plural. Não contém mutação de carreira.

### `l(String, Int)` — apresentação/roteamento de confirmação

Cria o diálogo de confirmação. O listener positivo roteia:

- código `1` → `i()` (promoção manual);
- código `2` → `g()` (dispensa);
- código `3` → `h()` (peneira confirmada).

O listener negativo apenas cancela o diálogo.

### `f()` — seleção + confirmação de dispensa

Resolve o draft selecionado pelo índice do adapter e, se válido, abre `l(..., 2)`. Não remove estado por conta própria.

### `j()` — validação + confirmação da peneira

- lê o custo em `best.j0.O1[club.O()]`;
- bloqueia por caixa insuficiente;
- bloqueia quando `c0.a0().size() >= 18`;
- caso válido, abre `l(..., 3)`.

A geração e o débito não acontecem aqui; ocorrem apenas após confirmação em `h()`.

### `k()` — validação + confirmação da promoção manual

- bloqueia quando `c0.Z().size() >= 30`;
- resolve o draft selecionado pelo índice do adapter;
- caso válido, abre `l(..., 1)`.

A materialização não acontece aqui; ocorre apenas em `i()`.

### `h()` — seam substantivo da peneira

Executa `best.b.h2(c0)`, mostra feedback, ordena/atualiza a lista visual e, independentemente de quantos drafts foram gerados, chama `c0.D(custo, 9)`. Depois atualiza o cabeçalho.

O comportamento substantivo está mapeado no runtime moderno por `CareerJuniorRuntimeStore.runTrial(...)`: seis gates intercalados com geração imediata, teto 18, despesa raw 9, RNG e finanças atômicos.

### `g()` — seam substantivo da dispensa

Remove somente o `best.p` selecionado de `c0.a0()`. Depois ordena/atualiza a apresentação e reposiciona a seleção quando ainda há itens. Não há RNG nem finanças.

O estado substantivo está mapeado pelo boundary de dispensa V14 em `CareerJuniorRuntimeStore`.

### `i()` — seam substantivo da promoção manual

Chama exatamente `best.t.e(FALSE, p, c0)`. Depois ordena/atualiza a apresentação, marca `ActivityMainTeam.F = TRUE`, reposiciona seleção e atualiza cabeçalho.

A única mutação esportiva da activity nessa rota é a chamada `e(FALSE,...)`; ela está mapeada por `CareerJuniorManualPromotionStore.promote(...)`, com materialização tardia, jogador/runtime/membership/draft/RNG atômicos. `ActivityMainTeam.F` é um sinal de refresh/cache da tela principal, não uma regra de criação/materialização.

## Listeners sintéticos

Os três listeners dos botões chamam, respectivamente, `k()`, `f()` e `j()`. O listener de item apenas muda o índice selecionado no adapter. Os listeners do diálogo somente roteiam para `i/g/h` ou cancelam.

## Classificação para a Fase 15.1

A superfície alcançável de `ActivityJuniores` fica completamente classificada:

- **substantivo e persistido:** `h`, `g`, `i`;
- **validação/controle que protege os mesmos seams:** `j`, `k`, `f`;
- **apresentação/roteamento:** `onCreate`, `e`, `l` e listeners;
- **lifecycle anual:** não é disparado por essa activity; ocorre em `best.b.q()` → `best.p.c(c0)` e está mapeado separadamente por `CareerJuniorAnnualLifecycleStore`.

Ordenação de adapter, Toasts, textos, diálogos, seleção e o flag de refresh visual pertencem à futura superfície Android/UI. Eles não justificam inventar persistência ou gameplay na Fase 15.

Com os boundaries de peneira, dispensa, evolução, promoção manual e lifecycle anual implementados, não permanece um seam de gameplay alcançável desconhecido dentro de `ActivityJuniores`. O fechamento da Fase 15.1 ainda depende dos testes/gates do exact HEAD que contém a regressão close/reopen anual e desta documentação.
