#!/usr/bin/env python3
import json
import re
import subprocess
from pathlib import Path

SCHEMA_DIR = Path("app/schemas/com.leomala.footballdynasty.data.local.FootballDynastyDatabase")
BASELINE_DIR = Path("/tmp/versioned-room-schemas")
EXPECTED_FILES = {"1.json", "2.json", "3.json", "13.json", "14.json", "15.json", "16.json", "17.json"}
IDENTITIES = {
    1: "37c2e4df984290903730a25553bdbed5",
    2: "fc4ed30d6548037a1226144e0c576c59",
    3: "a6d299fb67fcdcc51340cc62982e2d5a",
    9: "e246b6749364b3d2f7891177c2179fb4",
    10: "086ae14fb0b6a625e6f22446d47df0b7",
    11: "4d06657c2d3a4c3300b1fa598e4c9b4d",
    12: "6b87dfc792ccf982f9e1a83cfcc8e8b6",
    13: "6575d77e3eef9ea84d059c2aa2bdf14b",
    14: "49734dba8b165fbf8999ac19f3832f1e",
    15: "f928b7c2fd9fbc202cb5dfb238ddcc3e",
    16: "80aface69f2421741631f9438b4bd88a",
    17: "5c9f4997fab8ccd026c20143daf1d2b6",
}


def fail(message: str) -> None:
    raise SystemExit(message)


def load_schema(version: int) -> dict:
    name = f"{version}.json"
    before = json.loads((BASELINE_DIR / name).read_text())
    after = json.loads((SCHEMA_DIR / name).read_text())
    if before != after:
        fail(f"Generated Room schema {name} differs from versioned schema")
    database = after["database"]
    if database.get("version") != version or database.get("identityHash") != IDENTITIES[version]:
        fail(f"Room schema {version} identity/version changed")
    return database


if {p.name for p in SCHEMA_DIR.glob("*.json")} != EXPECTED_FILES:
    fail("Unexpected generated Room schema set")
if {p.name for p in BASELINE_DIR.glob("*.json")} != EXPECTED_FILES:
    fail("Unexpected versioned Room schema baseline")

db = {version: load_schema(version) for version in (1, 2, 3, 13, 14, 15, 16, 17)}

v1_from_phase3 = json.loads(
    subprocess.check_output(
        [
            "git", "show",
            "origin/phase3/versioned-persistence:app/schemas/com.leomala.footballdynasty.data.local.FootballDynastyDatabase/1.json",
        ],
        text=True,
    )
)
if json.loads((SCHEMA_DIR / "1.json").read_text()) != v1_from_phase3:
    fail("Room V1 changed from phase3/versioned-persistence")

e3 = {e["tableName"]: e for e in db[3]["entities"]}
e13 = {e["tableName"]: e for e in db[13]["entities"]}
e14 = {e["tableName"]: e for e in db[14]["entities"]}
e15 = {e["tableName"]: e for e in db[15]["entities"]}
e16 = {e["tableName"]: e for e in db[16]["entities"]}
e17 = {e["tableName"]: e for e in db[17]["entities"]}

required3 = {"career_core_state", "career_player_runtime", "career_procedural_players", "career_squad_memberships"}
if not required3.issubset(e3):
    fail("Room V3 required tables missing")
if not set(e3).issubset(e13):
    fail("Room V13 does not preserve V3")
if not set(e13).issubset(e14):
    fail("Room V14 does not preserve V13")
if set(e14) != set(e15):
    fail("Room V15 must preserve the V14 table set")
if set(e16) != set(e15) | {"career_competition_player_ratings"}:
    fail("Room V16 must add only the proven competition player rating table")
new_v17 = {
    "career_competition_snapshots",
    "career_competition_snapshot_members",
    "career_player_match_rating_history",
}
if set(e17) != set(e16) | new_v17:
    fail("Room V17 must add only the three proven non-reconstructible durability tables")

