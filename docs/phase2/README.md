# Fase 2 — Base Android + Harness de Compatibilidade

Esta pasta registra somente fatos comprovados da Fase 2. A ausência de uma fixture real de carreira e a ausência de um build Android executável no ambiente de análise são tratadas como gates explícitos, não como sucesso presumido.

## Documentos

- [BUILD_BASELINE.md](BUILD_BASELINE.md) — versões, wrapper/bootstrap e gate de build Android.
- [LEGACY_COMPATIBILITY.md](LEGACY_COMPATIBILITY.md) — formatos legados e nível de leitura comprovado.
- [BAN_COMPATIBILITY_SCAN.md](BAN_COMPATIBILITY_SCAN.md) — varredura dos 2.689 `.ban` fornecidos.
- [CHARACTERIZATION_TESTS.md](CHARACTERIZATION_TESTS.md) — testes e provas de caracterização.
- [LEGACY_SAVE_STATUS.md](LEGACY_SAVE_STATUS.md) — escala de suporte e situação real dos saves.
- [FIXTURE_CATALOG.md](FIXTURE_CATALOG.md) — fixtures imutáveis, hashes e finalidade.
- [DETERMINISM.md](DETERMINISM.md) — estratégia de RNG e reprodutibilidade.
- [DATA_INTEGRITY.md](DATA_INTEGRITY.md) — proteção contra drift dos dados congelados.
- [PHASE2_STATUS.md](PHASE2_STATUS.md) — checklist e gates restantes.

A regra global de congelamento em `docs/DATA_FREEZE.md` continua ativa.
