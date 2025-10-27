package tech.miroslav.caloriecounter.training

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.OffsetDateTime

/** Response DTO for a training log entry. */
data class TrainingLogEntryDto(
    val id: String,
    val exerciseId: String,
    val durationMin: BigDecimal?,
    val repetitions: Int?,
    val weightKg: BigDecimal?,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
)

/** Request to create a training log entry. */
data class CreateTrainingLogEntryRequest(
    @field:NotNull
    val exerciseId: String?,
    @field:DecimalMin("0.0", inclusive = false)
    val durationMin: BigDecimal? = null,
    @field:Min(1)
    val repetitions: Int? = null,
    @field:DecimalMin("0.0", inclusive = false)
    val weightKg: BigDecimal? = null
)

/** Request to update a training log entry (partial). */
data class UpdateTrainingLogEntryRequest(
    val exerciseId: String? = null,
    @field:DecimalMin("0.0", inclusive = false)
    val durationMin: BigDecimal? = null,
    @field:Min(1)
    val repetitions: Int? = null,
    @field:DecimalMin("0.0", inclusive = false)
    val weightKg: BigDecimal? = null
)
