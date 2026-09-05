# Fase 15 — alcance do reset financeiro periódico

Status: **IMPLEMENTED / AWAITING PHASE 15 CERTIFICATION**

Corpus factual oficial: `Brasfoot.apk_Decompiler.com.zip`, SHA-256 `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`, package `com.brasfoot.v2020`, versionCode `202632`.

## Conclusão superada da Fase 13

A documentação histórica da Fase 13 permanece preservada. A Fase 15, porém, encontrou evidência corpus-wide que supera a classificação anterior de `best.m.z()` como método sem caller alcançável.

A cadeia comprovada é:

`best.n.n()` → condição `E1()` → `best.b.d()` (`NovoAno`) → iteração sobre todos os clubes → `best.c0.l1()` → `best.m.z()`.

Callers já comprovados de `best.n.n()` incluem `ActivityEscolhaTimes`, `ActivityTeste`, `ActivityFimAno` e `best.b.d()`. Portanto o reset é alcançável por fluxo real de virada de ano/temporada.

## Efeito exato de `best.m.z()`

Java e SMALI concordam que `z()` zera exatamente os dez acumuladores periódicos do ledger:

- receita de bilheteria;
- receita de venda de jogador;
- premiação;
- patrocínio;
- compra de jogador;
- estádio;
- salários;
- encargo de empréstimo financeiro;
- multa;
- despesa diversa.

Os campos de principal emprestado (`f4372m`) e encargo mensal pré-calculado (`f4373n`) não são zerados. O equivalente puro moderno já existia em `LegacyFinanceLedgerRule.resetPeriod()` e preserva exatamente esses dois campos.

## Ordem e escopo

`best.b.d()` começa o fluxo substantivo anual percorrendo `g1()` e chamando `l1()` em cada clube. `LegacySeasonLifecycleOrder` já caracteriza esse primeiro estágio como `G1_L1_MAINTENANCE`, antes de `CALENDAR_REBUILD_L` e das operações anuais seguintes.

`best.c0.l1()` só chama `m.z()` quando o objeto financeiro existe. O mapeamento moderno respeita esse detalhe: o reset percorre somente as linhas de `career_club_manager_runtime` já materializadas para a carreira. Nenhuma linha ausente é criada e nenhum valor é inventado.

## Lacuna moderna encontrada

Antes desta correção:

- `LegacyFinanceLedgerRule.resetPeriod()` existia e tinha teste unitário;
- `CareerCommand.TransitionSeason` era o gatilho público de transição;
- `CareerSimulationEngine` executava a transição de calendário;
- nenhum caller de lifecycle aplicava `resetPeriod()` aos runtimes persistidos dos clubes.

Classificação pré-correção: `REACHABLE_NOT_IMPLEMENTED`.

## Implementação da Fase 15

A persistência do resultado de um comando agora passa por `CareerStateRepository.saveTransition(...)`. O default continua equivalente a `save(...)`, preservando repositórios que não possuem efeitos persistidos adicionais.

No Room, somente `CareerCommand.TransitionSeason` abre o boundary adicional comprovado:

1. inicia uma única `database.withTransaction`;
2. lê `clubRuntimeForCareer(careerId)`, já ordenado deterministicamente por `clubId`;
3. para cada runtime materializado, aplica diretamente `LegacyFinanceLedgerRule.resetPeriod()`;
4. grava o runtime preservando `cash`, flags, slots, principal emprestado, encargo mensal e todos os demais campos não periódicos;
5. persiste o `CareerState` já calculado pelo engine puro.

O engine calcula o próximo calendário sem efeitos colaterais antes do boundary Room. A ordem observável de mutações persistidas continua a ordem legada comprovada: reset financeiro primeiro, novo estado de calendário depois. Se qualquer gravação falhar, a transação Room reverte o conjunto.

Esta implementação deliberadamente **não** reconstrói os demais estágios de `best.b.d()` ainda não promovidos pela auditoria.

## RNG

O reset financeiro não recebe `RandomSource`, não chama RNG e não altera `CareerState.random`. A regressão compara o resultado do caminho público persistido com a transição pura do `CareerSimulationEngine` e exige igualdade do estado RNG (`initialSeed`, `internalState`, `draws`). Portanto o novo estágio não introduz draw adicional.

## Room / migration

Nenhum campo novo é necessário. O estado já existe no schema V13 em `career_club_manager_runtime`. Consequentemente:

- nenhuma migration nova;
- nenhum backfill;
- nenhum default inventado;
- nenhum `fallbackToDestructiveMigration`.

## Regressões

Cobertura adicionada ao caminho público real `CareerSimulationCoordinator.apply(..., CareerCommand.TransitionSeason)`:

- dois clubes materializados com todos os dez acumuladores não-zero são zerados;
- todos os campos não periódicos permanecem byte-for-byte equivalentes no modelo Room, incluindo dívida/encargo mensal;
- um clube existente sem runtime financeiro continua sem runtime;
- o `CareerState` resultante é exatamente o produzido pelo engine puro;
- RNG permanece inalterado;
- estado financeiro e estado de temporada sobrevivem ao fechamento e reabertura do banco.

Até o gate da Fase 15 rodar no HEAD exato, a matriz usa `IMPLEMENTED_NEEDS_REVALIDATION`, não `IMPLEMENTED_AND_CERTIFIED`.
