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

### `LegacyFormatProbeTest`

Congela o reconhecimento estrutural dos formatos:

- `.ban` só é aceito como Java Serialization quando o magic `AC ED` está presente;
- `.s21` é identificado como container de carreira conhecido sem declarar que o grafo foi decodificado;
- um `.ban` malformado permanece `UNKNOWN`;
- `LegacySaveReader.readCareer()` continua bloqueado até existir uma fixture real.

O teste impede que reconhecimento de extensão seja confundido com compatibilidade de conteúdo.

### `LegacyDataIntegrityTest`

Valida duas camadas independentes de integridade:

1. SHA-256 byte-for-byte da fixture `.ban`;
2. SHA-256 do fingerprint semântico após desserialização e normalização para snapshot.

Assim, alterações no binário ou no adapter são detectadas separadamente.

## Prova local fora do Gradle Android

Como o runtime desta execução não contém Android SDK/Gradle e não tem acesso externo, os arquivos JVM do harness foram compilados diretamente com `javac` e `kotlinc` e executados contra o `.ban` real.

Resultado da fixture:

```text
team=12 de Octubre
players=20
first=Mauro Cardozo
fingerprint=9b0d1878744ce2d64a99db8a4103ba18e8f0286706ec4e30142cd585011d79a6
draws=8
```

Resultado do probe/boundary adicional:

```text
ban=BAN_JAVA_SERIALIZATION
career=CAREER_KRYO_OR_LEGACY
bad=UNKNOWN
careerReader=blocked
```

A varredura read-only do corpus completo também foi repetida com o mesmo leitor:

```text
files=2689
players=66003
juniors=13098
failures=0
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
