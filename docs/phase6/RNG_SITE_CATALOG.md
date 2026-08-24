# RNG SITE CATALOG — Brasfoot 2026/27

## Fonte

Corpus oficial: `Brasfoot.apk_Decompiler.com.zip`  
SHA-256: `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`

A contagem abaixo foi feita no SMALI oficial de `best/a0.smali`, porque o objetivo é localizar instanciações reais de RNG mesmo quando o Java decompilado perde detalhes.

## `best.a0`

Total de `new-instance ... Ljava/util/Random;`: **10**.

| Método legado | Instanciações | Estado |
|---|---:|---|
| `public static a()` | 1 | Java + SMALI confirmam `nextInt(100) > 30` |
| `public static i()` | 1 | SMALI confirma `nextInt(100) > 25`; Java está truncado |
| `private static j(best.c0, boolean, boolean)` | 8 | SMALI confirma bound 100 e oito thresholds; Java está truncado |

Linhas do SMALI do corpus extraído usadas no inventário original: 180, 1054, 1489, 1659, 1692, 1714, 1741, 1770, 1799 e 1820. Os números de linha são auxiliares de auditoria do corpus decompilado e não constituem API estável.

## Sites certificados

### `best.a0.a()`

Trecho relevante:

`new Random().nextInt(100) > 30`

Projeção: `LegacyAnnualRandomRules.bestA0AGate(RandomSource)`.

### `best.a0.i()`

O Java está truncado; o site é `SMALI_REQUIRED`. O SMALI confirma que o draw só é alcançado depois de `O()>50`, `W()<31` e `O0()==true`:

`new Random().nextInt(100) > 25`

Projeção: `LegacyAnnualA0IRules` + `LegacyAnnualSelectionRules.bestA0IPlayerEligible`.

### `best.a0.j(best.c0, boolean, boolean)`

O Java está integralmente truncado; a caracterização é `SMALI_REQUIRED`.

Os oito sites usam bound `100` e, em ordem no bytecode:

| Site | Predicado comprovado |
|---|---|
| `SITE_1` | `nextInt(100) > 10` |
| `SITE_2` | `nextInt(100) > 90` |
| `SITE_3` | `nextInt(100) > 30` |
| `SITE_4` | `nextInt(100) > 30` |
| `SITE_5` | `nextInt(100) > 35` |
| `SITE_6` | `nextInt(100) > 45` |
| `SITE_7` | `nextInt(100) > 75` |
| `SITE_8` | `nextInt(100) > 95` |

A projeção usa `BestA0JRandomSite` e `bestA0JGate(RandomSource, site)`.

### Alcance pelo ciclo anual

`j(...)` não é código morto:

- `best.a0.b(c0,int,boolean)` chama `j(...)` em passagens high/low;
- `g()` e `h()` chamam `b(...)`;
- `best.a0.d()` faz parte do ciclo anual e executa `g()`/`h()`.

Logo os oito sites pertencem ao caminho anual normal.

## RNG transitivo em `best.f`

O caminho selecionado por `j(...)` constrói `best.f`, portanto também foram caracterizados:

- gate de construtor `nextInt(100) > 10` nos branches alcançáveis;
- gate `nextInt(100) <= 60` de `best.f.n()` apenas quando a ternária/OR não short-circuita;
- `Collections.shuffle(...)` em `best.f.q()/p()` e caminhos relacionados.

O branch `subject.O0() && current.Q0()` em `best.f.n()` seleciona a rota alternativa sem alcançar o RNG. Da mesma forma, `subject.O()<=30` e `current.Q0()==false` evitam o draw. Esse detalhe está congelado por teste de draw count.

## Diferença importante para o legado

Cada `new Random()` legado cria estado próprio a partir do mecanismo padrão da JVM. Não há evidência de seed global persistida compartilhada pelas instâncias. `Collections.shuffle(List)` também usa fonte implícita.

Assim, a reconstrução moderna não declara equivalência bit-a-bit de seed. Ela preserva:

1. bound e predicado por site;
2. ordem e short-circuit comprovados;
3. distribuição do shuffle;
4. execução reproduzível;
5. estado persistível quando o sorteio participa da carreira.

## Estado final da Fase 6

A camada de RNG do caminho anual está catalogada no escopo necessário desta fase. Os efeitos ao redor dos sorteios foram reconstruídos em regras separadas para seleção, manutenção mínima, `best.a0.i`, orquestração e plano anual `T1`.

O gerador procedural `best.p.d(...)` possui RNG adicional próprio e foi isolado como subsistema da Fase 7. Ele não é parcialmente reproduzido aqui para evitar fabricação de atributos.

Nenhum dado esportivo foi alterado nesta catalogação.
