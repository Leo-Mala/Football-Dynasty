# DECOMPILER_RISKS — Fase 1

Além dos 104 métodos explicitamente substituídos por `Method not decompiled`, a varredura encontrou **478 avisos/padrões de decompilação suspeita em 73 classes internas**. Foram contados: `Code decompiled incorrectly`, `Removed duplicated region`, falhas de inferência de tipo e `Unreachable blocks removed`.

## Maiores hotspots

| Classe | Ocorrências |
|---|---:|
| `a.t` | 42 |
| `a.p` | 41 |
| `a.af` | 34 |
| `ActivityClass` | 26 |
| `GfxCore` | 22 |
| `components.q` | 16 |
| `d.ac` | 14 |
| `d.m` | 14 |
| `a.q` | 12 |
| `a.y` | 12 |
| `ActivityPenalty` | 12 |
| `ActivityJogo` | 10 |
| `components.b` | 10 |
| `DialogIgrokInfo` | 8 |
| `components.r` | 8 |
| `a.u` | 7 |
| `ActivityEditorTeam` | 7 |
| `ActivityMainTeam` | 7 |
| `components.cn` | 7 |

Outras classes afetadas incluem `a.ab`, `a.ac`, `a.f`, `DialogTatics`, `NumberFormat`, `components.ae/ap/c/bd/t`, `d.q`, `ActivityResults`, `ActivityEscala`, `ActivityProcura`, `ActivityConvoca`, `ActivityEditor`, `ActivityEscolhaTimes`, `ActivityLoad`, `ActivityPref`, `ActivitySavedTatics`, `ActivityTimes`, `d.y`, `RangeSeekBar`, `StartOptions` e outras de menor incidência.

## Regra de uso do Java decompilado

1. Java é fonte de leitura e estrutura, não autoridade absoluta.
2. Qualquer método crítico que contenha warning deve ser comparado ao SMALI antes da migração.
3. Não corrigir automaticamente condições estranhas ou casts apenas porque parecem errados.
4. Métodos com grande número de warnings e impacto em partida, jogador, competição ou save recebem prioridade máxima em testes de caracterização.
5. `a.t`, `a.p`, `a.af`, `ActivityClass`, `GfxCore` e `components.cn` são hotspots de risco para a Fase 2.