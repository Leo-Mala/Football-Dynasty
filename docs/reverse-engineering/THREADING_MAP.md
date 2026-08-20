# THREADING_MAP — Fase 1

Foram detectadas referências explícitas a `Thread`, `Handler` ou `AsyncTask` em **16 classes internas**.

- `com/brasfoot/v2028/ActivityConviteSelecao.java`: AsyncTask
- `com/brasfoot/v2028/ActivityEscala.java`: AsyncTask
- `com/brasfoot/v2028/ActivityEscolhaTimes.java`: AsyncTask
- `com/brasfoot/v2028/ActivityFimAno.java`: AsyncTask
- `com/brasfoot/v2028/ActivityJogo.java`: Handler
- `com/brasfoot/v2028/ActivityLoad.java`: AsyncTask, Handler, Thread
- `com/brasfoot/v2028/ActivityPenalty.java`: Handler
- `com/brasfoot/v2028/ActivitySave.java`: AsyncTask, Thread
- `com/brasfoot/v2028/ActivitySub.java`: Thread
- `com/brasfoot/v2028/ActivityTeste.java`: Thread
- `com/brasfoot/v2028/CrashLogger.java`: Handler, Thread
- `com/brasfoot/v2028/GfxCore.java`: Handler
- `com/brasfoot/v2028/MainActivity.java`: AsyncTask
- `com/brasfoot/v2028/RcSimTick.java`: Handler
- `com/brasfoot/v2028/RcSortTick.java`: Handler
- `com/brasfoot/v2028/StartOptions.java`: AsyncTask

## Padrões observados

- Telas de save/load criam `Thread` manual e usam `runOnUiThread` para atualização de progresso/Toast.
- Partida e penalidades usam threads/timers para progressão temporal e UI.
- Inicialização/cópia de assets ocorre em código Activity legado e pode fazer I/O pesado.
- Estado global não é isolado por thread; a migração não pode simplesmente paralelizar essas rotinas.

## Mapeamento futuro

- I/O: `Dispatchers.IO`.
- simulação pesada: `Dispatchers.Default` com estado do jogo serializado/isolado.
- UI: `viewModelScope` + `StateFlow`.
- timers de partida: coroutines estruturadas em vez de threads soltas.

**Regra:** não alterar ordem dos side effects durante a migração. Primeiro reproduzir comportamento; depois otimizar concorrência.