# Manager identity evidence — official Brasfoot 2026/27 corpus

Official corpus: `Brasfoot.apk_Decompiler.com.zip`  
SHA-256: `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`

## Proven identity lifecycle

Legacy manager identity is numeric and world-scoped. It is **not** the manager name.

- World field `best.b.W0` is initialized to `-1`.
- `best.b.O()` increments that counter and returns the new value.
- `best.f0(String)` initializes manager runtime fields (`G=100`, `H=80`, `I=0`, `M=0`), stores the supplied name, and assigns `f4196c = world.O()`.
- `best.f0.G()` exposes that numeric manager id.
- `best.c0.G1(best.f0)` stores both the transient manager object and its numeric id; a null manager writes `-1`.
- `best.c0.y0()` lazily restores a missing transient manager reference by asking the world for the stored numeric id.
- `best.b.b1(int)` scans `F0()` — the ordered `ArrayList<best.f0>` — from index zero and returns the **first** manager whose `G()` equals the requested id.
- `best.b.h(best.f0)` appends managers to this list.

The first manager created from the initial `W0=-1` therefore receives id `0`; the next receives id `1`, and so on.

## Observable duplicate-id quirk

`best.b.b1(int)` does not enforce uniqueness and does not build a map. It performs an ordered linear scan and returns the first matching element. A malformed/imported legacy graph containing duplicate numeric ids therefore has observable first-match behavior.

Modern persistence must preserve that possibility instead of silently introducing a `UNIQUE(legacyManagerId)` constraint. An ordered `sourceOrdinal` plus non-unique numeric id is the faithful shape for materialized manager state.

## Fingerprints

Java:

- `best/f0.java :: f0(String)` — `3e4e1cc615ca7da21dc18fda2cf46b6abdbd13ead8d82700afef7a92d25d1ba3`
- `best/b.java :: O()` — `057254e7e6343b9037ebf99b43b7e370da2c91974c2349d277cd60c103deafa2`
- `best/f0.java :: G()` — `8b3d0435808d919ad0a513a47fd5564e8d83d66d157e840992670602a916d7a4`
- `best/c0.java :: G1(best.f0)` — `4f7a3aff08ec73a71157f4fad94e4f9b75d61837f2f55465aa945a002c886fd8`
- `best/c0.java :: y0()` — `9b4223a63b2e60ad6679832d746ae1e1ec3d7784e20cc4ce695596a1033e5633`
- `best/b.java :: b1(int)` — `6b7ffc45d7e9e5075fac2a0f8789ac3bcfff92ae156f83576784c4ba457346dc`

SMALI:

- `best/f0.smali :: <init>(String)` — `c2081728b8903a3127c8415921ec2623d4652d964d269984af34b0b5029faed3`
- `best/b.smali :: O()I` — `d0ff07c2a7304593a19c1e3c0b694b63919144fe640b8e969f28252e6f11e4dc`
- `best/f0.smali :: G()I` — `8c7bb7627fbb6bc5adeb14591235b413882a102ba07afd17b917b3c096751af2`
- `best/c0.smali :: G1(Lbest/f0;)V` — `2279e4c71d0bef6bc83533f38c78766641c1307c1ad0b8a2d5332a8f15dbba9c`
- `best/c0.smali :: y0()Lbest/f0;` — `7863e5a385e08ed57f50c0e87f706c5137300865b4f1641557cf4b8da2c7d1eb`
- `best/b.smali :: b1(I)Lbest/f0;` — `747c545160d8ebc91199a5f8c3e027655be3ff53847534d3f070f9e7f6b9a525`

## Modern consequence

`LegacyManagerIdentityRule` characterizes allocation, the club's `-1` null sentinel, and first-match ordered lookup without inventing persistence semantics.

For the Phase 13 ticket path this matters because `home.y0()?.o()` reads mutable manager `H`. A future durable representation must let the club carry the raw manager id and resolve the manager row in world order so `H` follows the manager across employment changes. Storing `H` directly on the club, resolving by name, or enforcing unique numeric ids would all change proven legacy behavior.

No Room migration is introduced by this characterization checkpoint. Existing V8 careers remain fail-closed until the coherent manager/club runtime persistence boundary is added additively.
