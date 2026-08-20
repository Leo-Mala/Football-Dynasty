# DATA_FILES — Fase 1

O diretório `assets` contém **5.764 arquivos**.

## Inventário por extensão

- `.ban`: **2.689**
- `.png`: **2.625**
- `.txt`: **446**
- `.bak`: **2**
- `.dat`: **1**
- `.ini`: **1**

## Inventário por pasta principal

- `teams`: **5.317** arquivos
- `names`: **222** arquivos
- `surnames`: **221** arquivos
- `removed_teams.txt`: **1** arquivo
- `eula.txt`: **1** arquivo
- `ok_bra.txt`: **1** arquivo
- `ok_seed.dat`: **1** arquivo

## `.ban` — base de clubes

Foram encontrados **2.689 arquivos `.ban`**. O cabeçalho binário começa com `AC ED 00 05`, assinatura de Java Object Serialization. Inspeção de strings e classes serializadas mostra objetos `e.t` (time) contendo listas de `e.g` (jogadores). Portanto `.ban` não é texto nem banco SQLite: é um grafo Java serializado.

Campos expostos pelo leitor `e.t/e.g` confirmam que esses arquivos carregam identidade do time, país/estado, nível, estádio/capacidade, reputação, cores, elenco, juniores e dados básicos de jogador.

## Names/surnames

Há centenas de `.txt` em `assets/names` e `assets/surnames`, indexados por códigos de país/região. Eles alimentam geração procedural de nomes e **devem permanecer congelados** durante a modernização.

## Imagens

Os **2.625 PNGs** no assets correspondem majoritariamente a recursos de clubes/escudos e outros elementos gráficos legados. Não foram alterados.

## `.bcf`

`options.bcf` não está pré-empacotado no assets; é criado em runtime pela aplicação e contém `est.Options` via Java serialization.

## Integridade

Nenhum asset esportivo foi modificado. Esta fase apenas catalogou os formatos e leitores.