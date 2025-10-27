package tech.miroslav.caloriecounter.diary

import java.math.BigDecimal
import java.time.LocalDate

/** Macros container per portion/entry or aggregate (in grams). */
data class MacrosDto(
    val proteinG: BigDecimal,
    val fatG: BigDecimal,
    val carbG: BigDecimal
)

/** Summary for a single diary entry. */
data class DiaryEntrySummaryDto(
    val id: String,
    val foodId: String,
    val foodName: String?,
    val amount: BigDecimal,
    val unit: String,
    val grams: BigDecimal,
    val kcal: BigDecimal,
    val macros: MacrosDto
)

/** Summary for a meal within a diary. */
data class MealSummaryDto(
    val meal: String,
    val totalKcal: BigDecimal,
    val percentOfDailyGoal: BigDecimal?,
    val percentOfEstimatedIntake: BigDecimal?,
    val macros: MacrosDto,
    val entries: List<DiaryEntrySummaryDto>
)

/** Full diary summary. */
data class DiarySummaryDto(
    val diaryId: String,
    val ownerId: String,
    val date: LocalDate,
    val totalKcal: BigDecimal,
    val percentOfDailyGoal: BigDecimal?,
    val percentOfEstimatedIntake: BigDecimal?,
    val macros: MacrosDto,
    val meals: List<MealSummaryDto>
)
