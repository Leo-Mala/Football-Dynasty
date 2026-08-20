# DEPENDENCY_MAP — Fase 1

## Dependências externas confirmadas pelo código decompilado

| Dependência | Uso observado | Classificação futura |
|---|---|---|
| Google Play Billing (`com.android.billingclient`) | compras/subscrição e `ProxyBillingActivity` | infraestrutura comercial, não regra esportiva |
| Kryo (`com.esotericsoftware.kryo`) | serialização do grafo de carreira em save | crítico para compatibilidade de saves |
| ReflectASM / Objenesis / ASM | dependências transitivas do ecossistema Kryo/serialização | compatibilidade técnica |
| Android Support / ConstraintLayout antigo | UI legada | substituir por AndroidX/Compose após paridade |

## Dependências internas principais

- `com.brasfoot.v2028.*` → `a.*`, `d.*`, `components.*`, `est.*`.
- `a.*` contém as entidades centrais e chama `GfxCore`, criando dependência bidirecional domínio↔UI/utilitário.
- `d.*` representa ligas/competições e depende das entidades `a.*`.
- `components.*` contém algoritmos auxiliares, UI e partes do motor; várias classes dependem diretamente de `GfxCore` e `a.t/a.ac/a.p`.
- `c.a` mantém o singleton global da carreira/opções; grande parte do app o acessa diretamente.
- `c.b` prepara e reconstrói referências do grafo antes/depois do save.

## Risco arquitetural

A dependência circular `GfxCore ↔ entidades ↔ components` é o principal risco de migração. A estratégia futura deve introduzir adapters/facades e testes de paridade antes de quebrar esse ciclo.