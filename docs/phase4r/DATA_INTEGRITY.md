# DATA_INTEGRITY — Fase 4R

## Baseline físico

Arquivo oficial: `Brasfoot.apk_Decompiler.com.zip`

SHA-256:

`3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`

O corpus foi caracterizado read-only. Nenhum `.ban` original foi regravado.

## Corpus `.ban`

- total: 1.687;
- `assets/teams2026`: 687;
- `assets/packs`: 1.000;
- leitura: 1.687/1.687;
- falhas: 0;
- jogadores: 35.432;
- juniores: 4.164;
- hashes físicos únicos: 1.515;
- duplicados byte-identical: 172.

Fingerprint agregado físico:

`150643f62e29bcd211a83843239e2ba52721bae1decb6d6021bfa1554f957e1f`

Fingerprint agregado semântico:

`377baca2c9b5f806ab2a94eabbbc34aa2e012681f5ecce8c9a5b19441dff5a37`

## Fixture ativa

A fixture versionada da Fase 4R é derivada diretamente do novo `teams2026` e é imutável. O teste fixa tanto SHA-256 físico quanto fingerprint semântico. A fixture antiga permanece apenas como regressão histórica superseded e não define mais fatos esportivos oficiais.

## Política de alteração

Durante a Fase 4R é proibido alterar fatos esportivos a partir de fontes externas. Não foram usados FIFA/FC/Kaggle/API-Football, conhecimento atual de elencos, ratings ou competições.

Expected values só podem mudar quando houver evidência rastreável de troca de baseline. Mudanças de bytes e mudanças semânticas são tratadas separadamente.

## IDs modernos

IDs modernos continuam determinísticos e derivados de campos legado/posição estável no stream. Nenhum UUID ou ID aleatório irreproduzível é usado para materializar a base esportiva.

## Gate

A fase só pode encerrar com:

- 0 falhas de leitura `.ban`;
- fixture ativa íntegra;
- fingerprints fixados;
- testes de identidade/determinismo verdes;
- nenhuma alteração esportiva arbitrária detectada no diff.
