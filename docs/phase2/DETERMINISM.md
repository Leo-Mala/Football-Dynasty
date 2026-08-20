# DETERMINISM — Fase 2

## Objetivo

A reconstrução precisa conseguir distinguir diferença de implementação de simples diferença aleatória. Por isso a Fase 2 introduz uma fronteira de RNG para testes, sem alterar probabilidades ou regras do jogo legado.

## Infraestrutura criada

`foundation/random/RandomSource.kt` define:

- `nextInt(bound)`;
- `nextBoolean()`;
- `nextDouble()`;
- contador de draws.

`SeededRandomSource(seed)` usa `java.util.Random` com seed explícita e registra quantas chamadas foram consumidas.

## O que isso prova hoje

O teste `RandomSourceTest` prova que duas instâncias com a mesma seed geram a mesma sequência de caracterização e o mesmo número de draws.

Isso ainda **não prova paridade do motor de partidas**. O motor legado ainda utiliza seus pontos próprios de `Random`/aleatoriedade, documentados na Fase 1. A abstração atual é infraestrutura para portar cada subsistema posteriormente sem esconder chamadas extras ou mudar a ordem de consumo aleatório.

## Regra para fases seguintes

Ao migrar um método que depende de aleatoriedade:

1. localizar o ponto legado exato no Java/SMALI;
2. identificar quando o RNG é criado ou reutilizado;
3. preservar limites e ordem das chamadas;
4. injetar `RandomSource` somente na versão moderna;
5. comparar quantidade de draws e estado resultante para seed controlada;
6. não ajustar probabilidades para tornar resultados “mais realistas”.

## Pontos ainda não determinísticos

- uso de `new Random()` sem seed explícita no legado;
- tempo/data usado na criação de saves/slots;
- qualquer ordem dependente de coleções cuja iteração não esteja garantida;
- caminhos de UI/threads que possam alterar ordem temporal de operações.

Esses pontos devem ser fechados por subsistema, não globalmente por suposição.

## Fingerprints

`DeterministicFingerprint` produz uma representação SHA-256 do snapshot normalizado de um time legado. Isso protege contra alterações semânticas mesmo quando a fixture é carregada por uma camada diferente.

A fixture atual produz:

`9b0d1878744ce2d64a99db8a4103ba18e8f0286706ec4e30142cd585011d79a6`
