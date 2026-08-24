# Phase 4R — Brasfoot 2026/27 save format

## Active save family

The new baseline does not use the previously documented `*.ai21 + *.s21/*.s121` family as its active save format.

Direct evidence from `ActivitySave`, `ActivityLoad` and `est.InfoArquivoSalvoType` establishes:

- metadata: `*.a26`
- main career graph: `*.s26`
- options: `options.bcf`
- metadata serialization: Java `ObjectOutputStream` / `ObjectInputStream`
- career serialization: Kryo with `setRegistrationRequired(false)` and `writeClassAndObject` / `readClassAndObject`
- metadata class: `est.InfoArquivoSalvoType`, `serialVersionUID = 1`
- metadata fields represented by the compatibility shell: `n`, `tc`, `a`, `i`, `path`

New saves are named from `bf` + current time in milliseconds, producing a matching `.a26` / `.s26` pair.

## Load/recovery evidence

The decompiled Java is incomplete in critical load/recovery code. SMALI is therefore authoritative where Java is truncated. The recovered flow confirms Kryo deserialization of the main career object and a backup/recovery path rather than Java serialization of the entire career graph.

Classification for the critical load method: `SMALI_REQUIRED`.

## Compatibility status

The ZIP contains code and data assets but no real user-created `.a26 + .s26` career fixture. Therefore:

- extension recognition: confirmed;
- Java metadata class structure: confirmed;
- metadata reader boundary: implemented;
- Kryo container mechanism: confirmed by Java/SMALI;
- complete career graph compatibility: **not certified**;
- semantic round-trip migration from a real legacy career: **not certified**.

`LegacySaveReader.readCareer()` intentionally remains blocked until a real Brasfoot 2026/27 save pair can be characterized. This prevents a partial decoder from being reported as compatible.

## Superseded historical formats

Recognition of `.ai21`, `.s21`, `.s121` and `.sbck` remains in the compatibility probe only for historical regression/documentation. Those formats are not the Phase 4R factual baseline.
