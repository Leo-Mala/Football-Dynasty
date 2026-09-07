# Fase 15 — evidência do roteador anual e entrada do lifecycle

Status: **CHARACTERIZED / IMPLEMENTATION GAPS REMAIN**

Corpus oficial: `Brasfoot.apk_Decompiler.com.zip` — SHA-256 `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465` — package `com.brasfoot.v2020` — versionCode `202632`.

Regra de autoridade: SMALI executável prevalece sobre Java decompilado quando houver divergência.

## 1. `ActivityFimAno.e()` — entrada anual comprovada

Java e SMALI concordam que o helper privado `e()` executa exatamente, nesta ordem:

1. `best.b.z3(true)`;
2. `best.n.n()`;
3. `finish()`.

`best.b.z3(boolean)` grava diretamente o campo booleano `L`, e `best.b.E1()` devolve esse mesmo campo. Portanto `ActivityFimAno.e()` arma explicitamente a rota anual antes de chamar `best.n.n()`.

No `onCreate()`, se `best.b.j2()` já estiver ativo, o código executa `best.b.K2(false)` e entra imediatamente em `e()`; no outro ramo a entrada em `e()` ocorre pelo callback de apresentação/interstitial. Essa diferença é de roteamento/apresentação e não altera a ordem substantiva acima.

Não se classifica a Activity inteira como `PRESENTATION_ONLY` nesta etapa: outros helpers ainda chamam `j0.D0()` / `j0.W0()` e precisam ser classificados separadamente antes de encerrar toda a superfície.

## 2. `best.n.n()` — roteador fino comprovado

O SMALI oficial de `best.n.n()` comprova exatamente:

- se `best.b.E1()` for `false`, chama `best.n.i()` e retorna;
- se `best.b.E1()` for `true`, chama `best.b.d()` e retorna.

No caminho vindo de `ActivityFimAno.e()`, `L` acabou de ser gravado como `true`; logo a cadeia alcançável substantiva é:

`ActivityFimAno.e()` → `best.b.z3(true)` → `best.n.n()` → `best.b.d()`.

`best.n.n()` não cria RNG nem aplica diretamente a mutação anual; ele escolhe uma das duas rotas. O ramo `best.n.i()` permanece fora do escopo desta entrada anual específica e deve ser tratado pelo seu próprio reachability/caller audit, sem substituir o comportamento comprovado por um equivalente de UI inferido.

## 3. `best.b.d()` — ordem executável do `NovoAno`

Java e SMALI oficiais concordam na seguinte ordem de alto nível:

1. `this.c = 0L`;
2. se `this.t1 != null`: `this.t1.l1()` e depois `this.t1.m3(0)`;
3. para todos os clubes de `this.b.g1()`: `club.l1()`;
4. `best.n.m()`;
5. `this.o2()`;
6. `this.q()`;
7. `e3(false)`;
8. `i3(false)`;
9. `J3(false)`;
10. `B3(false)`;
11. `bu = 1`;
12. escolhe o ramo `P` / `y0.V0` / `this.b.m()` e chama `r3(7)` ou `r3(1)` conforme o ramo;
13. `K3(false)`;
14. se `k0.t.y0() && k0.t.C0()`: `k0.t.u1()`;
15. se `l0.w != null`, aplica os `x0(2)` condicionais comprovados no SMALI.

Esta caracterização **não** atribui significado moderno aos métodos ainda opacos. Ela congela apenas a ordem e os efeitos diretamente demonstráveis.

### Etapas já promovidas

- `c0.l1()` → `m.z()` finance-period reset: já implementado no boundary moderno e coberto por regressões;
- `q()` → lifecycle anual de Juniores: Fase 15.1 certificada no exact head `85cac622867779150695d7348379156691b79445`;
- `o2()` reconstrói o `HashMap G1` por ID/W de clubes a partir das listas legadas `h` e `D`; isso é reconstrução de índice derivado e, isoladamente, não justifica novo estado persistente.

## 4. Novo blocker material encontrado em `best.n.m()`

A auditoria direta de Java+SMALI mostra que `best.n.m()` é alcançável dentro de `best.b.d()` e contém:

