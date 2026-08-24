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
| `private static j(best.c0, boolean, boolean)` | 8 | sites localizados; caracterização detalhada pendente |

Linhas do SMALI do corpus extraído usadas no inventário inicial: 180, 1054, 1489, 1659, 1692, 1714, 1741, 1770, 1799 e 1820.

Os números de linha são auxiliares de auditoria do corpus decompilado e não constituem API estável.

## Sites certificados nesta etapa

### `best.a0.a()`

O trecho relevante executa um sorteio com bound 100 e aceita o branch apenas quando o valor sorteado é maior que 30.

Projeção moderna segura:

`LegacyAnnualRandomRules.bestA0AGate(RandomSource)`

Essa projeção reproduz somente:

- uma chamada `nextInt(100)`;
- exatamente um draw lógico no `RandomSource`;
- predicado `> 30`.

Ela deliberadamente não executa nem nomeia o efeito esportivo obfuscado subsequente.

### `best.a0.i()`

O Java decompilado está substituído por `UnsupportedOperationException("Method not decompiled...")`; portanto este site é `SMALI_REQUIRED`.

O SMALI confirma que, dentro de filtros estruturais sobre itens de `E0()` / `Z()`, um candidato que também satisfaz `O() > 50`, `W() < 31` e `O0()==true` executa:

`new Random().nextInt(100) > 25`

Quando o predicado passa, o objeto é adicionado a uma lista temporária para processamento posterior.

Projeção moderna segura:

`LegacyAnnualRandomRules.bestA0IGate(RandomSource)`

A projeção cobre apenas o sorteio comprovado e não atribui nome esportivo ao objeto ou ao processamento subsequente.

## Diferença importante para o legado

Cada `new Random()` legado cria estado próprio a partir do mecanismo padrão de seed da JVM. Não há evidência de que essas dez instâncias compartilhem uma seed global persistida.

Portanto seria incorreto afirmar que uma única sequência moderna é bit-a-bit equivalente a todos os sorteios do APK. O objetivo moderno é:

1. preservar a distribuição/condição comprovada por site;
2. tornar a execução reproduzível;
3. persistir o estado quando o sorteio fizer parte do estado de carreira;
4. documentar explicitamente a diferença de estratégia de seed.

## Próximas caracterizações

Prioridade:

1. `best.a0.j(c0, boolean, boolean)` — separar os oito sites por caminho de controle;
2. determinar quais desses oito sites são alcançados pelo ciclo anual normal;
3. identificar bounds, predicates e efeitos observáveis em cada branch;
4. identificar se algum efeito é puramente derivado e não precisa ser persistido;
5. criar regra moderna apenas quando draw + bound + condição + efeito observável estiverem suficientemente comprovados.

Nenhum dado esportivo foi alterado nesta catalogação.
