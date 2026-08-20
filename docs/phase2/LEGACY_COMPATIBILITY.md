# LEGACY_COMPATIBILITY — Fase 2

## Objetivo

Criar uma camada de leitura que preserve os formatos antigos antes de qualquer migração de regras de negócio ou persistência.

## `.ban` — compatibilidade comprovada

Os arquivos `.ban` são Java Object Serialization. O stream real começa com `AC ED 00 05` e referencia as classes `e.t` (time) e `e.g` (jogador-base), ambas com `serialVersionUID = 16`.

A Fase 2 adicionou shells de serialização em `app/src/main/java/e/` com os nomes de pacote/classe e nomes de campos do byte stream preservados. Esses shells existem **somente para compatibilidade de desserialização** e não são o futuro modelo de domínio.

`LegacySerialization.readBan()`:

1. valida o magic do Java Object Serialization;
2. desserializa `e.t` / `e.g`;
3. converte para snapshots imutáveis em `domain/model`;
4. não grava nem modifica o arquivo legado.

### Prova executada

Fixture real e inalterada do APK:

`resources/assets/teams/12deoctubre_par.ban`

SHA-256 do binário original:

`7f386a66e3e87042695b6dfaf23f2bc53143cfe8fa35b91a95ccd5ad060e85a7`

Resultado observado pelo leitor:

- time: `12 de Octubre`
- fileRef: `12deoctubre_par`
- país legado: `150`
- jogadores: `20`
- juniores: `0`
- primeiro jogador: `Mauro Cardozo`
- idade: `38`
- posição legada: `0`

Fingerprint canônico do snapshot:

`9b0d1878744ce2d64a99db8a4103ba18e8f0286706ec4e30142cd585011d79a6`

A fixture é armazenada em Base64 apenas para permitir versionamento textual no Git; ao executar o teste ela é decodificada sem transformação semântica.

## `.ai21` — camada preparada, fixture real ausente

A classe legada `est.InfoArquivoSalvoType` usa Java serialization (`serialVersionUID = 1`) e possui os campos `a`, `n`, `tc`, `i`, `path`.

Foi criada a shell de compatibilidade com identidade serial exata e `LegacySerialization.readSaveMetadata()`.

O APK/decompilado fornecido não contém um arquivo `.ai21` de carreira real. Portanto a estrutura está implementada, mas **compatibilidade com um save real ainda não foi empiricamente provada**.

## `.s21` / `.s121` — não fingir compatibilidade

A Fase 1 confirmou o caminho moderno de gravação do agregado `a.b` via Kryo e a necessidade de reconstrução de referências transitórias após load. Não existe fixture real de carreira entre os arquivos fornecidos.

Por isso a Fase 2 não introduz um leitor incompleto ou inventado para o grafo inteiro. A próxima prova exige um save real e o conjunto exato de classes/serializers usados pelo caminho de load.

## Regra de segurança

Nenhum leitor desta fase escreve de volta no formato antigo. A camada é estritamente read-only até que os testes de caracterização demonstrem paridade suficiente para uma estratégia de migração versionada.
