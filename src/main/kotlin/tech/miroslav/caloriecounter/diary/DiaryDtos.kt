package tech.miroslav.caloriecounter.diary

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import java.time.LocalDate
import java.time.OffsetDateTime

/** Response DTO for a diary. */
data class DiaryDto(
    val id: String,
    val date: LocalDate,
    val comment: String?,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
)

/** Request to create a diary (one per date). */
data class CreateDiaryRequest(
    @field:NotNull
    @Schema(example = "2025-10-27")
    val date: LocalDate?,
    val comment: String? = null
)

/** Request to update a diary; allows changing date and/or comment. */
data class UpdateDiaryRequest(
    val date: LocalDate? = null,
    val comment: String? = null
)