- leitura de `P0()`;
- chamadas condicionais `d4()` e `e4()` protegidas por `try/catch`;
- **`new java.util.Random().nextInt(100)`**;
- chamada de `best.b.g4()` quando o draw é `> 50`;
- `j2(1)`;
- possível `F2(true)` quando o valor inicial de `P0()` é zero;
- roteamento posterior dependente de `V0` e `E1()`.

O `new Random()` é um gap material de determinismo que ainda **não pode ser mapeado por inferência** para a stream RNG persistida moderna.

### 4.1 `best.b.d4()` — fila temporal `I`

Java e SMALI concordam que `d4()`:

1. percorre a lista `I` (`components.o2`);
2. seleciona entradas cuja data `o2.b()` seja anterior a `best.b.M()`;
3. quando jogador `o2.a()` e clube `o2.c()` não são nulos, chama `best.o.U1(club)`;
4. adiciona a entrada vencida a uma lista temporária;
5. depois da varredura, remove da lista `I` todas as entradas temporárias.

O corpo é protegido por `try/catch` por item no legado. `components.o2` é serializável e seu construtor cria a data a partir do calendário atual do jogo mais 319 dias. Portanto esta etapa é mutação temporal persistível e não pode ser descartada como apresentação.

### 4.2 `best.b.e4()` — fila temporal `J`

Java e SMALI concordam que `e4()`:

1. percorre `J` (`components.y1`);
2. para cada entrada cuja data `y1.b()` seja anterior a `best.b.M()`, chama `y1.a()`;
3. `y1.a()` percorre quatro slots inteiros e, para cada valor positivo, chama `best.k.h(index, value)` e zera o slot;
4. a entrada vencida é então removida da lista `J` depois da varredura.

Também é estado temporal substantivo e requer mapeamento separado antes do fechamento do NovoAno.

### 4.3 `best.b.g4()` — efeito condicionado pelo draw bruto

O SMALI de `g4()` percorre todos os clubes de `g1()` e todos os jogadores de `club.Z()`. Para cada jogador cujo `best.o.w()` seja `true`, instancia:

`components.n3(player, player.z0(), true, true)`.

Com o quarto argumento `true`, o construtor `components.n3` não executa a contagem alternativa de clubes; ele testa `requestedValue <= player.A0() + round(player.A0() * 0.1)`. Quando o teste passa, instancia `best.f(player, requestedValue, true, false, 0)`, captura clube/valor resultantes e, se houver clube, chama `player.T1(club, value, true, false, false)`.

Logo o draw `new Random().nextInt(100) > 50` não controla apenas apresentação: ele habilita uma varredura capaz de mutar jogadores/clubes por meio de `n3 → best.f → best.o.T1(...)`. A semântica esportiva nominal desses tipos continua não inferida aqui; a cadeia executável é o que está congelado.

Esse resultado torna obrigatório caracterizar a RNG e os efeitos `best.f` / `best.o.T1` antes de implementar `best.n.m()` no runtime moderno.

## 5. Persistência / schema

Nenhum **novo formato** persistente foi provado por esta caracterização, embora `d4/e4/g4` atinjam estado substantivo que pode exigir equivalentes nas estruturas modernas já existentes. Portanto:

- Room permanece V14;
- não criar migration V15 somente por causa deste roteador;
- não persistir índices derivados como `G1` apenas porque o legado os reconstrói;
- não substituir `new Random()` por RNG moderno até o consumo e seus efeitos estarem integralmente caracterizados;
- não criar defaults/backfill esportivos;
- auditar primeiro se as filas temporais `I/J` possuem equivalentes modernos persistidos antes de propor qualquer schema adicional.

## 6. Próximo bloco obrigatório

Continuar a Fase 15, sem avançar para a Fase 16, nesta ordem:

1. caracterizar `best.f` e `best.o.T1(...)` no caminho exato de `g4()` para fechar o efeito do draw bruto;
2. localizar equivalentes modernos, se existentes, para as filas temporais `I/J` antes de considerar persistência nova;
3. auditar `j2`, `F2`, `F()` e o restante do roteamento de `best.n.m()`;
4. classificar o restante de `best.b.d()` (`P/y0.V0/b.m/r3`, `k0.t.u1`, `l0.w.x0`);
5. só então promover equivalentes modernos, com regressões determinísticas, rollback e save/reopen quando houver estado durável.

A Fase 15.1 Juniores permanece fechada/certificada; esta investigação pertence ao próximo gap material do mesmo Marco C.