# Fase 15 — evidência dos callees anuais restantes de `best.a.J(1)`

Status: **CHARACTERIZED / modern composition pending**

Corpus oficial: `Brasfoot.apk_Decompiler.com.zip` — SHA-256 `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465` — package `com.brasfoot.v2020` — versionCode `202632`.

SMALI é a autoridade executável nesta caracterização.

## 1. Escopo

`best.a.J(1)` já está congelado como dispatcher one-shot da lista anual `c`, com ordem preservada e `clear()` final. Esta rodada aprofunda os callees ainda não classificados, sem inventar equivalência moderna antes de provar seus estados.

## 2. `ds -> best.a.s()`

O SMALI oficial de `best.a.s()` percorre **todos os clubes globais em source order**. Para cada clube:

1. calcula `best.a.D()`, que retorna exatamente `Calendar.MONTH` do calendário da competição;
2. chama `club.Y0(month)`;
3. somente quando esse predicate é verdadeiro, chama `club.z()`.

`best.c0.Y0(month)` percorre `best.b.W()` e retorna `true` quando encontra uma entrada cujo `Calendar.MONTH` é igual ao argumento e para a qual `entry.O(club)` é verdadeiro.

`best.c0.z()` não é presentation-only. Ele executa exatamente:

`club.E(club.q())`

`club.q()` soma, como `long`, o valor retornado por `best.o.m0()` para todos os jogadores de `club.Z()` e `best.p.u()` para todos os drafts juniores da lista `M`.

`best.c0.E(value)` subtrai esse valor do campo long `n` do clube. Se o objeto financeiro `Y` existe e `Q0()` é verdadeiro, também chama `Y.e(value)`.

Consequências:

- `ds/s()` é mutação substantiva anual de clube/finanças;
- não há RNG em `s()`, `Y0()`, `z()`, `q()` ou `E(long)`;
- a ordem global de clubes é observável e deve ser preservada caso a mutação seja composta no runtime moderno;
- não é permitido reduzir esse comando a reset de standings, presentation-only ou no-op;
- antes de implementar, é obrigatório identificar com evidência o equivalente moderno do campo legado `best.c0.n`, de `best.m.e(long)`, das listas senior/junior usadas pelo somatório e do predicate `Q0()`.

## 3. `cw -> best.a.q()`

O SMALI de `best.a.q()` é um wrapper direto:

`best.b.O0().V()`

`best.b.O0()` retorna o campo `N : konrent.b0`, criando `new konrent.b0()` somente quando `N` é nulo e `K1()` é verdadeiro.

`konrent.b0.V()` é um inicializador/reconstrutor substantivo de estrutura tournament/cup. A sequência observada inclui:

- zera referências internas `o/D/E/F/H/G/J/I`;
- consulta competições/tournaments globais `y0/v0/x0/w0/z0/B0`;
- seleciona clubes via `k0.m(0)` e hooks específicos dos tournament types;
- monta uma lista de clubes globais com `J() > 1`;
- executa `Collections.shuffle(list)`;
- preenche slots por `club.J()` 2/3/4/5 quando ainda ausentes;
- quando todos os participantes necessários existem, cria ordem de quatro clubes escolhida por `new Random().nextInt(3)` entre três permutações fixas;
- cria `konrent.a0` e chama `konrent.f0.b(...)` para materializar a estrutura.

Consequências:

- `cw/q()` **consome RNG implícito do APK** e não pode ser conectado ao RNG stateful moderno como se fosse automaticamente a mesma seed;
- `Collections.shuffle()` também usa RNG implícito e sua ordem deve ser tratada junto da política já documentada para randomizações legadas implícitas;
- `cw` permanece `REACHABLE_NOT_IMPLEMENTED` no runtime completo até a estrutura `konrent.b0/a0/f0` ser mapeada para o modelo moderno de competições e sua política RNG ser resolvida sem alegação falsa de seed parity.

## 4. `aj -> best.b.p()`

O SMALI de `best.b.p()` executa dois sweeps anuais em ordem fixa:

1. percorre `best.b.f` e chama `best.o.e()` em cada jogador;
2. depois percorre todos os clubes de `E0()`, cada lista juvenil `club.a0()`, e chama `best.p.b()` em cada draft.

