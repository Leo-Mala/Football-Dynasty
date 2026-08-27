# Football Dynasty — Cronograma oficial dos marcos restantes

Status: **ATIVO**

Este documento define como o desenvolvimento deve continuar após o merge certificado da Fase 8.

## Baseline oficial

- branch de integração: `phase4/core-game-domain`;
- merge certificado da Fase 8: `86d8344dce508916699b270692fd035aaf8b2bd1`;
- FINAL_HEAD certificado da Fase 8: `6c5c02b792b5c900fce98eeb8132187003c20725`;
- corpus oficial único: `Brasfoot.apk_Decompiler.com.zip`;
- SHA-256: `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`;
- package legado: `com.brasfoot.v2020`;
- versionCode: `202632`.

## Regra principal de execução

As fases restantes **não serão desenvolvidas, certificadas e mergeadas uma por uma**.

Elas serão agrupadas em **grandes marcos de desenvolvimento**, para reduzir microcommits, PRs excessivos e recertificações repetitivas. As numerações de fase continuam existindo como subblocos de escopo e checklist, mas o trabalho operacional deve ocorrer por marco.

Formato padrão de cada marco:

1. conferir o estado real do GitHub e o SHA da base;
2. abrir uma única branch do marco;
3. abrir um único PR Draft do marco;
4. investigar todos os subblocos do marco no corpus oficial antes de implementar;
5. trabalhar em blocos grandes e coerentes;
6. evitar CI completo após cada arquivo ou microcorreção;
7. executar testes locais/dirigidos durante o desenvolvimento;
8. disparar certificação completa apenas em checkpoints substanciais e no FINAL_HEAD;
9. manter o mesmo PR até o marco terminar;
10. congelar um FINAL_HEAD;
11. executar auditoria agregada do diff inteiro;
12. obter todos os gates obrigatórios verdes no mesmo SHA;
13. marcar Ready e fazer merge somente após a certificação final;
14. só então iniciar o marco seguinte a partir do merge certificado.

Nenhum marco pode inventar gameplay, dados esportivos, regras, telas ou funcionalidades não comprovadas no corpus legado. `AGENTS.md` e `docs/DATA_FREEZE.md` continuam obrigatórios.

---

# MARCO A — FASES 9–10

## Objetivo

**O mundo do futebol começa a funcionar como uma temporada real.**

Este marco integra o motor já reconstruído à carreira e fecha o ciclo competitivo básico.

### Fase 9 — Partida → carreira → calendário

Escopo interno:

- evento agendado da carreira;
- seleção dos clubes e jogadores corretos;
- adaptação fiel do estado persistido para o runtime certificado da Fase 8;
- execução da partida com o mesmo `RandomSource` da carreira;
- retorno do `Match` moderno;
- aplicação de gols, cartões, lesões, substituições e efeitos persistentes comprovados;
- atualização da partida no calendário;
- avanço coerente para a próxima data/rodada;
- persistência atômica de gameplay + RNG;
- save/reopen do resultado.

### Fase 10 — Competições, rodadas, tabelas e classificação

Escopo interno, somente quando comprovado pelo corpus:

- geração/ordenação de rodadas;
- atualização de classificação;
- pontos, vitórias, empates, derrotas e saldo quando aplicável;
- critérios de desempate;
- mata-mata;
- avanço/eliminações;
- campeões;
- promoções/rebaixamentos;
- classificações entre competições;
- encerramento e reinício competitivo da temporada.

## Gate do Marco A

O marco termina apenas quando uma carreira consegue:

`calendário → rodada → partida → resultado → tabela → próxima rodada → fim da competição`

com determinismo, persistência e paridade comprovada.

---

# MARCO B — FASES 11–14

## Objetivo

**O jogo vira um manager completo.**

Estas quatro fases são desenvolvidas dentro do mesmo grande ciclo porque possuem forte dependência entre elenco, mercado, finanças e treinador.

### Fase 11 — Elenco, escalação, banco e táticas

Escopo interno:

- elenco disponível;
- titulares e reservas;
- posições;
- formações;
- regras de elegibilidade;
- lesionados/suspensos;
- escalação automática da IA;
- táticas comprovadas;
- ajustes alcançáveis durante a partida;
- conexão completa das substituições da Fase 8 com o estado da carreira.

### Fase 12 — Mercado, transferências e contratos

Escopo interno:

- busca/seleção de jogadores;
- propostas;
- compra/venda;
- movimentação entre clubes;
- contratos;
- efeitos financeiros da transferência;
- empréstimos ou outros mecanismos somente se comprovados pelo legado;
- consistência de elenco e membership;
- persistência transacional.

### Fase 13 — Finanças, clube e estádio

Escopo interno:

- receitas e despesas comprovadas;
- salários;
- caixa do clube;
- efeitos financeiros de partidas/temporada quando comprovados;
- estádio;
- demais operações administrativas alcançáveis do clube.

### Fase 14 — Treinador e progressão de carreira

Escopo interno:

- criação de carreira do usuário;
- clube controlado;
- identidade/estado do treinador;
- troca de clube;
- demissão;
- propostas de emprego;
- reputação/objetivos apenas onde comprovados;
- progressão real da carreira do usuário.

## Gate do Marco B

O marco termina quando o usuário consegue administrar um clube de forma funcional durante a temporada:

`elenco/tática → partida → mercado/contrato → finanças → treinador → continuidade da carreira`

sem sistemas falsos ou parcialmente simulados.

---

# MARCO C — FASE 15

## Objetivo