junior = e14.get("career_junior_drafts")
expected_junior = {
    "careerId", "clubId", "sourceOrdinal", "legacyN", "legacyB", "legacyC", "legacyE", "legacyJ",
    "legacyL", "legacyD", "name", "legacyG", "legacyF", "legacyO", "legacyM", "legacyH", "legacyI",
    "developmentRemainder",
}
if junior is None or {f["columnName"] for f in junior["fields"]} != expected_junior:
    fail("V14 junior draft contract changed")

runtime15 = e15["career_player_runtime"]
runtime15_fields = {f["columnName"] for f in runtime15["fields"]}
for field_name in ("legacyAnnualM", "legacyAnnualN", "legacyRawPayrollN"):
    if field_name not in runtime15_fields:
        fail(f"V15 senior runtime field missing: {field_name}")
    field = next(f for f in runtime15["fields"] if f["columnName"] == field_name)
    if field.get("notNull", False):
        fail(f"V15 migrated senior runtime field must remain nullable: {field_name}")

rating16 = e16["career_competition_player_ratings"]
expected_rating16_fields = {
    "careerId", "competitionId", "playerId", "legacyRatingSum", "legacyRatingCount",
    "legacyAverageRating", "legacyCategory",
}
if {f["columnName"] for f in rating16["fields"]} != expected_rating16_fields:
    fail("V16 competition rating fields changed")
if rating16["primaryKey"].get("columnNames") != ["careerId", "competitionId", "playerId"]:
    fail("V16 competition rating primary key changed")
for field_name in ("legacyRatingSum", "legacyRatingCount", "legacyAverageRating"):
    field = next(f for f in rating16["fields"] if f["columnName"] == field_name)
    if field.get("affinity") != "REAL" or not field.get("notNull", False):
        fail(f"V16 {field_name} must remain non-null REAL")
category = next(f for f in rating16["fields"] if f["columnName"] == "legacyCategory")
if category.get("affinity") != "INTEGER" or not category.get("notNull", False):
    fail("V16 legacyCategory must remain non-null INTEGER")
expected_rating_fk = {
    "table": "career_competitions", "onDelete": "CASCADE", "onUpdate": "NO ACTION",
    "columns": ["careerId", "competitionId"], "referencedColumns": ["careerId", "competitionId"],
}
if rating16.get("foreignKeys") != [expected_rating_fk]:
    fail("V16 rating ownership FK must remain competition-only and cascading")
indices = {(tuple(i.get("columnNames", [])), i.get("unique")) for i in rating16.get("indices", [])}
if indices != {(('careerId', 'competitionId'), False), (('careerId', 'playerId'), False)}:
    fail("V16 rating indices changed")

fields13 = lambda table: {f["columnName"] for f in e13[table]["fields"]}
required_v6 = {
    "career_scheduled_matches", "career_player_club_season_stats", "career_competitions",
    "career_competition_standings", "career_competition_matches",
}
if not required_v6.issubset(e13):
    fail(f"Room V13 lost V4/V5/V6 tables: {sorted(required_v6 - set(e13))}")
if fields13("career_scheduled_matches") != {
    "careerId", "matchId", "dayIndex", "eventTypeCode", "homeClubId", "awayClubId", "processed",
    "homeGoals", "awayGoals",
}:
    fail("V4 scheduled-match contract changed")
if not {"energy", "injuryUntilEpochDay"}.issubset(fields13("career_player_runtime")):
    fail("V5 player runtime fields changed")
if fields13("career_player_club_season_stats") != {
    "careerId", "playerId", "legacySeasonId", "legacyClubId", "legacyC", "legacyD", "legacyE", "legacyF",
    "legacyG", "legacyH",
}:
    fail("V5 season-stats contract changed")
expected_competition13 = {
    "careerId", "competitionId", "legacyCompetitionType", "legacyFormatCode", "currentRoundNumber",
    "totalRounds", "legacyRelegationCount", "legacyLeagueSubtype",
}
if fields13("career_competitions") != expected_competition13:
    fail("V13 competition input contract changed")
