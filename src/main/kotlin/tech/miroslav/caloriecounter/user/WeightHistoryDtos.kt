package tech.miroslav.caloriecounter.user

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.Instant

/**
 * DTOs for weight history endpoints.
 */

data class WeightHistoryDto(
    @Schema(example = "9e1a8c7b-8d1e-4c61-9e65-1b3e3f8b9e1a")
    val id: String,
    val ts: Instant,
    val weightKg: BigDecimal,
    val comment: String?
)

/** Request to create a weight history record. */
data class CreateWeightHistoryRequest(
    /** If null, server uses current instant. */
    val ts: Instant? = null,
    @field:NotNull
    @field:DecimalMin("0.0", inclusive = false)
    val weightKg: BigDecimal?,
    val comment: String? = null
)