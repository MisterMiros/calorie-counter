package tech.miroslav.caloriecounter.training

import jakarta.validation.constraints.NotNull
import java.time.LocalDate
import java.time.OffsetDateTime

/** Response DTO for a training log. */
data class TrainingLogDto(
    val id: String,
    val date: LocalDate,
    val comment: String?,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
)

/** Request to create a training log (one per date). */
data class CreateTrainingLogRequest(
    @field:NotNull
    val date: LocalDate?,
    val comment: String? = null
)

/** Request to update a training log; allows changing date and/or comment. */
data class UpdateTrainingLogRequest(
    val date: LocalDate? = null,
    val comment: String? = null
)