relegation = next(f for f in e13["career_competitions"]["fields"] if f["columnName"] == "legacyRelegationCount")
subtype = next(f for f in e13["career_competitions"]["fields"] if f["columnName"] == "legacyLeagueSubtype")
if relegation.get("affinity") != "INTEGER" or relegation.get("notNull", False):
    fail("V12 legacyRelegationCount must remain nullable INTEGER")
if subtype.get("affinity") != "INTEGER" or subtype.get("notNull", False):
    fail("V13 legacyLeagueSubtype must be nullable INTEGER")
if fields13("career_competition_standings") != {
    "careerId", "competitionId", "clubId", "stableOrdinal", "points", "played", "wins", "losses",
    "goalsFor", "goalsAgainst",
}:
    fail("V6 standings contract changed")
if fields13("career_competition_matches") != {"careerId", "competitionId", "matchId", "roundNumber", "fixtureOrdinal"}:
    fail("V6 competition-match contract changed")

required7 = {
    "career_player_commercial", "career_player_transfer_state", "career_club_manager_runtime",
    "career_active_loans", "career_stadium_constructions",
}
if not required7.issubset(e13):
    fail(f"Room V13 lost V7 manager tables: {sorted(required7 - set(e13))}")
if "career_stadium_runtime" not in e13:
    fail("Room V13 lost V8 stadium runtime table")
if fields13("career_stadium_runtime") != {
    "careerId", "clubId", "sector0Capacity", "sector1Capacity", "sector2Capacity", "sector3Capacity",
}:
    fail("V8 stadium-sector contract changed")
required9 = {"career_club_ticket_runtime", "career_manager_ticket_runtime", "career_match_construction_source"}
if not required9.issubset(e13):
    fail(f"Room V13 lost V9 ticket runtime tables: {sorted(required9 - set(e13))}")
if fields13("career_club_ticket_runtime") != {"careerId", "clubId", "rawDivisionCode", "legacyManagerId"}:
    fail("V9 club ticket-state contract changed")
if fields13("career_manager_ticket_runtime") != {"careerId", "sourceOrdinal", "legacyManagerId", "rawH"}:
    fail("V9 manager ticket-state contract changed")
if fields13("career_match_construction_source") != {"careerId", "matchId", "sourceCode"}:
    fail("V9 match construction-source contract changed")
construction = e13["career_stadium_constructions"]
expected_construction = {
    "careerId", "sourceOrdinal", "stadiumCode", "endTimestampMillis", "addition0", "addition1", "addition2",
    "addition3", "ownerClubId",
}
if fields13("career_stadium_constructions") != expected_construction:
    fail("V10 construction ownership contract changed")
owner = next(f for f in construction["fields"] if f["columnName"] == "ownerClubId")
if owner.get("affinity") != "TEXT" or owner.get("notNull", False):
    fail("V10 construction owner must be nullable TEXT")
manager = e13["career_manager_ticket_runtime"]
if not any(i.get("columnNames") == ["careerId", "legacyManagerId"] and not i.get("unique") for i in manager.get("indices", [])):
    fail("V9 manager identity index must remain non-unique")
required11 = {"career_coach_runtime", "career_coach_season_club_records"}
if not required11.issubset(e13):
    fail(f"Room V11 coach tables missing from V13: {sorted(required11 - set(e13))}")
if fields13("career_coach_runtime") != {
    "careerId", "managerSourceOrdinal", "isUserControlled", "currentClubId", "alternativeClubId", "previousClubId",
    "previousClubCountry", "previousClubDivisionIndex", "rawG", "rawD", "rawE", "rawF", "rawO", "rawM",
}:
    fail("V11 coach runtime contract changed")
if fields13("career_coach_season_club_records") != {
    "careerId", "managerSourceOrdinal", "sourceOrdinal", "legacySeasonId", "legacyClubId", "rawMatches", "rawWins",
    "rawLosses", "rawPoints", "rawOtherCount",
}:
    fail("V11 coach record contract changed")
