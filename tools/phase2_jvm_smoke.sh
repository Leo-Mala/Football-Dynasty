#!/bin/sh
set -eu

ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
WORK="${TMPDIR:-/tmp}/football-dynasty-phase2-jvm"
FIXTURE_B64="$ROOT/app/src/test/resources/legacy/12deoctubre_par.ban.b64"
FIXTURE="$WORK/12deoctubre_par.ban"

for tool in javac kotlinc kotlin base64; do
    command -v "$tool" >/dev/null 2>&1 || {
        echo "Missing required tool: $tool" >&2
        exit 2
    }
done

rm -rf "$WORK"
mkdir -p "$WORK/java" "$WORK/kotlin" "$WORK/proof"
base64 -d "$FIXTURE_B64" > "$FIXTURE"

javac --release 17 -d "$WORK/java" \
    "$ROOT/app/src/main/java/e/g.java" \
    "$ROOT/app/src/main/java/e/t.java" \
    "$ROOT/app/src/main/java/est/InfoArquivoSalvoType.java"

kotlinc \
    "$ROOT/app/src/main/java/com/leomala/footballdynasty/domain/model/LegacySnapshots.kt" \
    "$ROOT/app/src/main/java/com/leomala/footballdynasty/foundation/random/RandomSource.kt" \
    "$ROOT/app/src/main/java/com/leomala/footballdynasty/legacy/compatibility/LegacySerialization.kt" \
    "$ROOT/app/src/main/java/com/leomala/footballdynasty/legacy/compatibility/DeterministicFingerprint.kt" \
    "$ROOT/app/src/main/java/com/leomala/footballdynasty/legacy/compatibility/LegacySchemaCatalog.kt" \
    "$ROOT/app/src/main/java/com/leomala/footballdynasty/legacy/compatibility/LegacyFormatProbe.kt" \
    "$ROOT/app/src/main/java/com/leomala/footballdynasty/legacy/compatibility/LegacySaveReader.kt" \
    -classpath "$WORK/java" \
    -d "$WORK/kotlin"

kotlinc "$ROOT/tools/Phase2JvmProof.kt" \
    -classpath "$WORK/java:$WORK/kotlin" \
    -d "$WORK/proof"

kotlin -classpath "$WORK/java:$WORK/kotlin:$WORK/proof" Phase2JvmProofKt "$FIXTURE"
