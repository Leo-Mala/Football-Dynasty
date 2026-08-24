# PROCEDURAL FALLBACK BOUNDARY — Fase 6

## Fonte oficial

- corpus: `Brasfoot.apk_Decompiler.com.zip`;
- SHA-256: `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`;
- package legado: `com.brasfoot.v2020`;
- versionCode: `202632`.

## Por que existe este boundary

A manutenção anual mínima de elenco chega a `best.f.e(...)`. Quando nenhum jogador doador seguro é encontrado, o legado não simplesmente ignora a deficiência: ele chama um gerador procedural e depois converte/adiciona o resultado.

O caminho comprovado é:

`best.a0.f()` → `best.c0.n()` → `best.f.e(...)` → ausência de doador → `best.p.d(...)` → `best.t.e(...)`.

Esse fallback é alcançável pelo ciclo anual, mas não pode ser reconstruído de forma segura como um pequeno helper isolado.

## `best.p.d(...)`

A inspeção do Java e principalmente do SMALI mostra um gerador procedural amplo. Ele contém:

- múltiplas instanciações independentes de `java.util.Random`;
- branches condicionais por parâmetros e estado legado;
- tabelas/intervalos usados para construir atributos;
- geração/seleção de identidade textual por rotinas auxiliares;
- chamadas posteriores que continuam transformando o objeto (`D`, `e`, `h`, `g` e relacionadas);
- vários campos cuja semântica nominal ainda precisa ser fechada antes de produzir um jogador moderno equivalente.

Portanto `best.p.d(...)` não é um “default player” nem um registro vazio. Copiar apenas idade/posição/overall ou preencher o restante arbitrariamente quebraria a equivalência e introduziria conteúdo esportivo inventado.

## `best.t.e(...)`

O resultado procedural não é usado diretamente como um `Player` moderno. Ele passa por uma etapa de conversão/materialização em `best.t.e(...)`, que também precisa ser caracterizada junto do gerador para garantir que campos, flags e relações sejam preservados.

## Relação com `T1`

Quando o caminho anual encontra um jogador existente e um destino, a chamada comprovada é:

`best.o.T1(destination, A0(), false, false, false)`.

A Fase 6 já caracteriza esse call shape em `LegacyAnnualPlayerMovementRules` e `ANNUAL_PLAYER_MOVEMENT.md`, mas não o grava parcialmente no Room porque o modelo moderno ainda não representa todos os campos dinâmicos afetados.

O fallback procedural amplia essa necessidade: antes de aplicar de ponta a ponta o ciclo anual, a próxima fase precisa representar tanto a geração correta quanto os efeitos persistentes completos de movimentação.

## Decisão da Fase 6

Classificação:

`PROCEDURAL_GENERATOR_REQUIRES_DEDICATED_RECONSTRUCTION`

Regras:

- não criar jogador procedural com atributos arbitrários;
- não substituir o gerador por dados externos;
- não usar média/default para obter testes verdes;
- não adicionar Room V3 antes de saber quais campos realmente precisam persistir;
- não afirmar paridade funcional do fallback enquanto `best.p.d` + `best.t.e` não estiverem reconstruídos e testados.

## Próxima fase

**FASE 7 — MOVIMENTAÇÃO DE ELENCO E GERAÇÃO PROCEDURAL LEGADA DETERMINÍSTICA**.

Escopo mínimo recomendado:

1. mapear integralmente entradas/saídas de `best.p.d(...)`;
2. catalogar todos os sites de RNG alcançáveis nesse gerador, com bounds, ordem e short-circuit;
3. mapear campos produzidos por `p.D`, `p.e`, `p.h`, `p.g` e helpers necessários;
4. caracterizar `best.t.e(...)` e a transformação final em `best.o`;
5. fechar o estado dinâmico de `best.o` exigido por `T1`;
6. decidir, com evidência, se Room V3 é necessário;
7. se necessário, criar migration não destrutiva + schema + testes;
8. implementar repository transacional para movimentação de membership + efeitos associados;
9. testar determinismo, save/reopen e ausência de jogador inventado;
10. continuar usando exclusivamente o corpus oficial como fonte factual.

A Fase 6 pode encerrar sem fingir que este subsistema já existe: sua responsabilidade é fechar a fronteira determinística e documentar com precisão o que precisa ser reconstruído a seguir.
