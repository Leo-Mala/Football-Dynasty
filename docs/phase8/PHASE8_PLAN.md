# FASE 8 — MOTOR DE PARTIDAS LEGADO DETERMINÍSTICO

## Baseline

- merge certificado da Fase 7: `2cac79d3ae0fa31633e8a1490716efe5b431b301`;
- FINAL_HEAD da Fase 7: `19e18b44d8d7770ed17293babd212fb3c67cd1bc`;
- Phase 7 Validation #286: SUCCESS;
- corpus oficial: `Brasfoot.apk_Decompiler.com.zip`;
- SHA-256: `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`.

## Objetivo

Reconstruir o motor de partidas com paridade estrutural comprovável, RNG explícito e sem atualização ou fabricação de dados esportivos.

O domínio moderno já possui `Match` como modelo e motores de carreira/temporada, mas ainda não possui uma engine moderna de jogo. O corpus oficial localiza a fronteira em `ActivityJogo`, `best.s`, `components.r3`, `best.l`, `best.o` e `best.c0`.

## Evidência inicial

`ActivityJogo.v()` está incompleto no Java, mas existe em SMALI. Ele controla relógio/apresentação e consome eventos; o core de simulação está em `best.s`.

`best.s.R0()` chama `Q0()` para partidas em lote. `Q0()` simula ambos os tempos, inclusive acréscimos, chamando `best.s.k(...)` a cada minuto e `best.s.j(2,0)` na virada.

O primeiro recorte moderno é `LegacyMatchMinuteRules`, que reproduz somente a árvore direta de RNG e bounds de `best.s.k(...)`, mantendo nomes neutros para eventos ainda não totalmente caracterizados.

## Métodos Java incompletos prioritários

Devem ser recuperados por SMALI antes de portar sua lógica: `best.s.n0`, `p`, `P0`, `Q0`, `j`, `r` e `r0`.

## Ordem de trabalho

1. separar clock/UI de simulação;
2. catalogar RNG direto e transitivo de `best.s`;
3. caracterizar seleção `S/T/U/V/W` e shuffles;
4. recuperar métodos truncados via Java↔SMALI;
5. modelar eventos `best.l` sem semântica inventada;
6. reconstruir placar e demais efeitos comprovados;
7. mapear efeitos pós-jogo em competição, clube e jogador;
8. integrar ao calendário/carreira somente após paridade do engine puro;
9. alterar Room apenas se estado persistente novo for comprovadamente necessário.

## Regras permanentes

- usar `RandomSource` no domínio moderno; RNG cru permanece proibido;
- preservar ordem, bounds e short-circuit dos draws;
- não afirmar equivalência da seed implícita do APK;
- nenhum jogador, clube, rating, atributo, elenco ou competição factual pode ser alterado por fonte externa;
- `fallbackToDestructiveMigration` permanece proibido;
- Codex indisponível: revisão por inspeção independente, Java↔SMALI, testes e GitHub Actions.

## Exit gate

A fase só encerra com caminhos automático/acompanhado mapeados, RNG alcançável controlado, métodos truncados necessários recuperados, eventos/placar e efeitos pós-jogo comprovados, testes e build Android verdes no head exato, PR mergeável e sem finding material pendente.
