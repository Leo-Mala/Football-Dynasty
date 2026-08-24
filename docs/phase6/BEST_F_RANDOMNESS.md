# BEST_F_RANDOMNESS — Fase 6

## Por que `best.f` faz parte do escopo anual

O caminho anual caracterizado é:

`best.b.d()` → `best.a0.d()` → `best.a0.g()/h()` → `best.a0.b(...)` → `best.a0.j(...)` → `new best.f(...)` → `best.f.n(false)` ou `best.f.o(false)`.

Portanto a aleatoriedade dentro de `best.f` é transitivamente alcançável pelo ciclo anual e não pode ser ignorada na reconstrução determinística.

## RNG direto confirmado

Java e SMALI de `best.f` confirmam:

1. no construtor com modo `0`, sob condição envolvendo `j0()==29` e `O()>50`: `new Random().nextInt(100) > 10`;
2. no construtor com modo `1`, no branch equivalente quando flags anteriores não tornam a condição verdadeira: `new Random().nextInt(100) > 10`;
3. em `n(boolean)`, num branch condicionado por flags/valor do objeto: `new Random().nextInt(100) <= 60`.

Os dois sites do construtor representam o mesmo predicado em branches de modo distintos. A projeção moderna usa uma única regra estrutural:

- `LegacyAnnualRandomRules.bestFConstructorGate(RandomSource)` → `nextInt(100) > 10`.

Para `n(boolean)`:

- `LegacyAnnualRandomRules.bestFNGate(RandomSource)` → `nextInt(100) <= 60`.

A projeção não atribui nomes esportivos às flags/objetos obfuscados.

## Aleatoriedade implícita por shuffle

O código legado usa `Collections.shuffle(...)` em `best.f`, incluindo caminhos alcançáveis por `p()` e `q(...)`, que são chamados por `n()/o()`.

Há também `Collections.shuffle(...)` dentro do próprio `best.a0.j(...)`.

Como `Collections.shuffle(List)` usa uma fonte de aleatoriedade implícita que não pode ser salva/restaurada pela carreira moderna, a reconstrução não deve chamá-lo diretamente.

Foi adicionada:

`LegacyAnnualRandomRules.shuffleInPlace(values, RandomSource)`

A implementação usa Fisher–Yates reverso com `RandomSource.nextInt(index + 1)`.

Essa escolha preserva a distribuição uniforme do embaralhamento, permite testes e retomada determinística e evita dependência de seed implícita. Ela **não** declara que a ordem moderna será bit-a-bit idêntica à ordem produzida pelo seed padrão não persistido do APK legado.

## Testes

`LegacyAnnualRandomRulesTest` verifica:

- gates diretos consumindo o número esperado de draws;
- os oito thresholds SMALI de `best.a0.j(...)`;
- shuffle preservando o conjunto de elementos;
- shuffle consumindo `size - 1` draws na implementação moderna;
- repetibilidade de gates + shuffle com seed idêntica;
- retomada exata após `StatefulJavaRandomSource.snapshot()` / `restore()`.

## Estado

A fronteira RNG explícita do caminho `best.a0 -> best.f` está caracterizada suficientemente para impedir reintrodução de `Random()`/`Collections.shuffle()` não determinísticos no domínio moderno.

Ainda falta reconstruir com confiança os **efeitos de negócio** de `best.f` e demais subsistemas anuais. Esses efeitos só devem ser materializados quando entradas, mutações e invariantes estiverem comprovadas por Java/SMALI e testes de caracterização.
