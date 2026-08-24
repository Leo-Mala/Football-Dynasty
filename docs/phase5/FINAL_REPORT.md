# FASE 5 — RELATÓRIO FINAL

## Baseline

- base certificada: `phase4/core-game-domain@aad0327c4be35fbc906ee30d93ed89ce8c1b8765`;
- corpus oficial: `Brasfoot.apk_Decompiler.com.zip`;
- SHA-256: `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`;
- package legado: `com.brasfoot.v2020`;
- versionCode: `202632`;
- versionName: `Brasfoot.202632`.

## Escopo concluído

A Fase 5 caracteriza e reconcilia a paridade estrutural do ciclo de fim/início de temporada do Brasfoot 2026/27 sem copiar a arquitetura legado para o aplicativo moderno.

Foram confirmados por Java + SMALI:

- `ActivityFimAno.e()` e o roteamento por `best.n.n()`;
- ordem e branches de `best.b.d()`;
- reconstrução anual por `best.b.l()` com base padrão 2026;
- calendário de 365/366 dias e primeiro domingo de janeiro;
- `best.b.s()`, `D()`, `r()`, `o()`, `q()`, `w2()`, `n()`, `A1()`, `y1()`, `Y3()`, `P0()`;
- `best.a.m()`;
- `components.y3.b()`;
- `best.c0.l1()` e efeito transitivo `best.m.z()`;
- existência e posição de aleatoriedade direta em `best.a0`.

## Domínio moderno

- `LegacyCalendarRules` permanece a implementação moderna dos invariantes de calendário comprovados;
- `LegacySeasonLifecycleOrder` congela a ordem/branches do orquestrador sem inventar semântica para efeitos obfuscados;
- o domínio moderno continua usando RNG persistível/determinístico;
- não foi introduzido `java.util.Random()` irreproduzível na implementação moderna.

## Persistência

Room permanece na versão V2. Nenhum novo estado persistente foi demonstrado como obrigatório para fechar a paridade estrutural desta fase, portanto não foi criada uma migração V3 artificial.

A política continua proibindo `fallbackToDestructiveMigration`.

## Testes e Android gate

Workflow `Phase 5 Validation`, run certificado para o head `840e7dec2f0105606eee95f2e6af1908f5d877a4` antes do fechamento documental:

- Gradle help — PASS;
- KSP + Room schema V2 — PASS;
- `testDebugUnitTest` — PASS;
- 50 testes, 0 failures, 0 errors, 0 skipped;
- `LegacyCareerCalendarCharacterizationTest` — 5/5 PASS;
- `LegacySeasonLifecycleOrderTest` — 4/4 PASS;
- fixture Brasfoot 2026 SHA-256 — PASS;
- core benchmark — PASS;
- `assembleDebug` — PASS;
- Room V1->V2 / schemas / ausência de fallback destrutivo — PASS.

Qualquer commit documental posterior deve ser certificado novamente no head exato antes do merge.

## Integridade esportiva

Nenhum jogador, clube, rating, atributo, elenco ou competição foi alterado usando fonte externa. O único corpus factual usado foi o baseline Brasfoot 2026/27 fixado na Fase 4R.

## Limitações explícitas

A Fase 5 não declara equivalência funcional profunda de todos os efeitos anuais. Parte dos subsistemas continua obfuscada semanticamente e `best.a0` contém múltiplas chamadas diretas a `java.util.Random()` sem estado persistível compatível com o core moderno.

Essas limitações não invalidam a paridade estrutural do ciclo; elas definem o escopo seguro da etapa seguinte.

## Próxima fase recomendada

**FASE 6 — EFEITOS ANUAIS DETERMINÍSTICOS E SUBSISTEMAS DE TEMPORADA**.

Objetivo: reconstruir, por subsistema e com testes de caracterização, os efeitos anuais ainda obfuscados que realmente precisem existir no domínio moderno, roteando toda aleatoriedade pela abstração `RandomSource`, sem alterar dados esportivos do corpus e sem assumir equivalência onde a evidência Java/SMALI ainda for insuficiente.
