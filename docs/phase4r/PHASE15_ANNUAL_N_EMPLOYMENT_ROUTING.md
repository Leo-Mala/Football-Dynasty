# Fase 15 — roteamento anual de `best.a.n(boolean)`

Status: **IMPLEMENTED_AND_TESTED / BEST_B_A_CALLEE_OPEN**

Corpus oficial: `Brasfoot.apk_Decompiler.com.zip` — SHA-256 `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465` — package `com.brasfoot.v2020` — versionCode `202632`.

SMALI permanece a autoridade executável. Esta implementação usa somente a evidência já congelada e certificada em `PHASE15_ANNUAL_J2_REMAINING_CALLEE_EVIDENCE.md`; não atribui semântica nova a `best.f0`, `best.b.A(...)` ou `best.n.g`.

## Evidência congelada

`best.a.n(flag)` executa somente quando `best.b.N1()` é verdadeiro. Quando alcançável, percorre `best.b.H0()` em source order e lê `best.f0.K()`.

- `flag=false` (`cS`): somente entradas com `K()==true` e `y()==null` são elegíveis;
- `flag=true` (`cSempregado`): somente `K()==true` é exigido; `y()` não filtra a entrada;
- para cada entrada elegível, chama `best.b.A(f0, false)`;
- o retorno é imediatamente gravado em `best.n.g`;
- portanto cada entrada elegível sobrescreve a atribuição anterior e o valor final de `g` vem da última entrada elegível em source order.

Não há RNG no corpo de `best.a.n(boolean)`. Isso não prova ausência de RNG dentro de `best.b.A(...)`, que permanece boundary separado.

## Implementação moderna

`LegacyAnnualNEmploymentRoutingRules.plan(...)` congela somente:

- short-circuit quando `N1` é falso;
- elegibilidade por `K`/`y` para ambos os flags;
- preservação da ordem original;
- argumento constante `false` passado ao callee `A`;
- overwrite de `legacyG` após cada chamada elegível.

Nenhuma equivalência moderna de `best.n.g` foi inventada e o callee `best.b.A(...)` não foi implementado por aproximação.

## Regressões

`LegacyAnnualNEmploymentRoutingRulesTest` prova:

- `N1=false` produz zero chamadas;
- `cS` exige `K=true` e `y=null`;
- `cSempregado` ignora `y` mas continua exigindo `K=true`;
- source order é preservada;
- todas as chamadas usam o argumento legado `false`;
- cada chamada representa overwrite de `best.n.g`, deixando a última entrada elegível como origem da atribuição final.

## Persistência

Room permanece V14. Esta regra pura não autoriza persistência nova para `best.n.g` e não cria migration, default ou backfill.

## Classificação

- `best.a.n(false)` routing → **IMPLEMENTED_AND_TESTED**;
- `best.a.n(true)` routing → **IMPLEMENTED_AND_TESTED**;
- `best.b.A(best.f0,false)` → **REACHABLE_NOT_IMPLEMENTED / INVESTIGATION_REQUIRED**;
- durabilidade/uso posterior de `best.n.g` → **PERSISTENCE_MAPPING_OPEN**;
- `cS` / `cSempregado` end-to-end → **PARTIALLY_IMPLEMENTED**.

A Fase 15 permanece aberta até o callee `best.b.A(...)`, seu efeito e a necessidade real de persistência de `best.n.g` serem classificados e testados.
