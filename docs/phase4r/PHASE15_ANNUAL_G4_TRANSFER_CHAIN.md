# Fase 15 — cadeia anual `g4` / transferência automática

Status: **CHARACTERIZED / IMPLEMENTATION BLOCKED ON RAW RNG OWNERSHIP**

Corpus oficial: `Brasfoot.apk_Decompiler.com.zip` — SHA-256 `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465` — package `com.brasfoot.v2020` — versionCode `202632`.

Autoridade: SMALI executável prevalece sobre Java decompilado.

## 1. Reachability

A cadeia comprovada é:

`ActivityFimAno.e()` → `best.n.n()` → `best.b.d()` → `best.n.m()` → raw `new Random().nextInt(100)` → se draw `> 50`, `best.b.g4()` → para cada `best.o` com `w()==true`, `components.n3(player, player.z0(), true, true)`.

O quarto argumento `true` de `n3` pula a contagem alternativa de clubes. `n3.n()` aceita a continuação somente quando `requestedValue <= player.A0() + round(player.A0()*0.1)`.

## 2. `components.n3` no caminho exato de `g4()`

Quando o limite acima passa, `n3.g()` instancia exatamente:

`best.f(player, requestedValue, true, false, 0)`

Depois chama `f.n(false)`, lê `f.g()` (clube escolhido) e `f.k()` (valor). Se o clube resultante não for nulo, `n3.a()` chama:

`player.T1(targetClub, selectedValue, true, false, false)`.

Portanto `g4()` é gameplay substantivo e pode efetivamente transferir jogador entre clubes.

## 3. RNG adicional dentro de `best.f`

O efeito do draw externo de `best.n.m()` não fecha a aleatoriedade da cadeia. No construtor `best.f(..., mode=0)` há outro `new java.util.Random().nextInt(100)` alcançável quando o clube atual possui `j0()==29` e o jogador possui `O()>50`; esse draw participa da construção das listas candidatas.

Em seguida `f.n(false)` contém outro `new java.util.Random().nextInt(100)` quando `player.O()>30` e o clube atual é controlado (`Q0()==true`); draw `>60` altera o ramo de seleção.

Além disso, `best.f.p()` e `best.f.q(...)` usam `Collections.shuffle(...)` sem `Random` explícito. No Java/Android legado isso implica aleatoriedade própria da API, também não pertencente à stream stateful já persistida no runtime moderno.

Logo o caminho anual comprovado contém **múltiplas fontes raw/non-stateful de aleatoriedade**:

1. `best.n.m()` — `new Random().nextInt(100)` para decidir se `g4()` roda;
2. `best.f` construtor — draw condicional para composição de candidatos;
3. `best.f.n(false)` — draw condicional para seleção;
4. `Collections.shuffle(...)` em `best.f.p/q` — permutação sem seed explícita.

Não é seguro substituir nenhuma delas pela RNG persistida moderna sem uma decisão de compatibilidade explícita e regressões que congelem consumo/ordem.

## 4. Efeito de `best.o.T1(target, value, true, false, false)`

O SMALI comprova, para os argumentos usados por `n3`:

- marca refresh da tela principal se origem/destino forem o clube controlado;
- muda o clube do jogador via `K1(target)`;
- limpa flags `X` e `Z`;
- como `p4=false` e `p5=false`, entra na rota financeira/contratual normal;
- com `p3=true`, pode calcular encargo adicional sobre o valor conforme `K()` e o calendário;
- se valor `>0`, aplica débito/crédito somente quando os clubes envolvidos são controlados, usando os códigos financeiros executáveis do método;
- cria/renova contrato por 180 dias (`c(180,true)`), executa `Q1()` e limpa referências especiais do clube de origem quando apontam para o jogador;
- remove o jogador da origem (`origin.f1(player)`) e adiciona ao destino (`target.f(player)`), quando origem existe;
- aplica os flags finais condicionais comprovados no SMALI.

Isto confirma que `g4()` pode mutar roster, clube do jogador, contrato e finanças.

## 5. Implicação moderna

Ainda não existe equivalência segura para promover `best.n.m()/g4()` ao runtime moderno. O problema não é apenas uma escolha de RNG: é necessário mapear conjuntamente seleção de clubes, múltiplas fontes raw de aleatoriedade, transfer mutation, contrato, roster ownership e efeitos financeiros.

Room permanece **V14** nesta caracterização. Nenhum novo estado durável foi provado como necessário apenas por esta cadeia; não criar V15, backfill ou default esportivo.

## 6. Próximo passo

1. localizar no runtime moderno estruturas equivalentes de transferência/roster/contrato/finanças e verificar se já representam integralmente os efeitos de `T1`;
2. auditar `j2`, `F2`, `F()` e o restante do roteamento de `best.n.m()`;
3. caracterizar as filas `I/J` contra estruturas modernas existentes;
4. somente depois decidir a política de compatibilidade das fontes raw de RNG e implementar com regressões determinísticas/rollback/reopen.

Nenhuma regra esportiva externa foi usada.