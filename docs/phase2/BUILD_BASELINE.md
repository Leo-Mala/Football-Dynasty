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

## Wrapper

O repositório original não possuía `gradle-wrapper.jar`. Para não depender de um binário sem origem rastreável, a Fase 2 adiciona:

- `gradlew`
- `gradlew.bat`
- `gradle/wrapper/gradle-wrapper.properties`
- `gradle/wrapper-bootstrap/GradleBootstrap.java`

O bootstrap é texto auditável, requer JDK 17+, baixa exclusivamente o Gradle 9.5.0, valida o SHA-256 oficial `553c78f50dafcd54d65b9a444649057857469edf836431389695608536d6b746`, extrai sob `GRADLE_USER_HOME` e delega os argumentos ao Gradle real.

Quando houver Gradle disponível em uma máquina com acesso à internet, `./gradlew wrapper` pode substituir esse bootstrap pelo wrapper binário oficial gerado pelo próprio Gradle.

## Verificação nesta execução

O ambiente de análise disponibilizou JDK 21 e `kotlinc`, porém **não disponibilizou Android SDK, Gradle instalado nem acesso DNS externo no container**. Por isso `assembleDebug` não pôde ser executado localmente sem consumir GitHub Actions.

Para respeitar a cota mensal, nenhum Actions automático foi criado ou executado.

O código puro JVM do harness de compatibilidade foi compilado localmente com `javac` + `kotlinc` e executado contra um `.ban` real do APK decompilado com sucesso.

## Gate de build em máquina Android

Assim que um ambiente com Android SDK 37 e acesso aos repositórios Maven estiver disponível, executar:

```bash
./gradlew help
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
```

A Fase 2 não interpreta ausência desse ambiente como evidência de build verde. O gate `assembleDebug` permanece explicitamente **pendente de execução**, não mascarado como sucesso.
