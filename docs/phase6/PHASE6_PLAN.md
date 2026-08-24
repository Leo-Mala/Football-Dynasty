# FASE 6 — EFEITOS ANUAIS DETERMINÍSTICOS E SUBSISTEMAS DE TEMPORADA

## Baseline

- base certificada: `phase4/core-game-domain@343b115219824255d46ed538ef9519c3c92b33e4`;
- esse commit é o merge concluído da Fase 5;
- corpus legado oficial: `Brasfoot.apk_Decompiler.com.zip`;
- SHA-256: `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`;
- package legado: `com.brasfoot.v2020`;
- versionCode: `202632`.

## Objetivo

Reconstruir por subsistema os efeitos anuais profundos que a Fase 5 catalogou estruturalmente, preservando determinismo, estado de RNG persistível, Room e integridade esportiva.

Nenhum símbolo obfuscado recebe significado esportivo por inferência. Java decompilado e SMALI continuam sendo confrontados antes de uma regra ser considerada caracterizada.

## Primeira frente — aleatoriedade de `best.a0`

A auditoria inicial do SMALI oficial encontrou 10 instanciações diretas de `java.util.Random` em `best.a0`:

- 1 em `public static a()`;
- 1 em `public static i()`;
- 8 em `private static j(best.c0, boolean, boolean)`.

O método `a()` contém o gate comprovado `new Random().nextInt(100) > 30` antes de um efeito legado. A existência e posição do sorteio são confirmadas por Java + SMALI; o significado esportivo do efeito não será inventado.

## Política moderna de RNG

- é proibido copiar `new Random()` para o domínio de temporada moderno;
- sorteios reconstruídos devem receber `RandomSource` por dependência;
- quando a sequência precise sobreviver a save/reopen, usar infraestrutura restaurável compatível com `StatefulJavaRandomSource`/`CareerRandomState`;
- uma distribuição/limiar somente será reproduzida quando estiver comprovada;
- chamadas independentes a `new Random()` no legado não serão falsamente tratadas como um único RNG global equivalente.

## Gates

1. catalogar todos os sites de RNG anuais relevantes por método, bound, condição e efeito observável;
2. confirmar cada site crítico em SMALI;
3. criar regras modernas pequenas e injetáveis somente para sorteios comprovados;
4. testar repetibilidade e restauração do RNG;
5. impedir introdução de RNG irreproduzível no domínio anual moderno;
6. implementar efeitos anuais somente quando entradas, mutações e invariantes estiverem caracterizadas;
7. preservar Room V2 salvo necessidade real de novo estado persistente;
8. manter integralmente os gates Android e Room herdados;
9. certificar o head exato antes de merge.

## Não fazer

- não inventar nomes/semântica para `best.a0` ou estruturas `konrent.*`;
- não alterar dados esportivos;
- não importar dados externos;
- não usar `Math.random`, `ThreadLocalRandom` ou `Random()` solto no domínio reconstruído;
- não criar Room V3 apenas para representar documentação ou estado derivável;
- não enfraquecer testes ou integridade para obter CI verde.
