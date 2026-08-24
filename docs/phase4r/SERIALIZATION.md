# SERIALIZATION — Fase 4R

## `.ban`

Os 1.687 arquivos `.ban` do novo baseline continuam sendo streams Java Object Serialization compatíveis com os shells isolados do projeto:

- `e.t` — clube/time; `serialVersionUID = 16L`;
- `e.g` — jogador/júnior; `serialVersionUID = 16L`.

A prova é empírica e read-only: 1.687/1.687 arquivos foram desserializados sem falha. Os nomes obfuscados permanecem confinados à camada `legacy.compatibility`/shells e não são propagados pelo domínio moderno.

## Metadata de save

O novo baseline usa metadata `*.a26` via Java Object Serialization e mantém `est.InfoArquivoSalvoType` como classe relevante. Como o corpus não contém save real, a compatibilidade do shell é estrutural, não uma certificação nível carreira.

## Carreira principal

O save principal ativo é `*.s26`. A inspeção Java/SMALI mostra caminho Kryo com registro não obrigatório. O método de load possui decompilação Java incompleta/suspeita em pontos críticos; o SMALI é a fonte de recuperação nesses trechos.

Classificação:

| Área | Estado |
|---|---|
| `e.t` / `.ban` team | `JAVA_CONFIRMED_BY_CORPUS` |
| `e.g` / player+júnior | `JAVA_CONFIRMED_BY_CORPUS` |
| metadata `InfoArquivoSalvoType` | `JAVA_PARTIAL` — sem fixture real |
| load principal `.s26` | `SMALI_REQUIRED` |
| fallback/backup do save | `SMALI_REQUIRED` |
| grafo completo de carreira | `UNKNOWN` até fixture real |

## Regra de segurança

`LegacySaveReader.readCareer()` continua deliberadamente bloqueado. Não será criado um decoder parcial fingindo compatibilidade. Para desbloquear, é necessário um save real Brasfoot 2026/27 preservado read-only, hash fixado, replay do caminho Kryo/SMALI e comparação semântica repetível.

## Isolamento

Nomes de classes obfuscadas, serialVersionUIDs e detalhes de wire format são compatibilidade de borda. Adapters convertem os objetos legado em snapshots imutáveis; DAOs, entidades Room e domínio moderno não dependem diretamente desses nomes.
