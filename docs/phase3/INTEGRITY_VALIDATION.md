# Validação de Integridade

## Gate final executado

GitHub Actions run `32386187551`, job `96484566790`, sobre o código `e39910cbc50fccae848fe3ff408407ae929b0b7b`:

- `./gradlew help --no-daemon`: PASS
- `./gradlew :app:testDebugUnitTest --no-daemon --stacktrace`: PASS
- `./gradlew :app:assembleDebug --no-daemon --stacktrace`: PASS
- Room schema V1 export/audit: PASS
- testes: 25
- falhas: 0
- erros: 0
- ignorados: 0

O gate regenerou o schema final no commit `1da01d2cabe93447b63414fa7620d9e53f0797d2`.

## Fixture real

Fixture congelada: `app/src/test/resources/legacy/12deoctubre_par.ban.b64`.

Caracterização conhecida:

- SHA-256 dos bytes: `7f386a66e3e87042695b6dfaf23f2bc53143cfe8fa35b91a95ccd5ad060e85a7`
- clube: `12 de Octubre`
- country legacy: `150`
- seniors: `20`
- juniors: `0`
- primeiro jogador: `Mauro Cardozo`, idade `38`, posição `0`

A suíte final comprova `Legacy -> V1 -> Room -> V1`, igualdade do fingerprint antes/depois, idempotência, ausência de duplicação, corrupção detectável, rollback transacional, concorrência e reset/reimport determinístico.

## Corpus completo — evidência previamente caracterizada

O corpus completo não foi reimportado no runner final porque não estava disponível como fixture integral naquele ambiente. A caracterização anterior preservada é:

- fontes `.ban`: 2.689
- seniors: 66.003
- juniors: 13.098
- total jogadores: 79.101
- falhas de parsing: 0
- club IDs duplicados: 0
- player IDs duplicados: 0
- manifesto SHA-256 ordenado: `f676890f3b11a5b20a774c7865e354a837bf48ee311c5c12e89ae78852b93ef0`

Esses valores não são apresentados como uma nova execução do gate final.

## Data freeze

A revisão do diff contra `phase2/compatibility-harness` não encontrou alteração factual em jogadores, atributos, elencos, clubes, competições ou regras. A alteração em `e/t.java` apenas expõe getters para campos de cor já existentes no objeto serializado.
