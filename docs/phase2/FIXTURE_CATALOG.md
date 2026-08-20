# FIXTURE_CATALOG — Fase 2

## Política

Fixtures de compatibilidade são cópias imutáveis de amostras do legado usadas exclusivamente para caracterização. Elas não são editadas, normalizadas nem atualizadas para dados atuais.

Para reduzir redistribuição desnecessária, a Fase 2 versiona somente a menor amostra necessária para testes automatizados. O corpus completo permanece no ZIP/APK de referência fornecido pelo proprietário do repositório.

## Fixture 001 — time `.ban`

- **Arquivo legado:** `resources/assets/teams/12deoctubre_par.ban`
- **Fixture no repositório:** `app/src/test/resources/legacy/12deoctubre_par.ban.b64`
- **Representação:** bytes originais codificados em Base64, sem transformação semântica
- **Tamanho binário:** 1.870 bytes
- **SHA-256 dos bytes originais:** `7f386a66e3e87042695b6dfaf23f2bc53143cfe8fa35b91a95ccd5ad060e85a7`
- **Tipo serializado:** `e.t`, contendo `e.g`
- **Finalidade:** provar compatibilidade Java Serialization, mapeamento de time/jogadores e fingerprint determinístico
- **Testes:** `LegacyBanCharacterizationTest`, `LegacyFormatProbeTest`, `LegacyDataIntegrityTest`

### Snapshot de caracterização

- nome: `12 de Octubre`
- fileRef: `12deoctubre_par`
- país legado: `150`
- jogadores: `20`
- juniores: `0`
- primeiro jogador: `Mauro Cardozo`
- idade do primeiro jogador: `38`
- posição legada do primeiro jogador: `0`
- fingerprint semântico: `9b0d1878744ce2d64a99db8a4103ba18e8f0286706ec4e30142cd585011d79a6`

## Corpus `.ban` usado para validação externa

O ZIP decompilado contém **2.689 arquivos `.ban`**. A varredura read-only com o leitor da Fase 2 obteve:

- 2.689 arquivos lidos;
- 66.003 jogadores nas listas principais;
- 13.098 juniores;
- 0 falhas de desserialização.

O manifesto ordenado `SHA-256 + caminho relativo` dos 2.689 `.ban`, gerado durante a validação local, possui SHA-256 agregado:

`f676890f3b11a5b20a774c7865e354a837bf48ee311c5c12e89ae78852b93ef0`

Esse hash é um checkpoint do corpus analisado; não substitui os arquivos originais.

## Fixtures de save ainda ausentes

Não há no conjunto fornecido uma carreira real contendo `*.ai21 + *.s21/*.s121`. Consequentemente, nenhuma fixture artificial de carreira foi inventada. Quando uma fixture real for disponibilizada, ela deverá receber SHA-256, tamanho, origem, finalidade e nível de suporte antes de ser usada.
