package tech.miroslav.caloriecounter.diary

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.OffsetDateTime

/** Response DTO for a diary entry. */
data class DiaryEntryDto(
    val id: String,
    val foodId: String,
    val amount: BigDecimal,
    val unit: String,
    val meal: String,
    val comment: String?,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
)

/** Request to create a diary entry. */
data class CreateDiaryEntryRequest(
    @field:NotNull
    val foodId: String?,
    @field:NotNull
    @field:DecimalMin("0.0", inclusive = false)
    val amount: BigDecimal?,
    /** Unit must be one of: g, ml, cup, tablespoon, teaspoon, pack, item */
    @field:NotNull
    val unit: String?,
    /** Meal must be one of: breakfast, lunch, dinner, snack */
    @field:NotNull
    val meal: String?,
    val comment: String? = null
)

/** Request to update a diary entry (partial). */
data class UpdateDiaryEntryRequest(
    val foodId: String? = null,
    @field:DecimalMin("0.0", inclusive = false)
    val amount: BigDecimal? = null,
    val unit: String? = null,
    val meal: String? = null,
    val comment: String? = null
)
