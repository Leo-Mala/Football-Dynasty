# FINAL_REPORT — Fase 4R

> Status: **CERTIFICAÇÃO FINAL DO HEAD DE DOCUMENTAÇÃO**. A implementação e os gates técnicos foram aprovados no head `2f8b6d253af521cd472128eae18254b38093d657`. Este fechamento documental alterou o head e, por política de segurança, o mesmo gate deve passar novamente antes do merge.

## Baseline

- ZIP: `Brasfoot.apk_Decompiler.com.zip`;
- SHA-256: `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`;
- package: `com.brasfoot.v2020`;
- versionCode: `202632`;
- versionName: `Brasfoot.202632`;
- minSdk legado: 23;
- targetSdk legado: 36;
- arquivos: 13.889;
- Java: 5.395;
- SMALI: 5.489;
- `.ban`: 1.687.

## Git

- base inicial e final auditada até este fechamento: `phase4/core-game-domain@e1b5c516507fb38012093fff838929d26a6b2459`;
- branch: `phase4r/brasfoot-2026-rebaseline`;
- PR: #3 contra `phase4/core-game-domain`;
- head técnico certificado: `2f8b6d253af521cd472128eae18254b38093d657`;
- workflow certificado: `Phase 4R Validation` run `32693529780` — SUCCESS;
- merge: somente após o head exato contendo este fechamento documental repetir o gate com SUCCESS.

## Engenharia reversa

A serialização `.ban` `e.t/e.g` foi revalidada empiricamente contra todo o novo corpus. Save metadata 26 foi identificado como Java serialization e a carreira principal como Kryo `.s26`. Trechos críticos de load cuja saída Java não é confiável permanecem classificados `SMALI_REQUIRED`; nenhuma equivalência foi inventada. A arquitetura moderna continua sendo o destino, e o APK decompilado permanece referência comportamental/factual.

## Dados

- `.ban` lidos: 1.687/1.687;
- falhas: 0;
- jogadores: 35.432;
- juniores: 4.164;
- hashes físicos únicos: 1.515;
- duplicados byte-identical: 172;
- fingerprint físico agregado: `150643f62e29bcd211a83843239e2ba52721bae1decb6d6021bfa1554f957e1f`;
- fingerprint semântico agregado: `377baca2c9b5f806ab2a94eabbbc34aa2e012681f5ecce8c9a5b19441dff5a37`.

## Saves

Baseline ativo: metadata `*.a26` + carreira `*.s26`; `options.bcf` permanece reconhecido. Não há save real no corpus. Portanto a leitura integral de carreira permanece bloqueada até existir fixture real, sem migração destrutiva nem decoder parcial falso-verde. Formatos `.ai21/.s21/.s121` permanecem apenas como regressão histórica superseded.

## Domínio

Reaproveitados: snapshots imutáveis, adapters, IDs determinísticos, RNG persistível, `CareerState`, repositórios e arquitetura por responsabilidades. Revalidação obrigatória permanece para regras esportivas/calendário/temporada derivadas do legado. Foram substituídas apenas premissas comprovadamente exclusivas do corpus anterior.

## Room

- versão moderna: V2;
- schema export: preservado;
- migração 1→2: preservada e testada;
- V3: não justificada pelo rebaseline;
- `fallbackToDestructiveMigration`: ausente/proibido.

## Build / testes

No head técnico certificado `2f8b6d253af521cd472128eae18254b38093d657`:

- `./gradlew help --no-daemon` — PASS;
- KSP + `copyRoomSchemas` — PASS;
- `testDebugUnitTest` — PASS;
- JUnit: `45` testes, `0` failures, `0` errors, `0` skipped;
- performance: `365` comandos, fingerprint `3a33ba602ed801f94fcfd5ea7ac69556a425b2e340558509c706719a3a743d34`;
- fixture Brasfoot 2026 `Tre Penne` — SHA-256 PASS;
- `assembleDebug` — PASS;
- Room V1/V2 + migração 1→2 — PASS.

A falha original do Kotlin DSL em `sourceSets.test.assets` foi corrigida para AGP 9.x sem downgrade, sem remoção de testes e sem falso verde. A falha posterior de localização do schema V1 no `Migration1To2Test` também foi corrigida na causa, mantendo o teste real de migração.

## Integridade esportiva

Nenhuma fonte esportiva externa foi usada. Nenhum jogador, clube, rating, atributo ou competição foi alterado por conhecimento externo. O corpus oficial desta fase permaneceu exclusivamente `Brasfoot.apk_Decompiler.com.zip`.

## Auditoria de PR

No head técnico certificado:

- mergeable: true;
- 0 commits atrás da base;
- reviews pendentes: 0;
- threads pendentes: 0;
- conflitos: nenhum detectado;
- base permaneceu inalterada.

## Limitações conhecidas

1. ausência de save real `.a26 + .s26` impede certificação end-to-end de import de carreira;
2. equivalência exata do estado aleatório legado não é reivindicada onde o legado não persiste seed/state explicitamente;
3. regras críticas classificadas `SMALI_REQUIRED` devem continuar sendo recuperadas antes de implementação moderna correspondente.

Essas limitações são conhecidas e documentadas; não invalidam o rebaseline do corpus, da serialização `.ban`, da arquitetura moderna ou dos gates atuais.

## Próximo passo

Após o head exato de documentação ficar verde e o PR #3 ser mergeado, iniciar a Fase 5 a partir das lacunas reais do baseline Brasfoot 2026/27, sem reabrir o baseline anterior como autoridade factual.