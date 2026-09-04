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

O `new Random()` é um gap material de determinismo que ainda **não pode ser mapeado por inferência** para a stream RNG persistida moderna. Antes de implementar qualquer equivalente, é obrigatório auditar `d4()`, `g4()`, `e4()`, `j2`, `F2`, `F()` e os callers/efeitos necessários para saber qual parte desse método é gameplay persistente, qual é preparação/transição e qual RNG deve ser preservado.

## 5. Persistência / schema

Nenhum novo estado persistente foi provado por esta caracterização. Portanto:

- Room permanece V14;
- não criar migration V15 por causa deste roteador;
- não persistir índices derivados como `G1` apenas porque o legado os reconstrói;
- não substituir `new Random()` por RNG moderno até o consumo e seus efeitos estarem integralmente caracterizados;
- não criar defaults/backfill esportivos.

## 6. Próximo bloco obrigatório

Continuar a Fase 15, sem avançar para a Fase 16, nesta ordem:

1. caracterizar `best.n.m()` e seus efeitos alcançáveis, com atenção especial ao draw `new Random().nextInt(100)`;
2. auditar `best.b.d4()`, `g4()`, `e4()` e flags/rotas que alteram estado;
3. classificar o restante de `best.b.d()` (`P/y0.V0/b.m/r3`, `k0.t.u1`, `l0.w.x0`);
4. só então promover equivalentes modernos, com regressões determinísticas, rollback e save/reopen quando houver estado durável.

A Fase 15.1 Juniores permanece fechada/certificada; esta investigação pertence ao próximo gap material do mesmo Marco C.