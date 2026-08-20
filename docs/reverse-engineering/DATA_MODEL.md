# DATA_MODEL — Fase 1

O modelo abaixo é derivado de campos, getters/setters, construtores e tipos serializáveis encontrados no código. Nomes ofuscados são mantidos exatamente.

## Jogador — `a.p`

`a.p implements Serializable` e é a entidade central de jogador. Campos explicitamente legíveis incluem `anoIn`, `aposentado`, `energiaBase`, `forca`, `pais`, `posicao`, `salario`, `status`, métricas de prêmios (`aw*`), seleção (`selGoals`, `selAssists`, `selTitles`), cláusula/renovação (`rcClause`, `rcRenewYear`, `rcConvYear`), venda pendente (`pendSaleClub`, `pendSaleValue`, `pendIsLoan`) e várias métricas ofuscadas. Mantém listas de históricos/eventos e referências transitórias ao clube.

## Clube/Time — `a.ac`

`a.ac implements Serializable`. Mantém país, divisão, nome/identidade ofuscada, capacidade/camisa, elenco `ArrayList<p> xp`, listas auxiliares de jogadores, capitães, finanças/investimentos (`ctInvest`, preços, sponsor), scouting, reputação/históricos/recordes e estado de agenda.

## Partida — `a.t`

`a.t implements Serializable`. Mantém dois clubes (`qT`, `qU`), placar/arrays de estatísticas, data, renda, listas transitórias de jogadores/eventos, estado de narração, flags de fim e uma referência transitória `components.cn`, que é parte do motor de simulação e usa `Random`.

## Carreira/Save raiz — `a.b`

`a.b implements Serializable` é o agregado raiz da carreira. Ele importa e referencia ligas/competições de `d.*`, opções e estruturas de histórico. O singleton global `c.a.TF` aponta para a instância corrente de `a.b`.

## Liga/Competição — `d.q` e subclasses/associadas

`d.q extends a.al implements Serializable`. Campos legíveis incluem `nomeLiga`, `nomeDivisao`, `divisao`, `nTimes`, `nRebaixados`, `doisTurnos`, `duasVoltasMataMata`, `jogosDentroGrupo`, `melhoresTerceiros`, listas de clubes e estruturas de classificação. Classes `d.cne`, `d.css`, `d.cv`, `d.cpaulista`, `d.crio`, `d.sm`, `d.africa`, `d.asia`, `d.ofc` etc. especializam formatos/competições.

## Dados de arquivos `.ban` — `e.t` / `e.g`

`e.t` representa um time carregado do arquivo `.ban`: nome, país, estado, nível, estádio, capacidade, reputação, cores e listas de jogadores/juniores. `e.g` representa jogador do arquivo-base com nome, idade, país, posição, status, lado, flags estrela/top mundial e campos `cr1/cr2`.

## Opções — `est.Options`

Objeto Java serializável persistido em `options.bcf`. Inclui calendário/competições habilitadas, salário mensal, energia real, velocidade, sons, cores, auto-save, transfer ban, finanças realistas e outras preferências.

## Metadado de save — `est.InfoArquivoSalvoType`

Objeto serializável com cinco campos: `a`, `n`, `tc`, `i`, `path`. É usado pelas telas de Save/Load para listar carreira sem precisar carregar o grafo completo.

## Estado global

- `c.a.TF`: carreira corrente (`a.b`).
- `c.a.TG`: opções (`est.Options`).
- `GfxCore` e várias classes `components.*` acessam esse estado diretamente.
- Há numerosos campos `transient` que são reconstruídos após load por rotinas `c.b.wA/wB/wE/wF`, `TF.da()`, `v.g(context)`, `TF.p(true)` e `o.hv()`.