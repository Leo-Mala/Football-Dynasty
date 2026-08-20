# Modelo Versionado V1

## Versão

`DATA_SCHEMA_V1 = 1` é a primeira versão do contrato moderno de migração/persistência.

## DTOs

- `PlayerDataV1`: identidade estável, vínculo de origem, ordem do elenco, atributos legados e campos opacos necessários à preservação.
- `ClubDataV1`: identidade estável, `sourceFileRef`, dados legados do clube e roster ordenado.
- `CompetitionDataV1`: contrato versionado disponível sem forçar persistência Room antes de existir uma fonte caracterizada.
- `MatchDataV1`: contrato versionado disponível sem criar tabela artificial nesta fase.
- `CareerDataV1`: envelope moderno de carreira, incluindo fingerprints legados opcionais.

## Separação de camadas

DTO versionado é o contrato de migração. Domain model é a representação de negócio sem dependência de infraestrutura. Room Entity é exclusivamente persistência local. O adapter faz a tradução explícita entre essas fronteiras.

A camada de domínio não importa Room, SQLite, Android `Context`, Java serialization, Kryo, DAOs nem classes ofuscadas legadas.

## Evolução V2+

O V1 foi congelado somente após geração real do schema Room no gate final. Mudanças futuras de schema devem criar uma nova versão e uma `Migration` explícita; não é permitido reinterpretar silenciosamente o V1 nem usar destructive fallback.
