# ACTIVITY_MAP — Fase 1

O manifest declara **54 Activities**: **53** pertencentes ao pacote do jogo e **1** Activity da biblioteca de Billing (`ProxyBillingActivity`). O fluxo abaixo usa somente evidência estática de layout, classe e acesso ao estado global; ausência de seta não significa ausência de navegação dinâmica.

## Fluxo de entrada confirmado

`MainActivity` (LAUNCHER) → inicialização/cópia de dados → `StartOptions` / carregamento ou criação de carreira → telas de gestão ligadas a `ActivityMainTeam`.

## Tabela de telas

| Activity | Papel principal | Layout(s) observado(s) |
|---|---|---|
| `MainActivity` | bootstrap/inicialização e entrada do app | `activity_main`, `dialog_confirm` |
| `StartOptions` | opções de início/criação de carreira | `activity_start_options` |
| `ActivityEscolhaTimes` | escolha de clube/time | `activity_escolha_times` |
| `ActivityMainTeam` | hub principal da carreira/clube | `activity_main_team`, `dialog_confirm`, `dialog_oferta` |
| `ActivitySave` | salvar carreira | `activity_save`, `dialog_confirm` |
| `ActivityLoad` | carregar carreira | `activity_load`, `dialog_confirm` |
| `ActivityEscala` | escalação | `activity_escala` |
| `ActivityDialogRate` | avaliação/rate | `activity_dialograte` |
| `ActivityEditor` | editor | `activity_editor`, `dialog_confirm` |
| `ActivityEditorTeam` | edição de time | `dialog_edit_team` |
| `ActivityJogo` | partida | construção fortemente dinâmica |
| `ActivityPenalty` | disputa de pênaltis | `activity_penal` |
| `ActivityConvite` | convite de clube | `activity_convite` |
| `ActivityConviteSelecao` | convite de seleção | `activity_conviteselecao` |
| `ActivityEditJog` | edição de jogador | `activity_edit_jog` |
| `ActivityClass` | classificação/tabela | `activity_class` |
| `ActivityResults` | resultados | `activity_results`, `dialog_confirm` |
| `ActivityTecnico` | treinador/técnico | `activity_tecnico` |
| `ActivityTeamHistory` | histórico do clube | `activity_team_history` |
| `ActivityRecordes` | recordes | construção dinâmica |
| `ActivityRankings` | rankings | `activity_rankings` |
| `ActivityCampeoes` | campeões | `activity_campeoes` |
| `ActivityArt` | artilharia/estatísticas | `activity_art` |
| `ActivitySub` | assinatura/billing | `activity_sub` |
| `ActivtyRankingTecnicos` | ranking de técnicos | `activity_ranking_tec` |
| `ActivityPref` | preferências | `activity_pref` |
| `ActivityTimes` | listagem/gestão de times | `activity_times`, `dialog_confirm`, `dialog_proposta` |
| `ActivityEstadio` | estádio | construção dinâmica |
| `ActivityAgenda` | agenda | `activity_agenda` |
| `ActivityHist` | histórico | `activity_hist` |
| `ActivityJornal` | jornal/notícias | `activity_jornal` |
| `ActivityJuniores` | juniores | `activity_juniores`, `dialog_confirm` |
| `ActivityAmistosos2024` | amistosos de clubes | `activity_amistosos`, `dialog_confirm` |
| `ActivityAmistososSelecao` | amistosos de seleção | `activity_amistosos_selecao` |
| `ActivityCalendario` | calendário | `activity_calendario` |
| `ActivityFinancas` | finanças | `activity_financas` |
| `ActivityProcura` | procura/mercado de jogadores | `activity_procura`, `dialog_confirm`, `dialog_proposta` |
| `DialogIgrokInfo` | informações/ações do jogador | `dialog_aposentar`, `dialog_confirm`, `dialog_contrato`, `dialog_venda` |
| `DialogTatics` | táticas | construção dinâmica |
| `DialogA` | dialog/tela auxiliar | `activity_d` |
| `DialogAbout` | sobre | `dialog_about` |
| `ActivityFileManager` | arquivos | `activity_file` |
| `ActivityInstall` | instalação/importação de dados | `activity_install` |
| `DialogDemissoes` | demissões | `dialog_demissoes` |
| `ActivityTeste` | tela interna de teste | `activity_teste` |
| `ActivityFimAno` | fim de temporada/ano | `activity_fim_ano` |
| `ActivityBolaDeOuro` | Bola de Ouro/prêmios | `activity_bola_ouro` |
| `ActivityHallTecnicos` | hall/ranking de técnicos | `activity_bola_ouro` |
| `DialogFieldInfo` | informações do campo/jogador | `dialog_field_info` |
| `DialogTimeRodada` | time da rodada | `dialog_time_rodada` |
| `ActivitySavedTatics` | táticas salvas | `activity_savedtatics` |
| `ActivityConvoca` | convocação | `activity_convoca`, `dialog_confirm` |
| `ActivityField` | campo/tática visual | `activity_field` |
| `ProxyBillingActivity` | infraestrutura Google Play Billing | externo |

## Observações de navegação

- Muitas telas dependem de `c.a.TF`, o estado global da carreira; por isso o fluxo real não é transportado apenas por `Intent`/extras.
- Diversas transições são disparadas por helpers/listeners `Rc*` e por métodos estáticos de `GfxCore`.
- `Dialog*` são implementados como `Activity`, não como `DialogFragment`.
- `ProxyBillingActivity` é infraestrutura externa e não faz parte da lógica esportiva.