# PHASE2_STATUS — Base compilável + Harness de Compatibilidade

## Estado

**Implementação técnica: concluída no escopo disponível.**

**Gate final Android (`assembleDebug`): PENDENTE DE EXECUÇÃO em ambiente com Android SDK + acesso aos repositórios.**

A Fase 2 não deve ser promovida como 100% validada até esse comando passar. Nenhum erro de build conhecido foi escondido; o impedimento desta execução é de ambiente, não um teste vermelho conhecido.

## Entregas realizadas

- [x] branch dedicada `phase2/compatibility-harness`, derivada da Fase 1;
- [x] `main` intacta;
- [x] baseline AGP/Kotlin/Compose/SDK alinhado;
- [x] Gradle 9.5.0 fixado com SHA-256 oficial;
- [x] bootstrap `gradlew` / `gradlew.bat` auditável em texto;
- [x] shell Compose separada em pacote `ui`;
- [x] pacotes `foundation`, `legacy/compatibility`, `data`, `domain`, `ui` e testes;
- [x] shells Java Serialization exatas para `e.t` / `e.g`;
- [x] shell de metadado `est.InfoArquivoSalvoType`;
- [x] leitor read-only de `.ban`;
- [x] leitor estrutural de `.ai21` preparado;
- [x] snapshots imutáveis, sem reaproveitar o modelo legado como domínio futuro;
- [x] fingerprint determinístico de dados legados;
- [x] abstração de RNG com seed + contador de draws para futuras comparações;
- [x] catálogo de `a.p`, `a.ac`, `a.t`, `d.q`, `a.b` para caracterização;
- [x] fixture `.ban` real, imutável e rastreável;
- [x] prova local de desserialização do `.ban` real;
- [x] código JVM do harness compilado com `javac` + `kotlinc`;
- [x] nenhuma mudança em jogadores, atributos, ratings, elencos, clubes, competições ou formatos;
- [x] nenhum GitHub Actions consumido.

## Prova `.ban`

Arquivo original: `12deoctubre_par.ban`

SHA-256: `7f386a66e3e87042695b6dfaf23f2bc53143cfe8fa35b91a95ccd5ad060e85a7`

Leitura comprovada:

- `12 de Octubre`;
- país legado `150`;
- 20 jogadores;
- 0 juniores;
- primeiro jogador `Mauro Cardozo`, 38 anos, posição `0`.

Fingerprint canônico:

`9b0d1878744ce2d64a99db8a4103ba18e8f0286706ec4e30142cd585011d79a6`

## Saves

O leitor de metadado `.ai21` está preparado conforme a identidade serial confirmada na Fase 1. O conjunto fornecido não contém uma carreira real `.ai21 + .s21/.s121`, então o load completo de save **não foi declarado compatível**.

O grafo de carreira Kryo permanece fora de qualquer migração para Room até existir uma fixture real e um teste que prove reconstrução correta das referências transitórias.

## Build Android — pendência objetiva

O ambiente desta execução tem JDK e Kotlin compiler, mas não tem Android SDK/Gradle instalado e bloqueia download externo no container. Para preservar a cota do usuário, GitHub Actions não foi usado como atalho.

Gate final a executar em ambiente Android:

```bash
./gradlew help
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
```

Somente após os três comandos passarem a Fase 2 pode receber status **100% VALIDADA**.

## Recomendação para a Fase 3

A Fase 3 deve ser **Persistência/Modelo de Dados Legado → Camada Moderna Versionada**, mas apenas depois do gate de build da Fase 2.

Ordem recomendada:

1. validar `assembleDebug` e testes unitários;
2. obter/criar de forma controlada uma fixture de save real do aplicativo legado;
3. provar leitura de `.ai21` e `.s21/.s121` sem mutação;
4. capturar snapshots de `a.b`, `a.p`, `a.ac`, `a.t` e `d.q` após reconstrução pós-load;
5. definir DTOs de migração versionados;
6. só então introduzir persistência moderna, inicialmente atrás de um adapter/feature boundary;
7. manter o formato legado como fonte de importação e nunca sobrescrever o save antigo durante validação.
