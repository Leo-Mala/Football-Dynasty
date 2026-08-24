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

Nenhum símbolo obfuscado recebe significado esportivo por inferência. Java decompilado e SMALI são confrontados antes de uma regra ser considerada caracterizada.

## Escopo implementado

A Fase 6 fecha, por regras Kotlin puras e testes de caracterização:

1. os 10 sites diretos de RNG de `best.a0` e os sites anuais transitivos de `best.f`;
2. short-circuit e consumo correto de RNG nos branches recuperados;
3. substituição de `Collections.shuffle` por shuffle determinístico com `RandomSource`;
4. predicados `best.c0.M1/a1/Z0` e tabelas de thresholds comprovadas;
5. seleção de posição/jogador e roteamento de `best.a0.j(...)`;
6. caminhos e filtros `best.f` necessários ao ciclo anual;
7. manutenção mínima de elenco `best.a0.f() -> c0.n() -> f.e()/h()`;
8. rotina SMALI-only `best.a0.i()`;
9. decisões determinísticas de orquestração em `best.a0.a/b/c`;
10. plano estrutural da chamada anual `best.o.T1(destination, A0(), false, false, false)`.

## Política moderna de RNG

- é proibido copiar `new Random()` para o domínio de temporada moderno;
- sorteios reconstruídos recebem `RandomSource` por dependência;
- a sequência moderna pode sobreviver a save/reopen por `StatefulJavaRandomSource`/`CareerRandomState`;
- bounds, thresholds, ordem e short-circuit só são reproduzidos quando comprovados;
- chamadas independentes a `new Random()` no legado não são falsamente tratadas como equivalência bit-a-bit de seed.

## Persistência

Room permanece V2.

As regras da fase são deriváveis e o estado RNG necessário já existe em `career_core_state`. Não foi demonstrada necessidade legítima de Room V3.

A aplicação persistida completa de `T1` fica para a próxima fase porque o modelo moderno ainda não representa, com equivalência comprovada, todos os seus campos dinâmicos e efeitos associados.

## Boundary explícito

O fallback anual `best.p.d(...) -> best.t.e(...)` é um gerador procedural complexo com múltiplas fontes de RNG e construção de atributos. Ele não é implementado parcialmente nesta fase, pois isso inventaria jogadores/atributos.

A fronteira está documentada em `PROCEDURAL_FALLBACK_BOUNDARY.md` e define o próximo subsistema de reconstrução.

## Gates de encerramento

Antes do merge do PR #5, o mesmo head deve passar:

- guard de RNG cru;
- Gradle help;
- KSP + Room V2;
- suíte unitária integral;
- caracterização de calendário e lifecycle;
- RNG anual;
- seleção anual;
- squad floor;
- `best.a0.i`;
- orquestração `best.a0`;
- seleção profunda `best.a0.j/best.f`;
- plano anual `T1`;
- benchmark existente;
- fixture Brasfoot 2026 por SHA-256;
- `assembleDebug`;
- Room V1/V2 e ausência de destructive migration.

## Revisão

Codex está indisponível por limite mensal do proprietário e não será falsamente marcado como aprovado. A revisão de fechamento usa diff independente, Java↔SMALI, testes e GitHub Actions no head exato.

## Próxima fase após merge

**FASE 7 — MOVIMENTAÇÃO DE ELENCO E GERAÇÃO PROCEDURAL LEGADA DETERMINÍSTICA**.

Ela só deve começar depois do merge certificado da Fase 6.
