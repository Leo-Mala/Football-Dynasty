# Importação Legada `.ban`

## Pipeline

`Legacy .ban -> LegacySerialization -> Legacy snapshot -> LegacyBanToV1Adapter -> validation -> Room transaction -> V1 reconstruction -> fingerprint validation`

A origem é somente leitura. O arquivo legado nunca é escrito ou normalizado.

## Propriedades

- determinística e versionada;
- IDs estáveis;
- idempotente: a segunda importação do mesmo conjunto retorna `ALREADY_CURRENT`;
- transacional: clubes, jogadores, memberships, manifesto e estado final são substituídos atomicamente;
- concorrência coordenada por `Mutex` comum a import/reset;
- leitura, desserialização, hash e adaptação executados em dispatcher de I/O injetável;
- erros técnicos explícitos, com `lastError` sanitizado e limitado;
- logging técnico sem dump do corpus.

## Estados

A persistência usa:

- `NOT_IMPORTED` — nenhum import concluído no escopo;
- `RUNNING` — importação em andamento (`IMPORTING` semanticamente);
- `COMPLETE` — importação concluída (`IMPORTED` semanticamente);
- `FAILED` — tentativa falhou.

## Manifesto

O manifesto V1 armazena `scope`, `adapterVersion`, `schemaVersion`, `sourceCount`, `clubCount`, `seniorCount`, `juniorCount`, `sourceManifestSha256`, `semanticFingerprint` e `importedAtEpochMillis`.

## Reset, rollback e concorrência

O reset remove somente o `importScope` legado e seus metadados. Dados modernos com `importScope = null` sobrevivem. Testes reais comprovam reset/reimport com fingerprint idêntico, rollback provocado dentro da transação e duas importações simultâneas sem duplicação.
