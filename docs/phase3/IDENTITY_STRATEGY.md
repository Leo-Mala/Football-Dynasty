# Estratégia de Identidade

## Princípio

A Fase 3 usa `StableLegacyIdentity` para produzir IDs determinísticos. IDs não dependem de UUID aleatório, timestamp, `rowId`, ordem de SELECT ou `hashCode()` instável de objetos.

## Clube

A identidade do clube é derivada de material canônico de origem, incluindo referência legada estável; não depende apenas do nome visível.

## Jogador

A identidade do jogador combina contexto estável de origem e posição no roster legado; jogadores homônimos não são tratados como a mesma entidade apenas pelo nome.

## Competições e carreiras

Os contratos V1 reservam a mesma regra: material de origem canônico + SHA-256 quando uma fonte real caracterizada existir. Nenhuma identidade factual é inventada para fontes ainda bloqueadas.

## Canonicalização e hash

Fingerprints e material de identidade usam representação determinística e SHA-256. O corpus moderno é ordenado antes do hash para que a ordem de leitura do banco não altere o resultado.

## Evidência

Na caracterização previamente executada do corpus `.ban` original:

- arquivos: 2.689
- IDs de clubes duplicados: 0
- IDs de jogadores duplicados: 0

A suíte final também testa que o mesmo input produz o mesmo ID e que mudanças de ordem não alteram identidades já produzidas.
