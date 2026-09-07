# Fase 15 — auditoria de persistência de `best.o.M`

## Escopo

Este documento fecha somente a identidade funcional e o gap de persistência moderno do Boolean legado `best.o.M`. Ele **não** declara `best.o.s()`/`best.o.e()` integralmente implementados e **não** autoriza, isoladamente, uma migration Room V15.

Fonte factual única: corpus oficial `Brasfoot.apk_Decompiler.com.zip`, SHA-256 `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`, package `com.brasfoot.v2020`, versionCode `202632`. Quando Java decompilado e SMALI divergem, o SMALI prevalece.

## Identidade legado já congelada

A auditoria corpus-wide da Fase 15 já separou dois Booleans ofuscados que não podem ser confundidos:

- `best.o.M` é lido por `S()` e escrito por `s1(Boolean)`;
- o Boolean escrito por `M1(Boolean)` é outro campo (`d`), lido por `W0()` e usado no lifecycle de `D0()`.

`best.o.M` participa da progressão anual senior e é limpo por `best.o.e()` depois do processamento anual. Portanto não é um flag de apresentação.

## Escrita na escalação

A caracterização já existente de `ActivityEscalacao.y()/B()` está materializada em `LegacyLineupCommitRule`.

Para cada titular aceito (`slotCode > 0 && slotCode < 26`) a regra produz:

- o código de escalação do slot; e
- `starterFlagWrite = true`, documentado explicitamente como a chamada legado `s1(TRUE)`.

Jogadores de banco não recebem escrita `s1` nesse commit (`starterFlagWrite = null`).

Logo, neste fluxo alcançável:

`titular confirmado -> best.o.s1(TRUE) -> best.o.M = TRUE`

Isso prova que `M` é um latch ligado a participação/uso do jogador, e não um atributo esportivo estático.

## Escrita durante substituição

O runtime de partida moderno caracterizado mantém `LegacyMatchTransientRuntime.Player.selectedOrUsed`.

No caminho de substituição aplicado por `LegacyMatchTransientRuntime.applySubstitution(...)`, o jogador que entra é movido do banco para os ativos/usados e recebe:

`incoming.selectedOrUsed = true`

Esse estado corresponde ao mesmo boundary de uso alcançável que a auditoria legado associou aos writers de `M` em escalação/substituição. A implementação atual, porém, o conserva apenas no wrapper transitório da partida.

## Auditoria da persistência moderna

### `CareerPlayerRuntimeEntity`

A entidade V14 persiste, entre outros estados, idade, overall, market value, contrato, flags já mapeados, energia e lesão. Ela não possui coluna equivalente explícita para o latch anual `best.o.M`.

### Runtime de partida

`LegacyMatchTransientRuntime.Player.selectedOrUsed` é memória transitória da simulação. Não faz parte de `CareerPlayerRuntimeEntity`.

### Boundary moderno de resultado

`LegacyMatchModernResultMapper` projeta o runtime transitório para o modelo moderno `Match` apenas com identidade dos clubes e placar resolvido. O latch `selectedOrUsed` não atravessa esse boundary.

`CareerMatchRuntimeBridge` persiste o estado de calendário/RNG e o resultado da partida, mas não materializa um latch por jogador equivalente a `best.o.M`.

## Conclusão de paridade

A equivalência `M == algo já persistido e derivável no moderno` **não é comprovada**.

Ao contrário, há agora um gap objetivo:

1. o legado escreve `M=TRUE` em fluxo alcançável de escalação/uso;
2. a progressão anual lê esse Boolean;
3. `best.o.e()` o limpa após consumi-lo;
4. a implementação moderna possui apenas `selectedOrUsed` transitório durante a partida;
5. o boundary moderno de resultado não persiste esse latch por jogador;
6. um save/reopen entre o uso do atleta e o processamento anual perderia essa informação se dependesse somente do estado atual.

Classificação:

`best.o.M / S() / s1(Boolean) -> PERSISTENT_RUNTIME_GAP_PROVEN`

Esse gap é separado do acumulador `best.o.N`, cujo requisito de durabilidade já estava comprovado pela auditoria de progressão senior.

## Decisão de schema

**Não criar V15 somente por este documento.**

Antes de congelar a próxima migration devem ser fechados, no mesmo mapa agregado:

- controle completo de `best.o.s()` e `best.o.e()`;
- identidade final dos demais estados persistentes necessários ao lifecycle anual (`N` e os estados ainda abertos de `F()/D0()`);
- atomicidade carreira + jogador + RNG no boundary anual;
- defaults de migration baseados somente em semântica comprovada, sem backfill esportivo inventado.

Até lá Room permanece em V14 e a matriz da Fase 15 deve tratar `M` como gap persistente comprovado, não como comportamento implementado.
