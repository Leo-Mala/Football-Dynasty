# RNG SITE CATALOG — Brasfoot 2026/27

## Fonte

Corpus oficial: `Brasfoot.apk_Decompiler.com.zip`  
SHA-256: `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`

A contagem abaixo foi feita no SMALI oficial de `best/a0.smali`, porque o objetivo é localizar instanciações reais de RNG mesmo quando o Java decompilado perde detalhes.

## `best.a0`

Total de `new-instance ... Ljava/util/Random;`: **10**.

| Método legado | Instanciações | Estado |
|---|---:|---|
| `public static a()` | 1 | Java + SMALI confirmam `nextInt(100) > 30` no gate investigado |
| `public static i()` | 1 | site localizado; bound/efeito ainda requer caracterização isolada |
| `private static j(best.c0, boolean, boolean)` | 8 | sites localizados; caracterização detalhada pendente |

Linhas do SMALI do corpus extraído usadas no inventário inicial: 180, 1054, 1489, 1659, 1692, 1714, 1741, 1770, 1799 e 1820.

Os números de linha são auxiliares de auditoria do corpus decompilado e não constituem API estável.

## Site certificado nesta etapa

### `best.a0.a()`

O trecho relevante executa um sorteio com bound 100 e aceita o branch apenas quando o valor sorteado é maior que 30.

Projeção moderna segura:

`LegacyAnnualRandomRules.bestA0AGate(RandomSource)`

Essa projeção reproduz somente:

- uma chamada `nextInt(100)`;
- exatamente um draw lógico no `RandomSource`;
- predicado `> 30`.

Ela deliberadamente não executa nem nomeia o efeito esportivo obfuscado subsequente.

## Diferença importante para o legado

Cada `new Random()` legado cria estado próprio a partir do mecanismo padrão de seed da JVM. Não há evidência de que essas dez instâncias compartilhem uma seed global persistida.

Portanto seria incorreto afirmar que uma única sequência moderna é bit-a-bit equivalente a todos os sorteios do APK. O objetivo moderno é:

1. preservar a distribuição/condição comprovada por site;
2. tornar a execução reproduzível;
3. persistir o estado quando o sorteio fizer parte do estado de carreira;
4. documentar explicitamente a diferença de estratégia de seed.

## Próximas caracterizações

Prioridade:

1. `best.a0.i()` — identificar bounds, branches e mutações;
2. `best.a0.j(c0, boolean, boolean)` — separar os oito sites por caminho de controle;
3. determinar quais sites são alcançados pelo ciclo anual normal;
4. identificar se algum efeito é puramente derivado e não precisa ser persistido;
5. criar uma regra moderna apenas quando draw + bound + condição + efeito observável estiverem suficientemente comprovados.

Nenhum dado esportivo foi alterado nesta catalogação.
