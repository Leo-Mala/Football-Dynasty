# MIGRATION_MAPPING — Fase 1

| Legado | Responsabilidade | Destino moderno sugerido | Dependência para migrar |
|---|---|---|---|
| `c.a.TF` (`a.b`) | estado global da carreira | `GameState`/repositório de save | adapter de compatibilidade |
| `a.p` | jogador | `Player` domain model | mapear todos campos antes de renomear |
| `a.ac` | clube | `Club` domain model | preservar elenco/finanças/históricos |
| `a.t` | partida | `MatchState` | preservar arrays/side effects |
| `components.cn` | motor/estatísticas da partida | `MatchEngine` | testes por cenários + controle de RNG |
| `d.q` + `d.*` | competições | `CompetitionEngine` + rule sets | paridade de tabelas/calendário |
| `GfxCore` | god object transversal | vários services/use cases | extração incremental por subsistema |
| `ActivitySave/Load`, `c.a/c.b` | persistência | `LegacyPersistenceAdapter` + futuro repository | ler saves legados antes de novo schema |
| `est.Options` | preferências | DataStore/domain settings | migrar sem mudar defaults |
| Activities | UI | Compose screens + ViewModels | somente após separar regra de UI |
| `Rc*` listeners | ações de UI/regras pequenas | intents/actions/use cases | rastrear side effects antes de excluir |

## Ordem recomendada de extração

1. Formalizar modelos e fixtures de leitura `.ban`/save.
2. Isolar RNG e motor de partida sem alterar fórmulas.
3. Isolar calendário/competição.
4. Isolar contratos/mercado/finanças.
5. Introduzir persistência moderna atrás de adapter.
6. Migrar UI tela a tela para Compose.

Nenhuma dessas migrações foi executada na Fase 1; este arquivo é somente mapa.