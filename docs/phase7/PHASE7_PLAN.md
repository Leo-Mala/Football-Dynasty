# FASE 7 — MOVIMENTAÇÃO DE ELENCO E GERAÇÃO PROCEDURAL LEGADA DETERMINÍSTICA

## Baseline certificado

- merge da Fase 6: `565eadd1eee0f09faee26cf0632c60a7eec2aa5f`;
- corpus oficial: `Brasfoot.apk_Decompiler.com.zip`;
- SHA-256: `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`;
- package legado: `com.brasfoot.v2020`;
- versionCode: `202632`.

## Objetivo

Reconstruir o fallback procedural e a movimentação persistente alcançados pelo ciclo anual sem fabricar jogadores, atributos ou seeds que não existam no corpus oficial.

A Fase 6 encerrou com o boundary comprovado:

`best.a0.f()` → `best.c0.n()` → `best.f.e(...)` → ausência de doador → `best.p.d(...)` → `best.t.e(...)`.

O call shape anual recuperado em SMALI é exatamente:

- `best.p.d(target, requestedPosition, null, 0, null, Boolean.FALSE)`;
- se houver resultado: `best.t.e(false, generated, target)`.

## Primeira frente — gerador `best.p.d(...)`

O Java decompilado está truncado. O SMALI oficial é obrigatório.

A auditoria inicial confirma **7 sites diretos de `java.util.Random`** dentro de `p.d(...)`. O método também chama `p.D(target)`, `p.h()` e `p.g()`, que possuem RNG próprio, portanto o efeito procedural não pode ser reproduzido corretamente apenas pelos sete draws locais.

A Fase 7 deve preservar:

- bounds;
- ordem dos draws;
- short-circuit/branches que evitam draws;
- overrides de parâmetros;
- campos gerados;
- chamadas transitivas;
- conversão final em `best.o`.

## Segunda frente — `best.t.e(...)`

O método Java também está truncado; o SMALI confirma a construção de `best.o` a partir de getters de `best.p`, além de **5 sites de RNG no bytecode**, alguns mutuamente exclusivos/condicionais.

Antes de persistir um jogador procedural moderno, é obrigatório caracterizar quais campos de `best.p` alimentam:

- identidade/nome;
- posição;
- atributos numéricos;
- flags;
- estado temporal;
- clube/membership;
- índices/listas globais.

## Movimentação `T1`

A Fase 6 já caracterizou o call shape anual `T1(destination, A0(), false, false, false)`, mas não executou uma gravação parcial porque o modelo moderno ainda não representa todos os efeitos dinâmicos.

Na Fase 7:

1. mapear os campos dinâmicos de `best.o` realmente necessários;
2. separar estado persistente de estado derivável;
3. decidir Room V3 somente depois desse mapa;
4. se V3 for necessária, criar migration não destrutiva + schema + teste;
5. aplicar membership + efeitos associados numa transação única;
6. validar rollback, save/reopen e determinismo.

## Política de RNG

- nenhuma chamada direta a `Random()` no domínio moderno;
- todo draw reconstruído recebe `RandomSource`;
- não afirmar equivalência bit-a-bit com seeds padrão do APK;
- preservar a sequência moderna após snapshot/restore;
- registrar explicitamente sites que são mutuamente exclusivos.

## Dados esportivos

- nenhuma fonte externa;
- nenhum nome/atributo de jogador inventado;
- nenhum default arbitrário para completar campos desconhecidos;
- todos os valores reconstruídos precisam vir de regra comprovada no corpus.

## Room

Começa em **V2**. V3 não é objetivo por si só e só será criada se a reconstrução provar estado novo persistente indispensável.

`fallbackToDestructiveMigration` permanece proibido.

## Revisão

Codex está indisponível pelo limite mensal do proprietário. O fechamento usa auditoria independente de diff, Java↔SMALI, testes e GitHub Actions no head exato, sem declaração falsa de `Codex approved`.

## Exit gate

A fase só pode encerrar quando:

- `p.d` estiver caracterizado no caminho anual;
- RNG direto e transitivo estiver explicitamente controlado;
- `t.e` estiver caracterizado;
- nenhum atributo procedural depender de suposição;
- movimentação persistente estiver completa ou uma limitação comprovada permanecer isolada sem falsa paridade;
- Room/migrations estiverem coerentes;
- testes e build Android estiverem verdes no head exato;
- PR estiver mergeable e sem finding material pendente.