A segunda metade já possui equivalência moderna certificada para `best.p.b()` na Fase 15.1 (`LegacyJuniorRuntimeRules.progressDevelopment(...)` + runtime V14).

A primeira metade **não pode ser considerada coberta pela implementação juvenil**. `best.o.e()` é substantivo: quando `u0()` existe, escolhe entre dois caminhos privados (`s()` quando campo `e < 32`, senão `t()`) e ao final grava `M = FALSE`. Os efeitos internos de `s()/t()` precisam ser auditados separadamente antes de promover o sweep senior.

Consequências:

- `aj/best.b.p()` está parcialmente coberto apenas no sub-sweep juvenil;
- o sweep senior `best.o.e()` permanece gap material da Fase 15;
- a ordem **seniores primeiro -> juniores depois** precisa ser preservada quando o runtime anual for composto.

## 5. `cD -> best.a.p()`

O dispatcher só chama `p()` quando `best.b.Y1()` é verdadeiro. O SMALI de `best.a.p()` é mutação tournament substancial e não presentation-only:

- procura `T0(29)`;
- exige `Y1()==true` e `z0().size()==4`;
- muta a última entrada `konrent.t`, podendo recompor `N0()`/`S0()` com `konrent.o.f0(...)`;
- executa hooks `a1(false)`, `d1(4)`, `f1()`, `h1()`;
- coleta jogadores/clubes das três primeiras entradas;
- completa até 128 por `konrent.o.f0(128-currentCount)`;
- grava o resultado via `best.x.W(list)`;
- ao final sempre limpa `best.b.f1()`.

Antes de implementação é necessário mapear os objetos `best.x/konrent.t` e o estado produzido por `W(list)` para o competition runtime moderno. Nenhuma equivalência é inferida apenas pela semelhança nominal.

## 6. `cS/cSempregado -> best.a.n(boolean)`

`best.a.n(flag)` só executa quando `best.b.N1()` é verdadeiro. Percorre `best.b.H0()` em source order e lê `best.f0.K()`.

- com `flag=false` (`cS`): entra apenas quando `K()==true` **e** `f0.y()==null`;
- com `flag=true` (`cSempregado`): entra sempre que `K()==true`, independentemente de `y()`;
- para cada entrada selecionada chama `best.b.A(f0,false)` e grava o retorno em `best.n.g`;
- como `best.n.g` é sobrescrito a cada match, o valor final é o retorno do **último `f0` elegível em source order**.

Não há RNG no corpo de `n(boolean)`, porém `best.b.A(...)` deve ser auditado antes de declarar o comando determinístico end-to-end.

## 7. Estado da composição `J(1)` após esta rodada

- `dJ -> r()` — já mapeado para `CareerFinanceBorrowingStore.applyMonthlyBorrowingCharges()` e certificado;
- `ds -> s()` — **CHARACTERIZED**, mutação club/value/finance comprovada; modern mapping pendente;
- `cw -> q()` — **CHARACTERIZED**, tournament bootstrap com RNG implícito comprovado; modern mapping pendente;
- `aj -> best.b.p()` — **PARTIALLY_IMPLEMENTED**: juniores certificados, sweep senior `best.o.e()` pendente;
- `cD -> p()` — **CHARACTERIZED / REACHABLE_NOT_IMPLEMENTED**, tournament mutation pendente;
- `cS/cSempregado -> n(boolean)` — dispatcher caracterizado; `best.b.A(...)` é próximo boundary;
- `cO` — no-op no dispatcher, já congelado.

## 8. Persistência

Esta caracterização **não autoriza Room V15**. Nenhum campo novo deve ser congelado antes de provar os equivalentes modernos e quais estados precisam realmente sobreviver a save/reopen.

## 9. Próximos passos

1. auditar `best.o.e()` -> privados `s()/t()` e confrontar com `CareerPlayerRuntimeStore`;
2. auditar `best.b.A(best.f0,false)` e a durabilidade/uso de `best.n.g`;
3. mapear `best.c0.n` + `best.m.e(long)` para o runtime financeiro moderno antes de implementar `ds`;
4. confrontar `konrent.b0` e `best.a.p()` com competition runtime sem aproximar formatos;
5. só então compor `LegacyAnnualJ2CommandRules` com stores reais de forma transacional e atualizar a matriz agregada.
