# PERSISTENCE — Fase 1

## Formatos confirmados

| Arquivo | Conteúdo | Serialização | Local |
|---|---|---|---|
| `options.bcf` | `est.Options` | Java `ObjectOutputStream` | `Context.getFilesDir()` |
| `*.ai21` | `InfoArquivoSalvoType` (metadado/listagem) | Java serialization | `getFilesDir()` |
| `*.s21` / `*.s121` | grafo principal `c.a.TF` (`a.b`) | Kryo no caminho moderno de `rX()`; há caminho legado Java serialization em `save()` | `getFilesDir()` |
| `*.sbck` | cópia binária de backup do save principal | `FileChannel.transferFrom` | `getFilesDir()` |
| formatos antigos como `.a17` | metadado legado | Java serialization | trecho desktop/compatibilidade em `c.a.av()` |

## Save confirmado (`ActivitySave`)

1. Lista os arquivos `*.ai21` em `getFilesDir()`.
2. Novo slot recebe base `bf<timestamp>`.
3. `rF()` grava `options.bcf` com Java serialization.
4. Antes de serializar, chama `c.b.wC()`, `wB()`, `wE()` para preparar/reindexar referências.
5. `rX()` grava o grafo `c.a.TF` com Kryo (`registrationRequired=false`) em arquivo principal.
6. Atualiza `InfoArquivoSalvoType` com o caminho e grava o metadado em `*.ai21` via `ObjectOutputStream`.
7. Cria/atualiza `*.sbck` copiando o arquivo principal.
8. Existe ainda `save()` que grava metadado + carreira via Java serialization no mesmo stream; deve ser tratado como caminho legado/compatibilidade até rastreamento completo das chamadas.

## Load confirmado (`ActivityLoad` / `c.a`)

1. Lista `*.ai21` e lê `InfoArquivoSalvoType` para apresentar slots.
2. `qP()` carrega `options.bcf`.
3. O método `qS()` está perdido no Java e foi localizado em SMALI; ele é crítico para confirmar qual caminho (`Kryo`, backup ou compatibilidade) é usado no load principal.
4. `qT()` mostra um caminho Java-serialization que lê `a.b` e depois reconstrói referências.
5. Após load executa rotinas de reconstrução: `f.b.wD()`, `f.b.wA()`, `f.b.wF()`, `TF.da()`, `v.g(context)`, `TF.p(true)`, `o.hv()`.

## Implicação para migração

Room **não deve substituir diretamente** este formato na Fase 2. Primeiro é necessário um `LegacyPersistenceAdapter` capaz de ler os saves existentes e reconstruir campos transitórios de forma idêntica. Só então pode existir um novo formato interno/versionado.