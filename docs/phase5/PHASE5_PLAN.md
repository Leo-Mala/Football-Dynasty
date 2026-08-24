# FASE 5 — PARIDADE DO CICLO DE TEMPORADA BRASFOOT 2026/27

## Baseline

Esta fase parte exclusivamente do merge certificado da Fase 4R:

- base: `phase4/core-game-domain@aad0327c4be35fbc906ee30d93ed89ce8c1b8765`;
- corpus legado oficial: `Brasfoot.apk_Decompiler.com.zip`;
- SHA-256 do corpus: `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`;
- package legado: `com.brasfoot.v2020`;
- versionCode: `202632`;
- versionName: `Brasfoot.202632`.

O baseline anterior permanece superseded e não pode voltar a ser autoridade factual.

## Objetivo

Reconstruir e testar com confiança o ciclo de encerramento e abertura de temporada do Brasfoot 2026/27, preservando a arquitetura moderna Kotlin/Room e sem introduzir dados esportivos externos.

A prioridade é transformar comportamento observado no Java/SMALI em transições de domínio modernas, determinísticas e testáveis. O objetivo não é copiar a arquitetura legada.

## Escopo inicial comprovado

A auditoria direta do novo corpus identificou:

- `com.brasfoot.v2020.ActivityFimAno` como tela/orquestração de fim de temporada;
- `best.n.n()` como roteamento que chama `best.b.d()` quando o estado de novo ano está pendente;
- `best.b.d()` como orquestração principal de novo ano;
- `best.b.l()` como reconstrução do calendário;
- `best.b.l()` usa base `2026` para esta geração e reconstrói o calendário com `2026 + (season - 1)` quando o modo legado correspondente não é o modo 2022;
- `ActivityFimAno.e()` marca estado de transição e reentra no roteador de novo ano;
- `ActivityFimAno.onCreate()` executa manutenção de fim de ano antes da continuidade.

## Gates da fase

1. catalogar a sequência de `ActivityFimAno -> best.n -> best.b` em Java;
2. confirmar em SMALI qualquer método truncado, suspeito ou semanticamente crítico;
3. classificar cada etapa por nível de confiança;
4. revalidar `LegacyCalendarRules.BASE_YEAR = 2026` contra o novo corpus;
5. impedir transições modernas que contradigam invariantes comprovadas;
6. preservar RNG persistível e identidade determinística;
7. manter Room V2 enquanto nenhum novo estado persistente for demonstrado;
8. se V3 se tornar necessária, criar migração V2->V3 e teste real;
9. preservar todos os gates da Fase 4R;
10. obter `gradlew help`, KSP/Room, unit tests e `assembleDebug` verdes no head exato.

## Restrições

- nenhum jogador, clube, rating, atributo, elenco ou competição será atualizado por fonte externa;
- nenhum método Java truncado será tratado como verdade sem SMALI;
- nenhuma expected value será alterada apenas para fazer testes passarem;
- nenhum `fallbackToDestructiveMigration`;
- nenhuma leitura de `.s26` será declarada funcional sem fixture real de carreira.

## Critério de conclusão

A Fase 5 termina quando o ciclo de temporada representável estiver caracterizado, reconciliado com o domínio moderno, coberto por testes determinísticos, persistido com segurança quando necessário e certificado por Android CI sem regressão dos gates da Fase 4R.
