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

Linhas do SMALI do corpus extraído usadas no inventário: 180, 1054, 1489, 1659, 1692, 1714, 1741, 1770, 1799 e 1820.

Os números de linha são auxiliares de auditoria do corpus decompilado e não constituem API estável.

## Sites certificados

### `best.a0.a()`

O trecho relevante executa:

`new Random().nextInt(100) > 30`

Projeção moderna segura:

`LegacyAnnualRandomRules.bestA0AGate(RandomSource)`

Ela reproduz apenas uma chamada `nextInt(100)` e o predicado `> 30`; o efeito esportivo obfuscado subsequente não é nomeado nem executado aqui.

### `best.a0.i()`

O Java decompilado está substituído por `UnsupportedOperationException("Method not decompiled...")`; portanto este site é `SMALI_REQUIRED`.

O SMALI confirma que, dentro de filtros estruturais sobre itens de `E0()` / `Z()`, um candidato que também satisfaz `O() > 50`, `W() < 31` e `O0()==true` executa:

`new Random().nextInt(100) > 25`

Quando passa, o objeto entra numa lista temporária para processamento posterior.

Projeção moderna segura:

`LegacyAnnualRandomRules.bestA0IGate(RandomSource)`

### `best.a0.j(best.c0, boolean, boolean)`

O Java também está integralmente truncado; a caracterização é `SMALI_REQUIRED`.

Os oito sites de RNG usam o mesmo bound `100` e, em ordem no bytecode, os seguintes predicados:

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

A projeção moderna usa `BestA0JRandomSite` com esses thresholds exatos e `LegacyAnnualRandomRules.bestA0JGate(RandomSource, site)`. Os sites são numerados deliberadamente porque o corpus ainda não prova nomes esportivos seguros para cada branch.

### Alcance pelo ciclo anual

`j(...)` não é código morto. O SMALI confirma:

- `best.a0.b(c0, int, boolean)` chama `j(...)` em duas passagens;
- `best.a0.g()` chama `b(...)`;
- `best.a0.h()` chama `b(...)`;
- `best.a0.d()` — já caracterizado na Fase 5 como parte do ciclo anual — executa `g()` e `h()`.

Portanto os oito sites de `j(...)` são alcançáveis pelo caminho anual normal e permanecem dentro do escopo funcional da Fase 6.

## Diferença importante para o legado

Cada `new Random()` legado cria estado próprio a partir do mecanismo padrão de seed da JVM. Não há evidência de que as dez instâncias compartilhem uma seed global persistida.

Assim, seria incorreto afirmar que uma única sequência moderna é bit-a-bit equivalente ao APK. O objetivo moderno é:

1. preservar bound e predicado comprovados por site;
2. tornar a execução reproduzível;
3. persistir o estado quando o sorteio participar do estado da carreira;
4. manter explícita a diferença de estratégia de seed;
5. não inventar significado esportivo para branches ainda obfuscados.

## Próximas caracterizações

A camada de **gates RNG** de `best.a0` está catalogada: 10/10 sites localizados e seus thresholds conhecidos. O trabalho seguinte é caracterizar os efeitos observáveis ao redor desses gates:

1. separar as duas passagens de `b(...)` e seus parâmetros booleanos;
2. mapear o objeto `best.f` criado ao final de `j(...)` e as mutações `n(false)` / `o(false)`;
3. identificar quais mutações precisam ser representadas no domínio moderno e quais são estado derivado;
4. implementar efeitos somente quando entradas, mutações e invariantes forem suficientemente comprovadas;
5. manter toda aleatoriedade por `RandomSource`.

Nenhum dado esportivo foi alterado nesta catalogação.
