# FINAL_REPORT — Fase 4R

> Status: **EM CERTIFICAÇÃO**. Este arquivo só deve ser marcado como concluído após o Android gate do head final passar e o PR ser re-auditado.

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

- base inicial auditada: `phase4/core-game-domain@e1b5c516507fb38012093fff838929d26a6b2459`;
- branch: `phase4r/brasfoot-2026-rebaseline`;
- PR: #3 contra `phase4/core-game-domain`;
- merge: proibido enquanto este relatório permanecer EM CERTIFICAÇÃO.

## Engenharia reversa

A serialização `.ban` `e.t/e.g` foi revalidada empiricamente contra todo o novo corpus. Save metadata 26 foi identificado como Java serialization e a carreira principal como Kryo `.s26`. Trechos críticos de load cuja saída Java não é confiável permanecem classificados `SMALI_REQUIRED`; não foi inventada equivalência.

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

Baseline ativo: metadata `*.a26` + carreira `*.s26`; `options.bcf` permanece reconhecido. Não há save real no corpus. Portanto a leitura integral de carreira permanece bloqueada até fixture real, sem migração destrutiva ou decoder parcial falso-verde.

## Domínio

Reaproveitados: snapshots imutáveis, adapters, IDs determinísticos, RNG persistível, `CareerState`, repositórios e arquitetura por responsabilidades. Revalidação obrigatória permanece para regras esportivas/calendário/temporada derivadas do legado. Substituídas apenas premissas comprovadamente exclusivas do corpus anterior.

## Room

- versão moderna: V2;
- schema export: preservado;
- migração 1→2: preservada;
- V3: não justificada pelo rebaseline;
- `fallbackToDestructiveMigration`: proibido.

## Build / testes

A falha original do Kotlin DSL em `sourceSets.test.assets` foi corrigida para AGP 9.x. O primeiro gate 4R confirmou `gradlew help` e KSP/Room verdes, e expôs uma falha real de localização do schema V1 no `Migration1To2Test`. A correção mantém o helper de migração e disponibiliza schemas somente para test/debug, nunca release.

O resumo final de JUnit, performance, `assembleDebug` e Room será registrado em `VALIDATION.md` somente após o head final verde.

## Integridade esportiva

Nenhuma fonte esportiva externa foi usada. Nenhum jogador, clube, rating, atributo ou competição foi alterado por conhecimento externo.

## Limitações

1. ausência de save real `.a26 + .s26` impede certificação end-to-end de import de carreira;
2. equivalência exata do estado aleatório legado não é reivindicada;
3. regras críticas ainda classificadas `SMALI_REQUIRED` devem ser recuperadas antes de sua implementação moderna correspondente.

## Próximo passo

Somente após Android gate verde + auditoria de head/base + merge do PR #3: definir a Fase 5 a partir das lacunas reais do baseline 2026/27.
