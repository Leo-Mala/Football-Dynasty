# PHASE2_STATUS — Base Android + Harness de Compatibilidade

## Estado

**FASE 2 = 100% VALIDADA.**

A base Android e o harness de compatibilidade foram implementados e os dois gates finais foram comprovados em runner GitHub-hosted com JDK 17 e Android SDK 37.

Evidência final:

- commit que corrigiu a resolução dos pacotes Android 17/API 37: `a05583ef542366d374d6fae6787a87a75169287d`;
- workflow executou `Gradle help`, `:app:testDebugUnitTest` e `:app:assembleDebug` antes da etapa de publicação;
- wrapper oficial Gradle 9.5.0 foi publicado somente depois dos gates verdes;
- commit de publicação do wrapper: `e3558d7ce9875e19af4d905a40e98afaa99e705c` (`build: install official Gradle 9.5 wrapper`);
- `gradle/wrapper/gradle-wrapper.jar`, `gradlew`, `gradlew.bat` e `gradle-wrapper.properties` estão versionados;
- SHA-256 esperado do wrapper JAR foi validado no workflow: `497c8c2a7e5031f6aa847f88104aa80a93532ec32ee17bdb8d1d2f67a194a9c7`;
- distribuição Gradle 9.5.0 protegida por SHA-256: `553c78f50dafcd54d65b9a444649057857469edf836431389695608536d6b746`.

A primeira execução pública chegou ao runner, mas falhou antes do Gradle porque `sdkmanager` não publicava o pacote API 37 sob o caminho estável esperado. O workflow foi corrigido para resolver dinamicamente no catálogo real do `sdkmanager --list --channel=3` o pacote Android 17/API 37 e o Build Tools 37.x correspondente. Não houve downgrade de `compileSdk` para mascarar a falha.

## Entregas realizadas

- [x] branch dedicada `phase2/compatibility-harness`, derivada da Fase 1;
- [x] `main` intacta;
- [x] baseline AGP/Kotlin/Compose/SDK alinhado;
- [x] Gradle 9.5.0 fixado com SHA-256 oficial;
- [x] `gradle-wrapper.jar` oficial versionado e validado;
- [x] scripts oficiais `gradlew` e `gradlew.bat` gerados pelo Gradle;
- [x] `gradle-wrapper.properties` oficial com checksum da distribuição;
- [x] `Gradle help` verde;
- [x] `:app:testDebugUnitTest` verde;
- [x] `:app:assembleDebug` verde;
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
- [x] nenhuma mudança em jogadores, atributos, ratings, elencos, clubes, competições ou formatos.

## Provas de compatibilidade legada

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

O grafo de carreira Kryo permanece fora de qualquer migração definitiva até existir uma fixture real e um teste que prove reconstrução correta das referências transitórias.

Consulte `LEGACY_SAVE_STATUS.md` para a escala de suporte.

## Gate Android final

Resultado comprovado do fluxo final:

```text
JDK 17: PASS
Android SDK 37/API 37: PASS
Gradle 9.5.0: PASS
Gradle Wrapper oficial: PASS
./gradlew help: PASS
./gradlew :app:testDebugUnitTest: PASS
./gradlew :app:assembleDebug: PASS
wrapper publication: PASS
```

O commit `e3558d7ce9875e19af4d905a40e98afaa99e705c` é a evidência persistente da publicação do wrapper que ocorre somente após todos os comandos acima terem concluído com sucesso.

## Congelamento factual

A revisão da Fase 2 não introduziu atualização factual de futebol. Permanecem congelados jogadores, nomes, atributos, ratings, posições, elencos, clubes, países, divisões, competições, formatos, estruturas e regras esportivas.

## Recomendação para a Fase 3

A precondição técnica da Fase 3 está satisfeita. A próxima etapa é **Persistência Moderna Versionada + Importação Legada Segura**, mantendo a cadeia explícita:

`Legacy Format → Legacy Compatibility Model → Versioned Migration DTO → Modern Domain Model → Persistence Entity`

A compatibilidade de carreira legada completa continua condicionada a uma fixture real `.ai21 + .s21/.s121`; sua ausência não autoriza inventar o grafo Kryo nem declarar suporte que ainda não foi provado.
