package tech.miroslav.caloriecounter.exercise

import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotBlank
import java.time.OffsetDateTime

/** Translation DTO used in create/update */
data class ExerciseTranslationPayload(
    @field:NotBlank
    val locale: String,
    @field:NotBlank
    val name: String
)

/** Request to create a user-owned exercise item. */
data class CreateExerciseRequest(
    @field:NotEmpty
    val translations: List<@Valid ExerciseTranslationPayload>,
    /** Free-form tags */
    val tags: List<@NotBlank String>? = null,
    /** Muscles by name (must exist in reference table) */
    val muscles: List<@NotBlank String>? = null
)

/** Request to update a user-owned exercise item. All fields optional; translations/tags/muscles replace existing if provided. */
data class UpdateExerciseRequest(
    val translations: List<@Valid ExerciseTranslationPayload>? = null,
    val tags: List<@NotBlank String>? = null,
    val muscles: List<@NotBlank String>? = null
)

/** Response DTO for an exercise item. */
data class ExerciseDto(
    val id: String,
    val ownerId: String,
    val translations: List<ExerciseTranslationPayload>,
    val tags: List<String>,
    val muscles: List<String>,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
    val deletedAt: OffsetDateTime?
)
