# DOMAIN_IMPACT — Fase 4R

## Princípio

O corpus `Brasfoot.apk_Decompiler.com.zip` é referência factual/comportamental; a arquitetura Kotlin/Compose/Room continua sendo a implementação de destino. Nenhuma camada moderna foi descartada apenas porque o legado usa Java, SMALI ou serialização própria.

| MODERN_COMPONENT | LEGACY_REFERENCE | STATUS | ACTION | TEST_REQUIRED |
|---|---|---|---|---|
| `LegacySerialization` (`e.t/e.g`) | `.ban` Java Object Serialization | REUTILIZÁVEL / REVALIDADO | manter shell isolado | corpus + fixture |
| `StableLegacyIdentity` | ids/campos legados + ordinal do roster | REUTILIZÁVEL | manter IDs determinísticos | identity tests |
| `DeterministicFingerprint` | snapshot esportivo interpretado | REUTILIZÁVEL | trocar baseline esperado apenas com evidência do corpus 2026 | fixture/integrity |
| `LegacyFormatProbe` | `.a26/.s26` e formatos históricos 21 | ALTERADO | baseline ativo passa a 26; 21 fica regressão histórica | probe tests |
| `LegacySaveReader` | metadata Java + carreira Kryo | REVALIDAÇÃO OBRIGATÓRIA | manter carreira bloqueada sem save real | fixture real futura |
| `CareerState` / `CareerCoreState` | estado central de carreira | REUTILIZÁVEL | nenhuma evidência exige mudança estrutural | determinism/persistence |
| `StatefulJavaRandomSource` | aleatoriedade legada | REUTILIZÁVEL COM DIFERENÇA DOCUMENTADA | manter RNG moderno persistível; não alegar seed-equivalência legado | RNG tests |
| `LegacyCalendarRules` | calendário/regras reconstruídas na Fase 4 | REVALIDAÇÃO OBRIGATÓRIA | preservar até existir divergência comprovada no novo Java/SMALI | characterization |
| Room V1/V2 | persistência moderna | REUTILIZÁVEL | manter V2; não criar V3 sem necessidade real | migration/schema |
| DAOs/repositories | arquitetura moderna | REUTILIZÁVEL | sem alteração | repository tests |
| Compose/UI | implementação moderna | REUTILIZÁVEL | fora do rebaseline factual | assembleDebug |

## Componentes substituídos

Foram substituídas somente premissas comprovadamente ligadas ao baseline antigo:

- package/version metadata do legado;
- contagens e fingerprints do corpus `.ban`;
- fixture factual ativa;
- extensões de save ativas (`.a26/.s26` em lugar de `.ai21/.s21/.s121`).

## Room

O novo legado não introduziu, até esta auditoria, necessidade de entidade/campo persistente novo. O schema moderno permanece V2. Uma V3 só será criada se uma necessidade de persistência for demonstrada por comportamento caracterizado, nunca para espelhar a arquitetura de arquivos do APK.

## Limites de confiança

- leitura completa de carreira `.s26`: não certificada sem fixture real;
- equivalência exata de RNG do legado: não reivindicada;
- regras de calendário/temporada da Fase 4 permanecem sujeitas à revalidação incremental Java↔SMALI antes de expansão funcional.
