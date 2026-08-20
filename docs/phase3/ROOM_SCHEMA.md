# Room Schema V1

## Database

- Classe: `FootballDynastyDatabase`
- Versão: `1`
- `exportSchema = true`
- Schema versionado: `app/schemas/com.leomala.footballdynasty.data.local.FootballDynastyDatabase/1.json`
- Identity hash final: `37c2e4df984290903730a25553bdbed5`

## Tabelas

1. `clubs` — PK `id`; índice UNIQUE `sourceFileRef`; índice `importScope`.
2. `players` — PK `id`; índice `importScope`.
3. `squad_memberships` — PK `playerId`; FK `playerId -> players.id` com DELETE CASCADE; FK `clubId -> clubs.id` com DELETE CASCADE; índice `clubId`; índice UNIQUE `(clubId, rosterKind, sourceOrdinal)`.
4. `legacy_import_state` — PK `scope`.
5. `legacy_import_manifest` — PK `scope`; contém `sourceCount`, `clubCount`, `seniorCount`, `juniorCount`, manifesto e fingerprint.
6. `career_metadata` — PK `id`; contém `legacyMetadataFingerprint` e `legacyCareerFingerprint` opcionais.

O schema final contém `seniorCount` e não contém o nome antigo/ambíguo `playerCount` no manifesto.

## Migrações

`FootballDynastyMigrations.ALL` existe explicitamente e está vazio no V1 porque este é o primeiro banco moderno. Não existe `Migration 0 -> 1` artificial. Toda versão futura deve acrescentar migration explícita.

`fallbackToDestructiveMigration()` não é usado.

## Gate

O schema foi regenerado pelo compiler Room no gate final e versionado em `1da01d2cabe93447b63414fa7620d9e53f0797d2`. A auditoria automatizada verificou versão, `seniorCount`, ausência de `playerCount` e ambos os fingerprints de carreira.
