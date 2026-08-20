# Performance — Fase 3

## Ambiente

Benchmark da fixture real executado no mesmo GitHub Actions gate final, Ubuntu 24.04, Temurin JDK `17.0.20+8`, Android SDK/API 37.

Fixture benchmark (uma fonte `.ban`, 1 clube, 20 seniors, 0 juniors):

| Etapa | nanos | aprox. ms |
|---|---:|---:|
| leitura + decode | 660.889 | 0,661 |
| SHA da fonte | 134.419 | 0,134 |
| adapter Legacy -> V1 | 1.409.969 | 1,410 |
| source manifest | 1.840.982 | 1,841 |
| semantic fingerprint | 388.095 | 0,388 |
| persistência Room | 5.314.928 | 5,315 |
| verificação/round-trip | 5.673.354 | 5,673 |
| total medido | 23.088.813 | 23,089 |

Os tempos usam clock monotônico (`System.nanoTime()`) e não participam de IDs ou fingerprints.

## Limitação

Este é um **fixture benchmark**, não um benchmark do corpus completo de 2.689 arquivos. O corpus integral não estava disponível ao runner final e nenhum valor foi extrapolado.
