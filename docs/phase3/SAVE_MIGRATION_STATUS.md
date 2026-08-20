# Status da Migração de Save Legado

## Implementado

- Modern persistence infrastructure: **IMPLEMENTED**
- Modern `CareerDataV1` envelope: **IMPLEMENTED**
- `CareerRepository` + `RoomCareerRepository`: **IMPLEMENTED**
- Legacy `.ban` import: **IMPLEMENTED**
- Legacy save metadata reader: suportado somente no nível já caracterizado

`CareerMetadataEntity` preserva `legacyMetadataFingerprint` e `legacyCareerFingerprint`. Esses campos permanecem `null` quando não existe artefato real que permita calculá-los; nenhum fingerprint é inventado.

## Bloqueio legítimo

Full legacy Kryo career conversion:

**BLOCKED — REAL LEGACY CAREER FIXTURE REQUIRED**

Para desbloquear é necessário um conjunto real compatível, no mínimo:

- `.ai21` de metadata; e
- `.s21` ou `.s121` da carreira correspondente.

Sem essa fixture não é seguro inferir registro Kryo, grafo de objetos, referências pós-load ou compatibilidade completa. `LegacySaveReader.readCareer()` falha explicitamente em vez de fingir suporte.

Esse bloqueio não impede a conclusão da Fase 3, pois a infraestrutura de persistência moderna, o envelope de carreira e a migração `.ban` estão validados.