coach = e13["career_coach_runtime"]
records = e13["career_coach_season_club_records"]
coach_fk = {
    "table": "career_manager_ticket_runtime", "onDelete": "CASCADE", "onUpdate": "NO ACTION",
    "columns": ["careerId", "managerSourceOrdinal"], "referencedColumns": ["careerId", "sourceOrdinal"],
}
record_fk = {
    "table": "career_coach_runtime", "onDelete": "CASCADE", "onUpdate": "NO ACTION",
    "columns": ["careerId", "managerSourceOrdinal"], "referencedColumns": ["careerId", "managerSourceOrdinal"],
}
if coach_fk not in coach.get("foreignKeys", []):
    fail("V11 coach runtime must remain child of ordered V9 manager row")
if record_fk not in records.get("foreignKeys", []):
    fail("V11 coach records must cascade from coach runtime")
if not any(i.get("columnNames") == ["careerId", "managerSourceOrdinal"] and not i.get("unique") for i in records.get("indices", [])):
    fail("V11 coach record order index contract changed")

# V17 additive/fail-closed contract.
competition17 = e17["career_competitions"]
if {f["columnName"] for f in competition17["fields"]} != expected_competition13 | {"legacyCompetitionIndex", "legacyGroupCountA0"}:
    fail("V17 competition durability fields changed")
for field_name in ("legacyCompetitionIndex", "legacyGroupCountA0"):
    field = next(f for f in competition17["fields"] if f["columnName"] == field_name)
    if field.get("affinity") != "INTEGER" or field.get("notNull", False):
        fail(f"V17 {field_name} must remain nullable INTEGER")

rating17 = e17["career_competition_player_ratings"]
if {f["columnName"] for f in rating17["fields"]} != expected_rating16_fields | {"legacyStableOrdinal"}:
    fail("V17 rating stable-order contract changed")
stable = next(f for f in rating17["fields"] if f["columnName"] == "legacyStableOrdinal")
if stable.get("affinity") != "INTEGER" or stable.get("notNull", False):
    fail("V17 legacyStableOrdinal must remain nullable INTEGER for migrated V16 rows")

scheduled17 = e17["career_scheduled_matches"]
expected_scheduled17 = {
    "careerId", "matchId", "dayIndex", "eventTypeCode", "homeClubId", "awayClubId", "processed",
    "homeGoals", "awayGoals", "legacyDayMatchOrdinal", "legacyTieBreakActive", "legacyTieBreakWinnerClubId",
}
if {f["columnName"] for f in scheduled17["fields"]} != expected_scheduled17:
    fail("V17 scheduled-match durability fields changed")
for field_name, affinity in (
    ("legacyDayMatchOrdinal", "INTEGER"),
    ("legacyTieBreakActive", "INTEGER"),
    ("legacyTieBreakWinnerClubId", "TEXT"),
):
    field = next(f for f in scheduled17["fields"] if f["columnName"] == field_name)
    if field.get("affinity") != affinity or field.get("notNull", False):
        fail(f"V17 {field_name} must remain nullable {affinity}")

snapshot = e17["career_competition_snapshots"]
if {f["columnName"] for f in snapshot["fields"]} != {
    "careerId", "competitionId", "snapshotKind", "snapshotOrdinal", "legacyA", "legacyB", "topPlayerId",
}:
    fail("V17 competition snapshot fields changed")
if snapshot["primaryKey"].get("columnNames") != ["careerId", "competitionId", "snapshotKind", "snapshotOrdinal"]:
    fail("V17 competition snapshot primary key changed")
if snapshot.get("foreignKeys") != [{
    "table": "career_competitions", "onDelete": "CASCADE", "onUpdate": "NO ACTION",
    "columns": ["careerId", "competitionId"], "referencedColumns": ["careerId", "competitionId"],
}]:
    fail("V17 competition snapshot ownership FK changed")