**Garantir que nenhuma parte alcançável do Brasfoot foi esquecida.**

Esta é uma auditoria funcional completa do corpus oficial.

Deve ser criada uma matriz do tipo:

`LEGACY_FUNCTION / CALL_PATH / MODERN_EQUIVALENT / STATUS / EVIDENCE / TEST / ACTION`

Classificações mínimas:

- `IMPLEMENTED_AND_CERTIFIED`;
- `IMPLEMENTED_NEEDS_REVALIDATION`;
- `REACHABLE_NOT_IMPLEMENTED`;
- `UNREACHABLE`;
- `PRESENTATION_ONLY`;
- `SMALI_REQUIRED`;
- `UNKNOWN_NEEDS_INVESTIGATION`.

Se forem encontrados subsistemas alcançáveis ainda ausentes, eles devem ser tratados como subblocos `15.1`, `15.2`, `15.3` etc. **dentro do mesmo marco**, sem avançar para a Fase 16 enquanto existir lacuna funcional material.

## Gate do Marco C

- nenhuma função alcançável relevante sem classificação;
- nenhuma função comprovada omitida do moderno;
- nenhuma função inventada no moderno sem evidência legado;
- documentação antiga conflitante marcada como histórica/superseded;
- matriz de paridade agregada aprovada.

---

# MARCO D — FASE 16

## Objetivo

**Transformar tudo em carreira durável.**

Escopo:

- salvar/carregar todo o estado moderno necessário;
- fechar/reabrir o app sem perda de estado;
- continuidade de calendário, competição, elenco, contratos, finanças e treinador;
- RNG persistido atomicamente com o gameplay;
- isolamento total entre carreiras;
- rollback/transação em falha;
- virada de temporada repetida;
- jogadores procedurais e movimentações persistentes;
- migrações Room somente quando estado persistente comprovado exigir;
- nenhuma `fallbackToDestructiveMigration`.

Compatibilidade legado `.a26/.s26`:

- só pode ser declarada end-to-end se existir fixture real suficiente;
- sem fixture real, manter a limitação explícita em vez de criar decoder falso-verde.

## Gate do Marco D

Carreiras devem sobreviver a ciclos completos de:

`criar → jogar → salvar → fechar → reabrir → continuar → virar temporada`

com fingerprints/determinismo e isolamento validados.

---

# MARCO E — FASE 17

## Objetivo

**Transformar o núcleo técnico em aplicativo jogável.**

A UI será implementada somente sobre sistemas de domínio já comprovados e certificados.

Escopo de apresentação, conforme o legado comprovar:

- tela inicial;
- nova carreira/carregar carreira;
- escolha do clube;
- tela principal da carreira;
- elenco;
- escalação/táticas;
- calendário/rodadas;
- classificação;
- partida acompanhada;
- resultados;
- mercado;
- contratos;
- finanças;
- estádio;
- treinador;
- demais telas alcançáveis comprovadas na Fase 15.

A arquitetura pode usar Compose e navegação moderna, mas não pode remover, simplificar ou adicionar funções do jogo por decisão estética.

## Gate do Marco E

O APK precisa permitir, pela interface real:

`abrir app → criar/carregar carreira → escolher clube → administrar → jogar → avançar temporada → salvar/reabrir`

sem depender de telas de bootstrap ou comandos de teste.

---

# MARCO F — FASE 18

## Objetivo

**APK, testes reais e fechamento oficial.**

Escopo:

- auditoria funcional completa contra a matriz da Fase 15;
- integridade esportiva/factual;
- auditoria de RNG;
- auditoria Room/migrations;
- testes unitários e integrados;
- testes Android instrumentados quando aplicável;
- startup smoke em APK instalado;
- performance;
- testes de múltiplas temporadas;
- stress de 20/100 temporadas quando o sistema integrado estiver pronto;
- APK para teste manual do proprietário;
- correção dos bugs encontrados no aparelho;
- nova certificação após correções;
- somente depois, preparação de release oficial.

## Regra de entrega

A primeira entrega deste marco é **APK para teste manual**, não Release.

Release/tag/publicação só acontecem após:

1. APK instalado e testado manualmente;
2. bugs reais corrigidos;
3. recertificação do FINAL_HEAD;
4. autorização/critério de release aplicável ao projeto.

---

# Resumo dos grandes marcos

- **Fases 9–10:** o mundo do futebol começa a funcionar como temporada real.
- **Fases 11–14:** o jogo vira um manager completo.
- **Fase 15:** garante que não esquecemos partes do Brasfoot.
- **Fase 16:** transforma tudo em carreira durável.
- **Fase 17:** transforma o núcleo técnico em aplicativo jogável.
- **Fase 18:** APK, testes reais e fechamento.

## Regra para novos chats/agentes

Ao iniciar qualquer novo chat ou sessão de desenvolvimento deste projeto:

1. ler `AGENTS.md`;
2. ler `docs/DATA_FREEZE.md`;
3. ler **este arquivo** `docs/REMAINING_MILESTONES_ROADMAP.md`;
4. conferir o estado real das branches/PRs no GitHub;
5. identificar qual grande marco está ativo;
6. continuar o mesmo marco/branch/PR, sem recriá-lo;
7. usar as fases internas apenas como checklist de escopo;
8. não voltar ao modelo de uma branch/PR/certificação completa para cada subfase.

Este roadmap permanece provisoriamente válido até o fechamento da Fase 18. A Fase 15 pode descobrir subblocos adicionais obrigatórios, mas eles devem ser inseridos dentro do roadmap com evidência do corpus antes de avançar.