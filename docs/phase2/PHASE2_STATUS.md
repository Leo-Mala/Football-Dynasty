# PHASE2_STATUS — Base Android + Harness de Compatibilidade

## Estado

**Implementação da camada de compatibilidade: concluída no escopo comprovável.**

**Fase 2 completa/100% validada: NÃO AINDA.**

Restam dois gates objetivos que não serão mascarados:

1. gerar/versionar o `gradle-wrapper.jar` oficial a partir do Gradle 9.5.0, substituindo o bootstrap textual temporário;
2. executar com sucesso `testDebugUnitTest` + `assembleDebug` em ambiente com Android SDK 37 e acesso aos repositórios.

Nenhum erro conhecido foi ignorado. O ambiente de análise atual não possui Android SDK/Gradle instalado e bloqueia download externo no container; por isso não existe evidência honesta para marcar o build Android como verde.

## Entregas realizadas

- [x] branch dedicada `phase2/compatibility-harness`, derivada da Fase 1;
- [x] `main` intacta;
- [x] baseline AGP/Kotlin/Compose/SDK alinhado;
- [x] Gradle 9.5.0 fixado com SHA-256 oficial;
- [x] bootstrap temporário `gradlew` / `gradlew.bat` auditável em texto;
- [x] delegação do bootstrap testada localmente com cache Gradle simulado;
- [ ] `gradle-wrapper.jar` oficial versionado e validado;
- [x] shell Compose separada em pacote `ui`;
- [x] pacotes `foundation`, `legacy/compatibility`, `data`, `domain`, `ui` e testes;
- [x] shells Java Serialization exatas para `e.t` / `e.g`;
- [x] shell de metadado `est.InfoArquivoSalvoType`;
- [x] leitor read-only de `.ban`;
- [x] `LegacyFormatProbe` para `.ban`, `.bcf`, `.ai21`, `.s21/.s121` e `.sbck`;
- [x] `LegacySaveReader` read-only com carreira deliberadamente bloqueada sem fixture real;
- [x] `LegacyDataGateway` isolando o restante do app dos shells serializados;
- [x] snapshots imutáveis, sem reaproveitar o modelo legado como domínio futuro;
- [x] fingerprint determinístico de dados legados;
- [x] abstração de RNG com seed + contador de draws;
- [x] catálogo de `a.p`, `a.ac`, `a.t`, `d.q`, `a.b`;
- [x] fixture `.ban` real, imutável e rastreável;
- [x] SHA-256 byte-for-byte + fingerprint semântico protegidos por teste;
- [x] prova local de desserialização do `.ban` real;
- [x] varredura read-only de 2.689/2.689 `.ban` com 0 falhas;
- [x] código JVM do harness compilado com `javac` + `kotlinc`;
- [x] smoke test JVM reproduzível em `tools/phase2_jvm_smoke.sh`;
- [x] documentação obrigatória de build, compatibilidade, saves, fixtures, determinismo e integridade;
- [x] workflow Android exclusivamente manual (`workflow_dispatch`), sem gatilho por push/PR;
- [x] nenhuma mudança em jogadores, atributos, ratings, elencos, clubes, competições ou formatos;
- [x] nenhum GitHub Actions consumido até este checkpoint.

## Provas locais

### Fixture `.ban`

Arquivo original: `12deoctubre_par.ban`

SHA-256:

`7f386a66e3e87042695b6dfaf23f2bc53143cfe8fa35b91a95ccd5ad060e85a7`

Leitura comprovada:

- `12 de Octubre`;
- país legado `150`;
- 20 jogadores;
- 0 juniores;
- primeiro jogador `Mauro Cardozo`, 38 anos, posição `0`.

Fingerprint canônico:

`9b0d1878744ce2d64a99db8a4103ba18e8f0286706ec4e30142cd585011d79a6`

### Corpus `.ban`

```text
files=2689
players=66003
juniors=13098
failures=0
```

SHA-256 do manifesto ordenado `hash + caminho`:

`f676890f3b11a5b20a774c7865e354a837bf48ee311c5c12e89ae78852b93ef0`

### Smoke JVM

A camada Java/Kotlin de compatibilidade compilou fora do Android e executou com sucesso a prova de:

- hash da fixture;
- Java Serialization `.ban`;
- snapshot/fingerprint;
- RNG determinístico;
- catálogo das entidades centrais;
- probe de formato;
- bloqueio seguro de leitura do grafo de carreira sem fixture real.

## Saves

O leitor de metadado `.ai21` está preparado conforme a identidade serial confirmada na Fase 1. O conjunto fornecido não contém uma carreira real `.ai21 + .s21/.s121`, então o load completo de save **não foi declarado compatível**.

O grafo de carreira Kryo permanece fora de qualquer migração para Room até existir uma fixture real e um teste que prove reconstrução correta das referências transitórias.

Consulte `LEGACY_SAVE_STATUS.md` para a escala de suporte.

## Build Android — pendência objetiva

O workflow `.github/workflows/phase2-manual-validation.yml` não roda automaticamente e, portanto, não consome minutos em pushes.

Gate final:

```bash
bash gradlew help
bash gradlew :app:testDebugUnitTest
bash gradlew :app:assembleDebug
```

Após o Gradle oficial estar disponível, gerar também o wrapper oficial e versionar o `gradle-wrapper.jar`.

Somente depois de wrapper + testes + `assembleDebug` verdes a Fase 2 recebe status **100% VALIDADA**.

## Recomendação para a Fase 3

A Fase 3 deve ser **Persistência/Modelo de Dados Legado → Camada Moderna Versionada**, mas apenas depois do gate de build da Fase 2.

Ordem recomendada:

1. fechar os dois gates de build/wrapper;
2. obter/criar de forma controlada uma fixture de save real do aplicativo legado;
3. provar leitura de `.ai21` e `.s21/.s121` sem mutação;
4. capturar snapshots de `a.b`, `a.p`, `a.ac`, `a.t` e `d.q` após reconstrução pós-load;
5. definir DTOs de migração versionados;
6. só então introduzir persistência moderna atrás de um adapter/feature boundary;
7. manter o formato legado como fonte de importação e nunca sobrescrever o save antigo durante validação.