member = e17["career_competition_snapshot_members"]
if {f["columnName"] for f in member["fields"]} != {
    "careerId", "competitionId", "snapshotKind", "snapshotOrdinal", "memberOrdinal", "playerId", "clubIdAtSnapshot",
}:
    fail("V17 competition snapshot member fields changed")
if member["primaryKey"].get("columnNames") != ["careerId", "competitionId", "snapshotKind", "snapshotOrdinal", "memberOrdinal"]:
    fail("V17 snapshot member primary key changed")
club_snapshot = next(f for f in member["fields"] if f["columnName"] == "clubIdAtSnapshot")
if club_snapshot.get("affinity") != "TEXT" or club_snapshot.get("notNull", False):
    fail("V17 historical club snapshot must remain nullable TEXT")

history = e17["career_player_match_rating_history"]
if {f["columnName"] for f in history["fields"]} != {
    "careerId", "playerId", "legacyDayIndexB", "legacyDayMatchIndexC", "legacyRating",
}:
    fail("V17 player match rating history fields changed")
if history["primaryKey"].get("columnNames") != ["careerId", "playerId", "legacyDayIndexB", "legacyDayMatchIndexC"]:
    fail("V17 player match rating history primary key changed")
if history.get("foreignKeys") != [{
    "table": "career_player_runtime", "onDelete": "CASCADE", "onUpdate": "NO ACTION",
    "columns": ["careerId", "playerId"], "referencedColumns": ["careerId", "playerId"],
}]:
    fail("V17 player rating history ownership FK changed")

src = Path("app/src/main/java/com/leomala/footballdynasty/data/local/FootballDynastyDatabase.kt").read_text()
factory = Path("app/src/main/java/com/leomala/footballdynasty/data/local/FootballDynastyDatabaseFactory.kt").read_text()
p10 = Path("app/src/main/java/com/leomala/footballdynasty/data/local/Phase10CompetitionMigration.kt").read_text()
p12 = Path("app/src/main/java/com/leomala/footballdynasty/data/local/Phase12ManagerPersistenceMigration.kt").read_text()
p13 = Path("app/src/main/java/com/leomala/footballdynasty/data/local/Phase13StadiumRuntimeMigration.kt").read_text()
p13ticket = Path("app/src/main/java/com/leomala/footballdynasty/data/local/Phase13TicketRuntimeMigration.kt").read_text()
p13owner = Path("app/src/main/java/com/leomala/footballdynasty/data/local/Phase13StadiumConstructionOwnershipMigration.kt").read_text()
p14 = Path("app/src/main/java/com/leomala/footballdynasty/data/local/Phase14CoachRuntimeMigration.kt").read_text()
p14inputs = Path("app/src/main/java/com/leomala/footballdynasty/data/local/Phase14CompetitionInputsMigration.kt").read_text()
p15 = Path("app/src/main/java/com/leomala/footballdynasty/data/local/Phase15JuniorDraftMigration.kt").read_text()
p15senior = Path("app/src/main/java/com/leomala/footballdynasty/data/local/Phase15SeniorRuntimeMigration.kt").read_text()
p16 = Path("app/src/main/java/com/leomala/footballdynasty/data/local/Phase16CompetitionPlayerRatingMigration.kt").read_text()
p17 = Path("app/src/main/java/com/leomala/footballdynasty/data/local/Phase17LegacyDurabilityMigration.kt").read_text()
migration9 = Path("app/src/test/java/com/leomala/footballdynasty/data/local/Migration9To10Test.kt").read_text()
migration10 = Path("app/src/test/java/com/leomala/footballdynasty/data/local/Migration10To11Test.kt").read_text()
migration12 = Path("app/src/test/java/com/leomala/footballdynasty/data/local/Migration12To13Test.kt").read_text()
migration13 = Path("app/src/test/java/com/leomala/footballdynasty/data/local/Migration13To14Test.kt").read_text()

