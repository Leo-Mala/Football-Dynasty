# CHARACTERIZATION_TESTS — Fase 2

## Princípio

Os testes desta fase não tentam provar que uma futura reescrita é correta. Eles congelam observações verificáveis do legado para que futuras mudanças possam ser comparadas contra uma referência estável.

## Testes adicionados

### `LegacyBanCharacterizationTest`

Usa uma fixture `.ban` real do APK e valida:

- identidade do clube;
- `fileRef`;
- país legado;
- tamanho do elenco;
- quantidade de juniores;
- nome/idade/posição do primeiro jogador;
- fingerprint determinístico do snapshot inteiro.

Isso testa simultaneamente o contrato de Java Serialization, os shells `e.t/e.g`, a conversão read-only e o fingerprint.

### `LegacySchemaCatalogTest`

Congela as identidades dos cinco tipos centrais mapeados na Fase 1:

- jogador: `a.p`;
- clube: `a.ac`;
- partida: `a.t`;
- competição/liga: `d.q`;
- carreira raiz: `a.b`.

Também mantém alguns campos confirmados como sentinelas de drift documental. Não inventa comportamento desses tipos.

### `RandomSourceTest`

Valida que `SeededRandomSource` produz a mesma sequência com a mesma seed e contabiliza o número de draws. Ele ainda não substitui nenhuma chamada aleatória do jogo; serve como ponto de instrumentação para testes de paridade futuros.

## Prova local fora do Gradle Android

Como o runtime desta execução não contém Android SDK/Gradle e não tem acesso externo, os arquivos JVM do harness foram compilados diretamente com `javac` e `kotlinc` e executados contra o `.ban` real.

Resultado:

```text
team=12 de Octubre
players=20
first=Mauro Cardozo
fingerprint=9b0d1878744ce2d64a99db8a4103ba18e8f0286706ec4e30142cd585011d79a6
draws=8
```

## O que ainda NÃO está caracterizado

Não há nesta fase uma fixture real de carreira `.ai21` + `.s21/.s121`. Por isso ainda não há teste de round-trip ou load completo para `a.b`, `a.p`, `a.ac`, `a.t` e `d.q` serializados como uma carreira real.

O catálogo desses tipos é um gate estrutural, não uma falsa alegação de paridade comportamental.

## Próximo nível de testes

Quando houver um save real, criar fixtures de caracterização para:

1. metadado `.ai21`;
2. load do agregado `a.b`;
3. reconstrução das referências transitórias;
4. snapshots de clube/jogador/competição/partida antes e depois do load;
5. cenários de RNG capturados do motor legado;
6. comparação de transições de estado por subsistema.
