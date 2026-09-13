# Fase 17 — auditoria inicial de superfícies UI

Status: **ACTIVE**

Baseline de integração: `phase4/core-game-domain@8ad1da933ba2d267f06de7e900c6f3dfb2f8e69b`.

Corpus oficial único: `Brasfoot.apk_Decompiler.com.zip` — SHA-256 `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465` — package `com.brasfoot.v2020` — versionCode `202632`.

## Objetivo

Transformar o núcleo certificado em aplicativo jogável sem inventar ou remover funções. A UI moderna é uma camada de apresentação sobre boundaries de domínio/runtime já comprovados.

## Gate do Marco E

A interface real deve permitir:

`abrir app → criar/carregar carreira → escolher clube → administrar → jogar → avançar temporada → salvar/reabrir`

sem depender de `Phase2BootstrapScreen`, comandos de teste ou mutações artificiais.

## Superfícies autorizadas pelo roadmap/matriz da Fase 15

As superfícies abaixo estão autorizadas para implementação porque já aparecem no roadmap do Marco E e/ou representam presentation seams de funções classificadas na Fase 15:

- tela inicial;
- nova carreira / carregar carreira;
- escolha do clube;
- tela principal da carreira;
- elenco;
- escalação / táticas;
- calendário / rodadas;
- classificação;
- partida acompanhada;
- resultados;
- mercado;
- contratos;
- finanças;
- estádio;
- treinador;
- juniores;
- encerramento / virada de temporada;
- demais telas somente após vínculo explícito com uma linha alcançável da matriz da Fase 15.

## Estado moderno inicial

- `MainActivity` ainda renderiza `Phase2BootstrapScreen`;
- o diretório `ui/` ainda contém somente esse bootstrap;
- Room corrente é V17, herdada do Marco D; nenhum bump de schema é justificado por UI;
- a Fase 16 já certificou save/load/reopen, isolamento de carreira, RNG atômico e transações; a Fase 17 deve consumir esses owners, não duplicá-los.

## Regras de implementação

1. cada ação de UI deve apontar para um owner moderno certificado antes de ser habilitada;
2. sem no-op fingindo função pronta;
3. sem estado de carreira paralelo dentro de composables;
4. sem regra esportiva no View/Composable;
5. loading/error/empty podem ser modernos, mas não podem alterar gameplay;
6. navegação moderna pode substituir Activities legadas sem remover capacidade alcançável;
7. nenhuma tela nova por conveniência estética;
8. nenhum Room V18 apenas para navegação/UI;
9. testes de apresentação devem validar wiring e não substituir testes de domínio;
10. `Phase2BootstrapScreen` só será removida do entrypoint quando o fluxo inicial real estiver ligado aos owners de carreira.

## Primeira sequência de implementação

1. congelar destinos UI comprovados em um contrato de navegação sem semântica inventada;
2. mapear owners reais de criar/listar/carregar carreira e seleção de clube;
3. implementar tela inicial + fluxo nova/carregar carreira;
4. implementar seleção de clube usando somente dados persistidos/corpus certificado;
5. entrar na tela principal da carreira real;
6. depois expandir, em blocos coerentes, para elenco/táticas, calendário/classificação, partida/resultados, mercado/contratos, finanças/estádio, treinador/juniores e fim de temporada;
7. remover bootstrap técnico do caminho de produção somente quando seu substituto funcional estiver comprovado.

## Não objetivos

- nenhuma mudança de dados esportivos;
- nenhuma regra nova;
- nenhuma reinterpretação de quirks legados;
- nenhuma compatibilidade `.a26/.s26` além do que a Fase 16 realmente comprovou;
- nenhuma Release/tag/publicação.
