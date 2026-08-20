# GFXCORE_MAP — Fase 1

`GfxCore.java` tem aproximadamente **22.713 linhas**, **942 métodos detectáveis** e **2.637 campos/referências detectáveis** no Java decompilado. É um god object que mistura regras, UI, calendários e utilitários. A decomposição abaixo é conceitual; nenhum código foi movido nesta fase.

## Grupos identificados

### Partidas / simulação
Métodos e famílias observadas incluem `autoSubs`, `energyMatchTick`, `momentumTick`, `simGoals`, `shotPhrase`, `trySub`, `varCheck`, `showVarOverlay`, `stance*`, `addNarr`, `addShots`, `preMatchUI`, `isUserMatch`, `hasMatch*`, `firstLegMatch`, `returnLegMatch`, `aggLine`, além de rotinas de pênaltis, cartões e lesões.

### Competições / tabelas
Famílias observadas: `ucl*`, `sul*`, `acesso*`, `add*Qualifiers`, `apply*WorldCup`, `copa*`, `liga*`, `recopa*`, `supercopa*`, `SuperMundial`, `finish*Cup`, `reorderCrossGroup`, `awardSuperMundial`, `isChampionsMatch`, `isEuropaMatch`, `isConferenceMatch`, `isLibertadoresMatch`, `isSulaMatch`.

### Calendário / temporada
Famílias: `day*`, `schedule*`, `season*`, `ensure*Days`, `maybeSchedule*`, `runSimSeason`, `startSimSeason`, `recopaEarlyDays`, `smMidYearDays`, `userPlaysDay`, `updateClubRecords`.

### Jogadores / evolução
Famílias: `ageDecline`, `drainByAge`, `retireDecision`, `retireReplace`, `purgeAposentados`, `scout*`, `award*`, `bola*`, `mvp*`, `giftJunior`, `showSeasonAwards`, `updateIdols`.

### Clubes / elenco
Famílias: `squad*`, `minSquad*`, `club*`, `team*`, `chem*`, `setCaptain`, `energyClub`, `goalsForClub`, `assistsForClub`, `gamesForClub`, `capture*Seeds`, `loadTeamsCache`, `saveTeamsCache`.

### Transferências / contratos
Famílias: `transfer*`, `troca*`, `loan*`, `schedulePendingSale`, `schedulePendingLoan`, `processPendingSales`, `completePendingSale`, `rcRenewBlock`, `recordTransfer`, `showBuyLoan`, `showEmprestados`.

### Finanças
Famílias: `fin*`, `appMoney`, `bgMoney`, `checkSponsor`, `nrSponsor`, `ticketAdjust`, `rcMoney`, `trocaMoney`, `showBoardInFinancas`, `finEndOfYearNet`.

### Save / load / dados
Métodos observados incluem `loadTeamsCache`, `saveTeamsCache`, `loadEscudo`, `rankData`, `recWmSave` e helpers de leitura/escrita. A persistência principal também está espalhada por `ActivitySave`, `ActivityLoad` e `c.a`.

### UI / preferências / avisos
Famílias: `announce*`, `show*`, `add*Pref`, `add*Button`, `cardDialog`, `styledDialog`, `jornal*`, `newsTransfer`, `dominantColor`, `addCompLogo`, `removeCompLogo`.

## Destino arquitetural futuro (não implementado)

| Grupo legado | Futuro módulo sugerido | Regra de migração |
|---|---|---|
| Partidas/simulação | `MatchEngine` | portar comportamento com testes de paridade antes de limpar fórmulas |
| Competições | `CompetitionEngine` + regras por competição | preservar formatos legados exatamente |
| Calendário/temporada | `SeasonCalendar` | preservar ordem/efeitos de avanço de data |
| Jogadores/evolução | `PlayerLifecycleEngine` | preservar idade/evolução/aposentadoria |
| Mercado/contratos | `TransferEngine` / `ContractEngine` | preservar side effects financeiros e de elenco |
| Finanças | `FinanceEngine` | separar cálculo de UI sem alterar valores |
| Save/load | `LegacyPersistenceAdapter` | primeiro ler/gravar formato legado; Room só em fase posterior |
| UI/helpers | camada Compose | remover acoplamento somente após extração de regras |

## Risco central

`GfxCore` chama diretamente entidades, Activities e componentes; entidades/componentes também chamam `GfxCore`. Isso forma dependências circulares. A extração deve ser incremental e protegida por testes de caracterização, não por reescrita total de uma vez.