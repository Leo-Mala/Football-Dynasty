# Fase 15 — sprint de fechamento acelerado

Status: **ACTIVE / EXECUTION QUEUE**

Objetivo: fechar o Marco C em lotes coerentes, sem recertificar cada micro-boundary e sem relaxar qualquer gate. Este arquivo não cria gameplay; apenas ordena gaps já provados na matriz agregada.

## Política de execução

- baseline limpo: usar sempre o último HEAD remoto com os três workflows obrigatórios verdes;
- agrupar implementação + testes + documentação de um mesmo subsistema antes de novo checkpoint CI;
- não reabrir Fase 15.1 Juniores, `d4/o2`, `e4/y1`, decline senior, high-d0 RNG ou finalização de growth sem regressão real;
- nenhum detalhe novo do corpus é inferido quando o ZIP/SMALI bruto oficial não está materializado;
- Room permanece V14 até o mapa completo dos estados anuais provar o delta mínimo necessário.

## Lote A — progressão senior + persistência mínima

Fechar em conjunto:

1. trecho precedente restante de `best.o.s()`;
2. todos os readers/writers de `M` e `N`;
3. composição `best.o.e()` → growth/decline → clear M;
4. decidir o menor delta persistente possível somente após prova completa;
5. se schema novo for inevitável: migration aditiva, schema exportado, migration test, reopen e rollback no mesmo lote.

Blocker atual: o trecho restante de `best.o.s()` é `SMALI_REQUIRED`; as taxas/branches não podem ser inferidas.

## Lote B — reset/tournament anual

Fechar em conjunto:

1. `best.k0.c(index)` após o traversal já congelado;
2. `components.n1` thresholds/inputs;
3. identidade/cardinalidade/lifecycle de `best.h0`;
4. flag de jogador acionada nesse fluxo;
5. readers/writers de `j0` e `d/W0`;
6. composição final de `best.b.F()` preservando a ordem dos três passes e a quirk de `z0()[0]`.

Não refazer `[0,1,2,2,5,6,6,3,3,4,4]`, `d1(0)` ou o controle de `D0()`.

## Lote C — `J(1)` anual

O dispatcher já está congelado. Fechar apenas os callees materiais:

1. `cw → best.a.q()` — rebuild de torneio + collections/RNG;
2. `cD → best.a.p()` — mutação de torneio/listas;
3. `cS/cSempregado → best.b.A(f0,false)` e lifecycle de `best.n.g`;
4. `ds → best.c0.q()/E(long)` — equivalência numérica da folha salarial.

`dJ`, o roteamento `ds`, o roteamento `n(false/true)` e o sweep `aj` não devem ser reimplementados.

## Lote D — seleção anual `best.f`

Compor as regras já existentes sem duplicar RNG:

1. coleções candidatas reais e sua source order;
2. filtros/ranges já congelados em `LegacyAnnualSelectionRules`;
3. draws já congelados em `LegacyAnnualRandomRules`;
4. política explícita para os pontos de RNG implícito do APK sem alegar seed parity;
5. conectar o resultado ao `LegacyAnnualG4TransferExecutionRule` já certificado.

## Lote E — fechamento Marco C

Somente quando A–D estiverem fechados:

1. auditoria completa do diff do PR #16;
2. matriz agregada sem `UNKNOWN_NEEDS_INVESTIGATION`/`REACHABLE_NOT_IMPLEMENTED` material alcançável;
3. 0 review threads/blockers materiais;
4. um FINAL_HEAD;
5. nesse exact SHA: `Phase 7 Validation`, `Phase 8 Validation`, `Phase 8 Final Certification` todos SUCCESS;
6. só então encerrar Fase 15 e avaliar entrada na Fase 16.

## Regra de velocidade

Não criar commits apenas para repetir documentação de um único método. Cada rodada deve, sempre que a evidência permitir, consumir um lote inteiro ou uma fatia funcional substancial dele antes do próximo checkpoint CI.