if "const val SCHEMA_VERSION: Int = 17" not in src:
    fail("SCHEMA_VERSION must be 17")
if IDENTITIES[9] not in migration9:
    fail("Migration9To10Test must pin certified V9 identity")
if IDENTITIES[10] not in migration10:
    fail("Migration10To11Test must pin certified V10 identity")
if IDENTITIES[12] not in migration12:
    fail("Migration12To13Test must pin certified V12 identity")
if IDENTITIES[13] not in migration13:
    fail("Migration13To14Test must pin certified V13 identity")
for label, migration in (
    ("V10->V11", p14), ("V11->V13", p14inputs), ("V13->V14", p15),
    ("V14->V15", p15senior), ("V15->V16", p16), ("V16->V17", p17),
):
    if re.search(r"\bINSERT\s+INTO\b", migration, re.I) or re.search(r"\bUPDATE\s+career_", migration, re.I):
        fail(f"{label} migration must not fabricate historical state")
if "ADD COLUMN `legacyRelegationCount` INTEGER" not in p14inputs:
    fail("V11->V12 must add nullable exact relegation count")
if "ADD COLUMN `legacyLeagueSubtype` INTEGER" not in p14inputs:
    fail("V12->V13 must add nullable exact x0 subtype")
for migration in ("Migration(1, 2)", "Migration(2, 3)", "Migration(3, 4)", "Migration(4, 5)"):
    if migration not in factory:
        fail(f"Explicit Room migration missing: {migration}")
if (
    "Migration(5, 6)" not in p10 or "Migration(6, 7)" not in p12 or "Migration(7, 8)" not in p13
    or "Migration(8, 9)" not in p13ticket or "Migration(9, 10)" not in p13owner
    or "Migration(10, 11)" not in p14 or "Migration(11, 12)" not in p14inputs
    or "Migration(12, 13)" not in p14inputs or "Migration(13, 14)" not in p15
    or "Migration(14, 15)" not in p15senior or "Migration(15, 16)" not in p16
    or "Migration(16, 17)" not in p17
):
    fail("V5->V6 through V16->V17 migration chain incomplete")

match = re.search(r"val ALL: Array<Migration> = arrayOf\((.*?)\)", factory, re.S)
order = re.findall(r"MIGRATION_\d+_\d+", match.group(1) if match else "")
expected_order = [
    "MIGRATION_1_2", "MIGRATION_2_3", "MIGRATION_3_4", "MIGRATION_4_5", "MIGRATION_5_6",
    "MIGRATION_6_7", "MIGRATION_7_8", "MIGRATION_8_9", "MIGRATION_9_10", "MIGRATION_10_11",
    "MIGRATION_11_12", "MIGRATION_12_13", "MIGRATION_13_14", "MIGRATION_14_15", "MIGRATION_15_16",
    "MIGRATION_16_17",
]
if order != expected_order:
    fail(f"Migration registry order changed: {order}")

for version in (3, 9, 10, 12, 13, 14, 15, 16, 17):
    print(f"ROOM_V{version}_IDENTITY_HASH={IDENTITIES[version]}")
print("ROOM_V10_STADIUM_OWNERSHIP_CONTRACT=PASS")
print("ROOM_V11_COACH_RUNTIME_FAIL_CLOSED_CONTRACT=PASS")
print("ROOM_V12_RELEGATION_COUNT_FAIL_CLOSED_CONTRACT=PASS")
print("ROOM_V13_LEAGUE_SUBTYPE_FAIL_CLOSED_CONTRACT=PASS")
print("ROOM_V14_JUNIOR_DRAFT_CONTRACT=PASS")
print("ROOM_V15_SENIOR_RUNTIME_FAIL_CLOSED_CONTRACT=PASS")
print("ROOM_V16_COMPETITION_PLAYER_RATING_CONTRACT=PASS")
print("ROOM_V17_LEGACY_DURABILITY_CONTRACT=PASS")
