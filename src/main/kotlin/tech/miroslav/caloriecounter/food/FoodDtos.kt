package tech.miroslav.caloriecounter.food

import jakarta.validation.Valid
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import java.math.BigDecimal
import java.time.OffsetDateTime

/** Translation DTO used in create/update */
data class FoodTranslationPayload(
    @field:NotBlank
    val locale: String,
    @field:NotBlank
    val name: String,
    val producer: String? = null
)

/** Request to create a user-owned food item. */
data class CreateFoodRequest(
    @field:NotBlank
    val type: String,
    @field:DecimalMin("0.0", inclusive = false)
    val densityGPerMl: BigDecimal? = null,
    @field:DecimalMin("0.0", inclusive = false)
    val packG: BigDecimal? = null,
    @field:DecimalMin("0.0", inclusive = false)
    val itemG: BigDecimal? = null,
    @field:NotEmpty
    val translations: List<@Valid FoodTranslationPayload>,
    @field:DecimalMin("0.0", inclusive = true)
    val proteinGPer100g: BigDecimal,
    @field:DecimalMin("0.0", inclusive = true)
    val fatGPer100g: BigDecimal,
    @field:DecimalMin("0.0", inclusive = true)
    val carbGPer100g: BigDecimal
)

/** Request to update a user-owned food item. All fields optional; translations replace existing if provided. */
data class UpdateFoodRequest(
    val type: String? = null,
    @field:DecimalMin("0.0", inclusive = false)
    val densityGPerMl: BigDecimal? = null,
    @field:DecimalMin("0.0", inclusive = false)
    val packG: BigDecimal? = null,
    @field:DecimalMin("0.0", inclusive = false)
    val itemG: BigDecimal? = null,
    val translations: List<@Valid FoodTranslationPayload>? = null,
    @field:DecimalMin("0.0", inclusive = true)
    val proteinGPer100g: BigDecimal? = null,
    @field:DecimalMin("0.0", inclusive = true)
    val fatGPer100g: BigDecimal? = null,
    @field:DecimalMin("0.0", inclusive = true)
    val carbGPer100g: BigDecimal? = null
)

/** Response DTO for a food item, including first translation for convenience. */
data class FoodDto(
    val id: String,
    val ownerId: String,
    val type: String,
    val densityGPerMl: BigDecimal?,
    val packG: BigDecimal?,
    val itemG: BigDecimal?,
    val proteinGPer100g: BigDecimal,
    val fatGPer100g: BigDecimal,
    val carbGPer100g: BigDecimal,
    val translations: List<FoodTranslationPayload>,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
    val deletedAt: OffsetDateTime?
)
