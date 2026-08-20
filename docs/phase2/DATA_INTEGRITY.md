# DATA_INTEGRITY — Fase 2

## Regra

O conteúdo esportivo legado permanece congelado. A modernização pode mudar representação técnica, nunca valores factuais ou regras durante esta fase.

## Proteções implementadas

### 1. Fixture byte-for-byte

`LegacyDataIntegrityTest` calcula SHA-256 dos bytes decodificados da fixture `.ban` e exige exatamente:

`7f386a66e3e87042695b6dfaf23f2bc53143cfe8fa35b91a95ccd5ad060e85a7`

Qualquer alteração de um único byte falha o teste.

### 2. Fingerprint semântico

A mesma fixture é desserializada e convertida para snapshot imutável. O fingerprint canônico precisa permanecer:

`9b0d1878744ce2d64a99db8a4103ba18e8f0286706ec4e30142cd585011d79a6`

Isso protege os campos mapeados contra mudanças acidentais no adapter, mesmo quando os bytes da fixture não mudam.

### 3. Corpus completo externo

A leitura read-only foi executada sobre os 2.689 `.ban` fornecidos:

- 2.689/2.689 lidos;
- 66.003 jogadores principais;
- 13.098 juniores;
- 0 falhas.

O manifesto ordenado de `SHA-256 + caminho` tem hash agregado:

`f676890f3b11a5b20a774c7865e354a837bf48ee311c5c12e89ae78852b93ef0`

Esse checkpoint permite detectar se o corpus de referência mudar entre auditorias sem versionar os 2.689 binários no novo repositório.

## Conteúdo protegido

Até o fim da reconstrução/paridade, não atualizar ou normalizar:

- jogadores e nomes;
- idade/posição/atributos/ratings;
- vínculo jogador → clube;
- elencos;
- clubes e países;
- divisões e competições;
- formatos e regras esportivas.

## Limitação atual

A proteção automatizada versionada cobre uma fixture representativa e seu snapshot. O corpus completo é protegido por checkpoint externo porque os arquivos não foram copiados em massa para o repositório novo. Quando os dados forem incorporados tecnicamente em fase futura, o gate deve evoluir para um manifesto versionado do conjunto efetivamente importado antes de qualquer conversão.
