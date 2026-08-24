# Phase 4R — `.ban` corpus characterization

## Layout

The official 2026/27 corpus contains exactly 1,687 `.ban` files:

- `resources/assets/teams2026/`: 687
- `resources/assets/packs/`: 1,000

All files were opened read-only from the supplied ZIP. No original legacy file was rewritten.

## Serialization compatibility

The legacy team/player shells remain structurally compatible with the new corpus:

- team class: `e.t`
- player class: `e.g`
- `serialVersionUID`: `16` for both

The field layout used by the compatibility boundary matches the new decompiled Java and was exercised against every `.ban` stream.

## Corpus-wide gate

```text
TOTAL_BAN_FILES=1687
READ_SUCCESS=1687
READ_FAILURES=0
TOTAL_PLAYERS=35432
TOTAL_JUNIORS=4164
UNIQUE_PHYSICAL_HASHES=1515
DUPLICATE_HASH_GROUPS=172
DUPLICATE_FILES=172
UNKNOWN_SERIALIZED_CLASSES=0
```

The 172 duplicate files are byte-identical pairs/groups in the supplied corpus; they are retained because the rebaseline is read-only and does not delete or normalize factual legacy data.

## Integrity fingerprints

Two aggregate fingerprints are recorded:

- Physical manifest SHA-256: `150643f62e29bcd211a83843239e2ba52721bae1decb6d6021bfa1554f957e1f`
  - deterministic SHA-256 over sorted `path<TAB>size<TAB>file_sha256` rows for all `.ban` files.
- Semantic manifest SHA-256: `377baca2c9b5f806ab2a94eabbbc34aa2e012681f5ecce8c9a5b19441dff5a37`
  - deterministic SHA-256 over the sorted corpus after deserialization and canonical team/player fingerprinting.

These values distinguish physical byte drift from interpreted sporting-data drift.

## Active immutable fixture

The active Phase 4R fixture is:

`resources/assets/teams2026/san/trepenne_smr.ban`

- file SHA-256: `c664cc841b44e39423835795b00bb1c248862eb0a0e1c579831857d748fa9281`
- semantic fingerprint: `edf33fe98fa1c490d4b86f2b73e172ecb6ba063e09fb6f289bec66fca2ca46c8`
- team: `Tre Penne`
- players: 14
- juniors: 0

The previous `12deoctubre_par.ban` fixture remains only as a superseded historical-regression fixture and is no longer the official factual baseline.
