# ANNUAL SUBSYSTEMS — Fase 6

## Fonte oficial

- corpus: `Brasfoot.apk_Decompiler.com.zip`;
- SHA-256: `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`;
- package: `com.brasfoot.v2020`;
- versionCode: `202632`.

A investigação desta fase usa exclusivamente Java/SMALI do corpus oficial. Nenhum dado esportivo externo foi introduzido.

## Orquestrador profundo `best.a0.d()`

Java + SMALI confirmam a ordem:

`g() -> h() -> f() -> c() -> i()`.

A Fase 6 fecha os efeitos necessários desse caminho no nível seguro permitido pelo domínio moderno:

- `g()/h()` encaminham clubes para `b(...) -> j(...)`;
- `j(...)` seleciona excesso posicional, filtra jogador e roteia tentativa via `best.f`;
- `f()` executa manutenção mínima de elenco por `c0.n()`;
- `c()` executa manutenção determinística de referências `best.x/c0` sem RNG;
- `i()` seleciona jogadores por filtros + gate aleatório e tenta realocação por `best.f` modo 2.

## `best.a0.j(...)`

O Java está truncado; o SMALI é a fonte executável.

### Posição alvo

`D0(true)` fornece contagens por posição. A lista de posições `0..4` é embaralhada antes da escolha.

Passagem `p2=true`:

- primeira tentativa: `{4,5,5,10,8}` com comparação `>=`;
- fallback: `{3,4,4,8,6}` com comparação `>=`.

Passagem `p2=false`:

- `{3,4,4,6,4}` com comparação estrita `>`.

### SITE_1

Depois da escolha posicional, o método executa `nextInt(100) > 10` antes de montar a lista de jogadores. O draw ocorre mesmo quando nenhuma posição foi encontrada.

Um jogador só entra na lista quando:

- `l0()` coincide com a posição escolhida;
- `N0()` é falso;
- se `O0()` é verdadeiro, o resultado do SITE_1 também precisa ser verdadeiro.

A lista resultante é embaralhada e o primeiro item é usado.

### Roteamento posterior

Com `p1=true`:

- modo `best.f = 0`;
- para `O() < 40`, SITE_2 (`>90`) pode trocar `n(false)` por `o(false)`;
- para `O() >= 40`, usa `n(false)` sem esse draw.

Com `p1=false`:

- jogador `O0()` primeiro passa pelo SITE_3 (`>30`);
- se passar: modo `1` + `n(false)`;
- caso contrário, exatamente uma faixa de `O()` é avaliada pelos sites 4..8:
  - `>=90`: `>30`;
  - `80..89`: `>35`;
  - `70..79`: `>45`;
  - `60..69`: `>75`;
  - `<60`: `>95`;
- quando a faixa passa: modo `1` + `n(false)`;
- quando falha: modo `0` + `o(false)`.

Quando `best.f.g()` encontra destino, o caller executa `o.T1(destino, A0(), false, false, false)`.

Implementação moderna: `LegacyAnnualSelectionRules` + `LegacyAnnualRandomRules`.

## `best.c0.Z0(...)`, `a1(...)` e `M1()`

Java + SMALI permitem fechar os predicados usados por `best.f`.

### `M1()`

`Z().size() >= 30`.

### `a1(...)`

- rejeita roster `>=30`;
- exige apenas o mínimo de `O()`:
  - quando `R0()`: `j0.f4269d0[O()] = {1,40,30,20,5}`;
  - caso contrário: `j0.f4272e0[p0()] = {1,10,20,40,50,55}`.
- o parâmetro booleano não altera o corpo dessa versão do APK.

### `Z0(...)`

Além do cap de 30 e do mínimo acima:

- aplica máximo `j0.f4275f0[p0()] = {20,30,45,85,100,100}`;
- quando o booleano é verdadeiro, aplica caps posicionais a partir de `D0(false)`:
  - posição 0: rejeita quando contagem existente `>3`;
  - posição 1: `>5`;
  - posição 2: `>5`;
  - posição 3: `>10`;
  - posição 4: `>5`.

Implementação moderna: `LegacyAnnualSelectionRules.bestC0Z0/bestC0A1/bestC0M1`.

## `best.f.n(...)` — short-circuit de RNG

O Java e o SMALI confirmam que a condição começa por uma ternária:

`(subject.O0() && current.Q0()) ? false : ... nextInt(100) <= 60`.

Consequências exatas:

- `subject.O0()==true && current.Q0()==true` escolhe diretamente a rota alternativa e **não consome RNG**;
- `subject.O() <= 30` escolhe a rota primária sem RNG;
- `current.Q0()==false` escolhe a rota primária sem RNG;
- somente quando `!subject.O0() && subject.O()>30 && current.Q0()` ocorre `nextInt(100) <= 60`.

Isso é importante para save/reopen determinístico: consumir um draw no branch `O0 && Q0` deslocaria toda a sequência futura. A regressão está em `LegacyAnnualRandomRulesTest`.

## `best.f.q(...)` e `p()`

### Range de `q(...)`

Base:

- `min = current.O()-1`;
- `max = current.O()+1`;
- quando `current.O()==1`, `min=1`.

Modo 1:

- se `current.J()!=0 || current.p0()<4 || subject.O()<40`: `1..2`;
- senão: `1..1`.

