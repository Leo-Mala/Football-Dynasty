# Behavioral Parity Checklist

Use this checklist for every migrated gameplay subsystem.

## Required before implementation is marked complete

- [ ] Legacy class/method locations identified.
- [ ] Inputs identified.
- [ ] Outputs identified.
- [ ] Persistent state mutations identified.
- [ ] Global/shared state mutations identified.
- [ ] Randomness or seed behavior identified.
- [ ] Date/season progression effects identified.
- [ ] Financial side effects identified where applicable.
- [ ] Competition/table side effects identified where applicable.
- [ ] Java decompiler gaps checked.
- [ ] Matching SMALI inspected when Java is incomplete or suspicious.
- [ ] Legacy data used by the subsystem remains unchanged.
- [ ] Modern implementation has a repeatable test or comparison fixture.
- [ ] Known deviations are documented explicitly.

## Completion rule

A screen looking correct is not evidence of gameplay parity. A subsystem is complete only after its state transitions and side effects are shown to match the legacy reference for the scenarios being migrated.
