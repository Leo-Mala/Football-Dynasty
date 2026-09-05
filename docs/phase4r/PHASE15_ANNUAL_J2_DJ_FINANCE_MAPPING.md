# Fase 15 — mapping do comando anual `dJ` / `best.a.r()`

Status: **IMPLEMENTED_AND_CERTIFIED on baseline `22024a53954b177c18dea045365f700ab7fcc129`**

Corpus oficial: `Brasfoot.apk_Decompiler.com.zip` — SHA-256 `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465` — package `com.brasfoot.v2020` — versionCode `202632`.

## 1. Call path legado

O dispatcher anual já caracterizado executa:

`best.b.j2(1)` → `best.a.J(1)` → comando `"dJ"` → `best.a.r()`.

O SMALI congelado em `PHASE15_ANNUAL_J2_M0_EVIDENCE.md` prova que `best.a.r()` percorre os clubes e, quando o clube possui objeto financeiro `T()` e `T().o() > 0`, chama:

`club.D(value, 4)`

O raw expense code `4` é a categoria comprovada de borrowing charge no ledger legado.

## 2. Equivalente moderno já existente

O runtime moderno já contém `CareerFinanceBorrowingStore.applyMonthlyBorrowingCharges(careerId)` explicitamente documentado como boundary do monthly `"dJ"` borrowing-charge pass de `best.a.r()`.

O método:

1. obtém somente os clubes com runtime financeiro já materializado;
2. percorre `clubDao().all()` em source order imutável;
3. pula clubes sem estado financeiro materializado, preservando o null/absence semantics do legado;
4. aplica `LegacyFinanceRuntimeRule.applyMonthlyBorrowingCharge(before)`;
5. persiste cada mutação via `CareerManagerRuntimeStore.commitFinanceState(...)` dentro de uma única transação Room;
6. permite caixa negativo exatamente como `best.a.r()`.

Não há RNG neste caminho.

## 3. Regressão real existente

`CareerFinanceBorrowingStoreTest.monthly borrowing pass follows immutable club source order and can make cash negative` prova:

- dois clubes são aplicados em source order, não em ordem de inserção arbitrária;
- charge `30_000` reduz caixa `40_000 → 10_000` e entra em `borrowingChargeExpense`;
- charge `15_000` reduz caixa `10_000 → -5_000`, portanto não há clamp moderno inventado;
- a categoria financeira continua sendo borrowing charge, não miscellaneous.

O mesmo store também possui regressão file-backed de reopen para o lifecycle de borrowing, e a persistência usa o mesmo runtime financeiro durável.

## 4. Certificação

O store e essa regressão já estavam presentes no exact baseline:

`22024a53954b177c18dea045365f700ab7fcc129`

Esse SHA foi certificado conjuntamente por:

- Phase 7 Validation #885 — SUCCESS;
- Phase 8 Validation #697 — SUCCESS;
- Phase 8 Final Certification #508 — SUCCESS.

Portanto o callee `dJ → best.a.r()` não deve permanecer como blocker funcional da Fase 15.

## 5. Classificação

- command code `dJ` no `best.a.J(1)` → dispatcher **IMPLEMENTED_NEEDS_REVALIDATION** via `LegacyAnnualJ2CommandRules` até a composição integral de `J(1)`;
- efeito substantivo `best.a.r()` → **IMPLEMENTED_AND_CERTIFIED** via `CareerFinanceBorrowingStore.applyMonthlyBorrowingCharges()`;
- Room/schema → nenhuma alteração necessária; V14 já representa o ledger e caixa exigidos.

## 6. Próximo passo

Remover `r()` da lista de gaps substantivos restantes e continuar a auditoria de `q()`, `s()`, `best.b.p()`, `p()` e `n(boolean)`, preservando a ordem e o `clear()` one-shot do dispatcher anual.