Para qualquer `subject.O() <= 20`, o loop por grupo sobrescreve o intervalo para:

- `min = 0`;
- `max = group.A0()`.

Candidato entra somente se:

- `O()` dentro do intervalo;
- não é o current;
- `Q0()==false`;
- roster `<30`.

A lista é embaralhada e a seleção final usa `a1`, `M1+p0>=4` ou `Z0`, conforme modo.

### Fallback `p()`

Pool global apenas com:

- `R0()==false`;
- `Q0()==false`;
- roster `<30`.

Depois de shuffle, escolhe o primeiro que passa `Z0(...)`.

## Manutenção mínima de elenco — `a0.f() -> c0.n() -> f.e()/h()`

`a0.f()` chama `c0.n()` para cada entrada `E0()` com `Q0()==false`.

`c0.n()` calcula `D0(false)` uma vez e executa no máximo uma tentativa por posição abaixo de:

`{2,3,3,5,3}` para posições `0..4`.

O overall solicitado é o mesmo mínimo usado pelos predicados de seleção (`j0.f4269d0` ou `j0.f4272e0`).

### Clubes doadores em `f.e()`

Para alvo `R0()==true`, a busca usa um pool já escopado por `T0(j0())` e aceita `p0` entre `target.p0()-2` (clamp 0) e `target.p0()+1`.

Para alvo `R0()==false`, usa `E0()` e exige:

- `Q0()==false`;
- não ser o alvo;
- `J()` igual ao alvo;
- `p0` entre `target.p0()-1` (clamp 0) e `target.p0()+1`.

O pool é embaralhado.

### Jogador doador em `f.h()`

Para cada clube, o roster é copiado e embaralhado. O primeiro jogador elegível precisa:

- posição igual à solicitada;
- `O()` em `requestedOverall ± 5`, limitado a `5..100`;
- `O0()==false`;
- `W0()==false`.

O clube só pode ceder quando a quantidade total daquela posição é pelo menos:

`{3,4,4,6,4}` para posições `0..4`.

Encontrado um jogador, `T1(target, A0(), false, false, false)` efetiva a movimentação e seus efeitos associados.

Sem candidato, o legado chama `best.p.d(...)` para gerar um fallback e o insere via `best.t.e(...)`. A reconstrução moderna **não cria um jogador factual arbitrário**: a geração de fallback deve ser caracterizada separadamente antes de ser materializada.

Implementação moderna segura: `LegacyAnnualSquadFloorRules` congela mínimos, janelas e filtros, sem inventar os atributos de `best.p.d()`.

## `best.a0.i()` — SMALI_REQUIRED

O método Java está truncado; o SMALI confirma:

### Clube elegível

- sempre exige `Q0()==false`;
- se `J()!=0`, exige `p0()<5`;
- se `J()==0`, exige `p0()<4`.

### Jogador elegível

Short-circuit na ordem:

1. `O() > 50`;
2. `W() < 31`;
3. `O0()==true`;
4. `nextInt(100) > 25`.

Falha nos três primeiros filtros não consome RNG.

Cada jogador coletado é processado com `best.f` modo `2`, tentando:

1. `n(false)`;
2. se nenhum destino foi encontrado, nova instância e `o(false)`;
3. se houver destino, `T1(destino, A0(), false, false, false)`.

Implementação moderna: `LegacyAnnualA0IRules` + regras compartilhadas de `best.f`.

## `best.a0.c()`

Java + SMALI coincidem integralmente e não usam aleatoriedade:

- percorre `K0()`;
- quando `L0()` existe e `L0().Q0()==true`, chama `L0().h1()`;
- quando `L0()` existe e `Q0()==false`, chama `x.s0(false)`.

O efeito é determinístico e não exige novo estado Room para ser caracterizado.

## Métodos truncados de `best.f`

### `best.f.e(...)`

É necessário e foi recuperado via SMALI, pois é chamado por `c0.n()` no caminho anual. Seus filtros e fallback estão descritos acima.

### `best.f.d(...)`

A busca de callers no Java e no bytecode do corpus não encontrou `invoke-* Lbest/f;->d(Lbest/o;Lbest/c0;I)I` fora da própria definição. Portanto não há evidência de alcançabilidade pelo ciclo anual desta versão.

Classificação: `DECOMPILED_STUB_NOT_REQUIRED_FOR_PHASE6_ANNUAL_PATH`.

Isso não afirma que o método seja irrelevante para todo o aplicativo; apenas impede expandir a Fase 6 por um método sem caller comprovado no escopo anual.

## Persistência

As regras adicionadas são puras e deriváveis. O estado RNG necessário já está persistido em `career_core_state` V2. Nenhum novo campo persistente foi demonstrado como necessário.

**Room permanece V2.**

## Limite funcional seguro

A fase possui regras executáveis para os predicados, limites, decisões e planos estruturais anuais comprovados. A aplicação integral de `T1(...)` sobre modelos ricos e a geração procedural `best.p.d(...)` não são inventadas nesta etapa porque o modelo moderno atual ainda não representa, com evidência suficiente, todos os campos necessários desses efeitos.

Essa fronteira é deliberada: preservar comportamento comprovado é preferível a introduzir uma simulação esportiva não demonstrada pelo corpus.
