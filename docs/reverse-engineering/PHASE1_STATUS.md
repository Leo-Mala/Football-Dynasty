# PHASE1_STATUS — Engenharia Reversa

## Resultado

- [x] Manifest, SDKs, permissões e Activities inventariados.
- [x] **326 classes internas** inventariadas (incluindo pacote `components`).
- [x] **54 Activities** declaradas no manifest mapeadas: 53 do app + 1 do Google Play Billing.
- [x] `GfxCore` decomposto conceitualmente por responsabilidade.
- [x] **104 stubs Java decompilados incorretamente** catalogados em todos os pacotes internos e cruzados com SMALI.
- [x] **104/104** stubs possuem SMALI correspondente localizado.
- [x] **478 avisos/padrões de decompilação suspeita em 73 classes** catalogados como risco adicional.
- [x] Arquivos de dados catalogados: **2.689 `.ban`**, **2.625 `.png`**, **446 `.txt`** + auxiliares.
- [x] Estrutura `.ban` identificada como Java Object Serialization de `e.t/e.g`.
- [x] Save/load e formatos `.bcf`, `.ai21`, `.s21/.s121`, `.sbck` documentados.
- [x] Dependências externas e internas principais mapeadas.
- [x] Threading legado mapeado.
- [x] Modelo legado → arquitetura futura documentado.
- [x] Nenhum jogador, atributo, elenco, clube, competição ou asset esportivo foi alterado.

## Qualidade/limitações do decompilado

O Java não pode ser tratado isoladamente como fonte final de comportamento. Os maiores hotspots são `a.t`, `a.p`, `a.af`, `ActivityClass`, `GfxCore`, `components.cn`, `d.q` e classes com métodos grandes recuperados do SMALI.

## Cobertura

**Cobertura estrutural da Fase 1: alta.** Classes, telas, arquivos, persistência, dependências, stubs e riscos de decompilação estão catalogados.

**Cobertura semântica de código ofuscado: parcial por definição.** Os stubs possuem recuperação estrutural SMALI, mas métodos ofuscados complexos não recebem nomes/intenções inventados. O significado final deve ser fechado por subsistema durante testes de caracterização antes da porta para Kotlin.

Isso não bloqueia a Fase 2: bloqueia apenas a reescrita “no escuro”.

## Gate para Fase 2

A Fase 2 deve começar pelo **bootstrap Android realmente compilável + harness de compatibilidade**, ainda sem alterar regras do jogo. Prioridades:

1. Gradle Wrapper e build local verificável sem CI automático.
2. Fixtures/leitores para `.ban` e formatos de save.
3. Testes de caracterização do estado `a.b`, jogador `a.p`, clube `a.ac`, partida `a.t` e competição `d.q`.
4. Controle/instrumentação de RNG para conseguir reproduzir cenários.
5. Teste de leitura de save legado antes de qualquer proposta de Room/DataStore.

## GitHub Actions

Nenhum GitHub Actions foi usado nesta Fase 1. A análise e validação foram locais para preservar a cota mensal.