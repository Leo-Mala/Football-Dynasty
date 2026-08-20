# GAME_SYSTEMS — Fase 1

Sistemas identificados por classes, métodos e estruturas persistidas:

| Sistema | Evidência principal | Classes centrais |
|---|---|---|
| Inicialização/instalação | launcher, cópia/instalação de dados | `MainActivity`, `ActivityInstall`, `c.a` |
| Nova carreira/seleção | opções e escolha de times | `StartOptions`, `ActivityEscolhaTimes`, `a.b` |
| Hub do clube | tela principal da carreira | `ActivityMainTeam`, `GfxCore` |
| Elenco/escalação/tática | telas e modelos de jogador/time | `ActivityEscala`, `DialogTatics`, `ActivitySavedTatics`, `a.p`, `a.ac` |
| Partida | estado `a.t`, motor `components.cn`, tela `ActivityJogo` | `a.t`, `components.cn`, `ActivityJogo`, `ActivityPenalty` |
| Campeonato/classificação | liga `d.q`, telas de tabela/resultados | `d.q`, `ActivityClass`, `ActivityResults` |
| Calendário/temporada | agenda, calendário, fim de ano | `ActivityAgenda`, `ActivityCalendario`, `ActivityFimAno`, `GfxCore` |
| Transferências/mercado | busca, info jogador, offers/loan helpers | `ActivityProcura`, `DialogIgrokInfo`, `Rc*`, `GfxCore`, `a.p` |
| Finanças/estádio | telas específicas + campos do clube | `ActivityFinancas`, `ActivityEstadio`, `a.ac`, `GfxCore` |
| Juniores/evolução | tela juniores + campos do jogador + métodos GfxCore | `ActivityJuniores`, `a.p`, `GfxCore` |
| Convocação/seleções | convocação e amistosos de seleção | `ActivityConvoca`, `ActivityConviteSelecao`, `ActivityAmistososSelecao` |
| Rankings/recordes/histórico | telas de ranking, campeões, hall | `ActivityRankings`, `ActivityRecordes`, `ActivityCampeoes`, `ActivityHallTecnicos` |
| Save/Load | Java serialization + Kryo + backup | `ActivitySave`, `ActivityLoad`, `c.a`, `c.b` |
| Preferências | objeto `est.Options` | `ActivityPref`, `est.Options`, `c.a` |
| Editor | edição de times/jogadores | `ActivityEditor`, `ActivityEditorTeam`, `ActivityEditJog` |
| Billing/subscrição | Google Billing | `ActivitySub`, `RcShop`, billing client |

## Aleatoriedade

Uso de `java.util.Random` é amplo em `a.p`, `a.ac`, `a.t`, `a.b`, `d.q`, `components.cn` e `GfxCore`. A aleatoriedade afeta simulação de partidas, geração/evolução, scouting, sorteios e decisões de IA. Não foi encontrada uma política global única de seed; há instâncias estáticas e instâncias criadas localmente. Isso torna testes de paridade determinística uma prioridade da Fase 2.

## Regras de temporada e competições

Métodos nomeados no `GfxCore` mostram rotinas específicas para UCL, Sul-Americana, acessos, supercopa/supermundial e qualificadores continentais. `d.q` guarda critérios de turnos, grupos, mata-mata e rebaixamento. Esses formatos devem ser migrados como regras configuradas/comportamentais, não “atualizados” para regras modernas do futebol real.