package com.leomala.footballdynasty.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Exact seven-field commercial slice proven on legacy serialized player `a.p`.
 *
 * Rows are created only from explicitly materialized source/runtime state. Migration from V6 does
 * not synthesize rows because the previous schema cannot prove values for these primitive fields.
 */
@Entity(
    tableName = "career_player_commercial",
    primaryKeys = ["careerId", "playerId"],
    foreignKeys = [
        ForeignKey(
            entity = CareerPlayerRuntimeEntity::class,
            parentColumns = ["careerId", "playerId"],
            childColumns = ["careerId", "playerId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.NO_ACTION,
        ),
    ],
    indices = [Index(value = ["playerId"])],
)
data class CareerPlayerCommercialEntity(
    val careerId: String,
    val playerId: String,
    val salario: Int,
    val rcClause: Int,
    val rcRenewYear: Int,
    val rcConvYear: Int,
    val pendSaleClub: Int,
    val pendSaleValue: Int,
    val pendIsLoan: Boolean,
)

/** Raw transfer-only player fields required by characterized `best.o.T1/Q1`. */
@Entity(
    tableName = "career_player_transfer_state",
    primaryKeys = ["careerId", "playerId"],
    foreignKeys = [
        ForeignKey(
            entity = CareerPlayerRuntimeEntity::class,
            parentColumns = ["careerId", "playerId"],
            childColumns = ["careerId", "playerId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.NO_ACTION,
        ),
    ],
    indices = [
        Index(value = ["playerId"]),
        Index(value = ["careerId", "legacyPlayerCode"], unique = true),
    ],
)
data class CareerPlayerTransferStateEntity(
    val careerId: String,
    val playerId: String,
    val legacyPlayerCode: Int,
    val legacyClubCode: Int,
    val rawCrossActiveFlag: Boolean,
    val rawOCode: Int,
    val rawDCode: Int,
)

/**
 * Career-local club state shared by characterized transfer and finance runtimes.
 * Global `ClubEntity` remains immutable sporting/source data.
 */
@Entity(
    tableName = "career_club_manager_runtime",
    primaryKeys = ["careerId", "clubId"],
    foreignKeys = [
        ForeignKey(
            entity = CareerMetadataEntity::class,
            parentColumns = ["id"],
            childColumns = ["careerId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.NO_ACTION,
        ),
        ForeignKey(
            entity = ClubEntity::class,
            parentColumns = ["id"],
            childColumns = ["clubId"],
            onDelete = ForeignKey.NO_ACTION,
            onUpdate = ForeignKey.NO_ACTION,
        ),
    ],
    indices = [Index(value = ["clubId"])],
)
data class CareerClubManagerRuntimeEntity(
    val careerId: String,
    val clubId: String,
    val active: Boolean,
    val cash: Long,
    val primarySlotPlayerCode: Int?,
    val secondarySlotPlayerCode: Int?,
    val rawStateFlag: Boolean,
    val ticketIncome: Int,
    val playerSaleIncome: Long,
    val prizeIncome: Int,
    val sponsorIncome: Int,
    val playerPurchaseExpense: Long,
    val stadiumExpense: Int,
    val salaryExpense: Long,
    val borrowingChargeExpense: Int,
    val fineExpense: Int,
    val miscellaneousExpense: Int,
    val borrowed: Int,
    val monthlyBorrowingCharge: Int,
)

/** Active loan record corresponding to serialized legacy `components.o2`. */
@Entity(
    tableName = "career_active_loans",
    primaryKeys = ["careerId", "playerId"],
    foreignKeys = [
        ForeignKey(
            entity = CareerPlayerRuntimeEntity::class,
            parentColumns = ["careerId", "playerId"],
            childColumns = ["careerId", "playerId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.NO_ACTION,
        ),
        ForeignKey(
            entity = ClubEntity::class,
            parentColumns = ["id"],
            childColumns = ["sourceClubId"],
            onDelete = ForeignKey.NO_ACTION,
            onUpdate = ForeignKey.NO_ACTION,
        ),
        ForeignKey(
            entity = ClubEntity::class,
            parentColumns = ["id"],
            childColumns = ["destinationClubId"],
            onDelete = ForeignKey.NO_ACTION,
            onUpdate = ForeignKey.NO_ACTION,
        ),
    ],
    indices = [
        Index(value = ["sourceClubId"]),
        Index(value = ["destinationClubId"]),
    ],
)
data class CareerActiveLoanEntity(
    val careerId: String,
    val playerId: String,
    val sourceClubId: String,
    val destinationClubId: String,
    val expiresAtEpochMillis: Long,
)

/**
 * Ordered legacy `components.y1` construction list.
 *
 * `stadiumCode` remains opaque legacy data. `ownerClubId` is deliberately separate modern
 * persistence metadata: it records the already-known club context when a new construction is
 * started. V9 rows migrate with NULL ownership and therefore fail closed at completion rather than
 * guessing a legacy stadium-code mapping.
 */
@Entity(
    tableName = "career_stadium_constructions",
    primaryKeys = ["careerId", "sourceOrdinal"],
    foreignKeys = [
        ForeignKey(
            entity = CareerMetadataEntity::class,
            parentColumns = ["id"],
            childColumns = ["careerId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.NO_ACTION,
        ),
    ],
)
data class CareerStadiumConstructionEntity(
    val careerId: String,
    val sourceOrdinal: Int,
    val stadiumCode: Int,
    val endTimestampMillis: Long,
    val addition0: Int,
    val addition1: Int,
    val addition2: Int,
    val addition3: Int,
    val ownerClubId: String? = null,
)
