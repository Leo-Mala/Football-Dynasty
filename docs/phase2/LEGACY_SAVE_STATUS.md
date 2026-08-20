# LEGACY_SAVE_STATUS — Fase 2

## Estado por formato

| Formato | Evidência no legado | Estado atual | Nível |
|---|---|---|---:|
| `options.bcf` | `est.Options` via Java Object Serialization | estrutura e magic conhecidos; sem fixture real fornecida | 1 |
| `*.ai21` | `est.InfoArquivoSalvoType` via Java Object Serialization | shell + leitor implementados; sem fixture real fornecida | 1 |
| `*.s21` / `*.s121` | agregado de carreira `a.b`, caminho moderno com Kryo e caminho legado existente | reconhecido, mas leitura deliberadamente bloqueada sem fixture real | 1 |
| `*.sbck` | backup binário do save principal | reconhecido como backup; sem decodificação independente | 1 |
| `*.ban` | `e.t/e.g` via Java Object Serialization | leitura comprovada em 2.689/2.689 arquivos do corpus fornecido | não é save de carreira |

## Escala de suporte

- **Nível 0** — não reconhecido.
- **Nível 1** — arquivo/formato identificado.
- **Nível 2** — cabeçalho/metadados lidos de fixture real.
- **Nível 3** — estrutura principal desserializada de fixture real.
- **Nível 4** — referências transitórias reconstruídas.
- **Nível 5** — carreira completamente carregada e comparada com o legado.
- **Nível 6** — save utilizável pelo aplicativo moderno.

## Por que o save principal continua no Nível 1

O APK e o ZIP decompilado fornecidos contêm o código que grava/carrega os formatos, mas não contêm uma carreira real formada pelo conjunto `*.ai21 + *.s21/*.s121` para caracterização empírica. Sem essa fixture seria incorreto inventar registro Kryo, ordem de campos, reconstrução de referências ou declarar compatibilidade.

`LegacySaveReader.readCareer()` permanece propositalmente bloqueado com `UnsupportedOperationException`. Isso é uma proteção: impede que uma implementação parcial seja confundida com suporte real.

## Gate para elevar o nível

Para avançar do Nível 1:

1. obter um save real criado pelo aplicativo legado;
2. preservar os arquivos originais read-only e registrar SHA-256;
3. caracterizar primeiro o `*.ai21`;
4. reproduzir exatamente o caminho de leitura do `*.s21/*.s121` observado no código/SMALI;
5. executar as rotinas pós-load na mesma ordem;
6. capturar snapshots de `a.b`, `a.p`, `a.ac`, `a.t` e `d.q`;
7. comparar resultados repetíveis antes de criar qualquer conversor para persistência moderna.

Até esse gate, Room/DataStore não substituem o save legado.
