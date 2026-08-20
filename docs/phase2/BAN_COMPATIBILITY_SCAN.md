# BAN_COMPATIBILITY_SCAN — Fase 2

## Varredura integral executada

A camada `LegacySerialization.readBan()` foi compilada localmente com `javac` + `kotlinc` e executada, em modo estritamente read-only, contra **todos os 2.689 arquivos `.ban`** presentes em `resources/assets/teams` do ZIP decompilado fornecido.

Resultado:

```text
files=2689
players=66003
juniors=13098
failures=0
```

Ou seja, os shells de serialização `e.t` / `e.g` conseguiram desserializar **2.689/2.689 arquivos** sem `InvalidClassException`, `ClassNotFoundException`, erro de stream ou erro de mapeamento.

A varredura apenas abriu cada stream, converteu para snapshot imutável e contabilizou as listas. Nenhum `.ban` foi regravado, transformado ou substituído.

## Fixture versionada

Para evitar versionar milhares de arquivos esportivos no novo repositório, apenas uma fixture mínima e rastreável foi incorporada aos testes:

`12deoctubre_par.ban`

SHA-256 do binário legado original:

`7f386a66e3e87042695b6dfaf23f2bc53143cfe8fa35b91a95ccd5ad060e85a7`

A fixture está armazenada em Base64 para manter o commit textual e é decodificada somente em memória no teste.

## Conclusão

A compatibilidade estrutural de leitura do formato `.ban` está **comprovada para 100% do corpus fornecido na Fase 1**. Isso não equivale a validar todas as regras de negócio dos jogadores/clubes; comprova especificamente a leitura fiel da serialização-base `e.t/e.g`.
