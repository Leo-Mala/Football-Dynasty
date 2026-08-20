# BUILD_BASELINE — Fase 2

## Baseline fixado

- Android Gradle Plugin: **9.3.0**
- Gradle: **9.5.0**
- Kotlin / Compose Compiler plugin: **2.3.21**
- Compose BOM: **2026.08.00**
- `compileSdk`: **37**
- `targetSdk`: **36**
- `minSdk`: **26**
- Java bytecode level: **17**

A escolha segue a compatibilidade publicada pelo Android Developers para AGP 9.3 / API 37 e a exigência do Compose 1.12 por `compileSdk 37` + AGP 9. O projeto usa o Kotlin integrado do AGP 9 e não aplica `org.jetbrains.kotlin.android`.

## Wrapper/bootstrap

O repositório original não possuía `gradle-wrapper.jar`. Como a interface de publicação usada nesta reconstrução não permite inserir com segurança o binário oficial do wrapper, a Fase 2 adiciona um bootstrap temporário e auditável em texto:

- `gradlew`
- `gradlew.bat`
- `gradle/wrapper/gradle-wrapper.properties`
- `gradle/wrapper-bootstrap/GradleBootstrap.java`

O bootstrap requer JDK 17+, baixa exclusivamente o Gradle 9.5.0, valida o SHA-256 oficial `553c78f50dafcd54d65b9a444649057857469edf836431389695608536d6b746`, extrai sob `GRADLE_USER_HOME` e delega os argumentos ao Gradle real.

A delegação do bootstrap foi testada localmente com uma instalação Gradle simulada em cache; os argumentos `help --no-daemon` chegaram ao executável delegado corretamente.

### Gate do wrapper oficial

A presença do bootstrap **não deve ser confundida com o wrapper binário oficial**. Assim que houver Gradle disponível em uma máquina com acesso à internet, executar o task `wrapper` duas vezes conforme a recomendação oficial do Gradle para gerar/atualizar:

- `gradle/wrapper/gradle-wrapper.jar`
- scripts oficiais `gradlew` / `gradlew.bat`
- metadata final do wrapper.

Depois disso, versionar o JAR oficial e confirmar seu checksum antes de remover `gradle/wrapper-bootstrap/`.

## Verificação nesta execução

O ambiente de análise disponibilizou JDK 21 e `kotlinc`, porém **não disponibilizou Android SDK, Gradle instalado nem acesso DNS externo no container**. Por isso `assembleDebug` não pôde ser executado localmente sem consumir GitHub Actions.

Para respeitar a cota mensal, nenhum GitHub Actions foi executado.

O código puro JVM do harness de compatibilidade foi compilado localmente com `javac` + `kotlinc` e executado contra um `.ban` real do APK decompilado com sucesso. O repositório contém `tools/phase2_jvm_smoke.sh` para repetir essa prova sem Android SDK.

## Validação Android econômica

Existe apenas um workflow manual:

`.github/workflows/phase2-manual-validation.yml`

Ele usa exclusivamente `workflow_dispatch`; não executa por push ou pull request. O workflow instala somente JDK 17 + SDK 37/Build Tools 36.0.0 e roda testes unitários + `assembleDebug` em um único job.

Como arquivos criados pela API podem não preservar bit executável, o workflow usa `bash gradlew` explicitamente.

## Gate de build em máquina Android

Executar:

```bash
bash gradlew help
bash gradlew :app:testDebugUnitTest
bash gradlew :app:assembleDebug
```

A Fase 2 não interpreta ausência desse ambiente como evidência de build verde. O gate `assembleDebug` permanece explicitamente **pendente de execução**, não mascarado como sucesso.
