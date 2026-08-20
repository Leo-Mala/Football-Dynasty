# CLASS_INVENTORY — Fase 1

A varredura estática encontrou **575 arquivos Java** no archive. Foram classificados como código interno do jogo/suporte **326 classes**; as demais pertencem principalmente a Android Support/ConstraintLayout, Google Play Billing, Kryo, ReflectASM, Objenesis e ASM.

## Pacotes internos

| Pacote | Classes | Papel predominante |
|---|---:|---|
| `com.brasfoot.v2028` | 118 | Activities, GfxCore, listeners/actions e infraestrutura do app |
| `components` | 98 | adapters, UI auxiliar e algoritmos/motor interno ofuscado |
| `d` | 46 | ligas, competições e estruturas esportivas |
| `a` | 38 | entidades e estado central do domínio |
| `est` | 9 | DTOs/configurações serializáveis |
| `field` | 6 | campo visual/controles táticos |
| `b.a` | 4 | helpers internos ofuscados |
| `c` | 2 | singleton global e reconstrução de referências de save |
| `e` | 2 | modelo dos arquivos `.ban` (`e.t` time, `e.g` jogador) |
| `f` | 2 | helpers de reconstrução/coleções |
| `b` | 1 | helper interno |

## Classes críticas já identificadas

- `a.b`: agregado raiz serializável da carreira/save.
- `a.p`: jogador.
- `a.ac`: clube/time.
- `a.t`: partida/estado de jogo.
- `d.q`: liga/competição genérica com turnos, grupos, mata-mata e rebaixamento.
- `components.cn`: parte central do motor de partida/simulação, com `Random` e acesso a `a.t/a.ac/a.p`.
- `c.a`: singleton `TF` da carreira + opções + persistência.
- `c.b`: reindexação/reconstrução de referências antes/depois de save/load.
- `GfxCore`: god object com ~22.713 linhas e ~942 métodos detectáveis.

## Cobertura detalhada

O inventário completo por classe foi usado para gerar os demais mapas da Fase 1. A contagem por classe considera métodos detectáveis no Java decompilado; trechos perdidos são catalogados em `SMALI_RECOVERY.md`. Como o decompilador produziu avisos em dezenas de classes, essas contagens são aproximações e o SMALI continua sendo a referência em trechos críticos.

## Regra de migração

Nenhuma classe de domínio deve ser renomeada/reinterpretada apenas pela aparência do Java decompilado. O significado de campos ofuscados deve ser confirmado por chamadas, serialização e comportamento antes de criar equivalentes Kotlin.