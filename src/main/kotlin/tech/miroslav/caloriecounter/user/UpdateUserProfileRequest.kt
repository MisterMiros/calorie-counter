package tech.miroslav.caloriecounter.user

import jakarta.validation.constraints.Positive
import java.math.BigDecimal
import java.time.LocalDate

/**
 * Request DTO for updating current user's profile.
 * All fields are optional; only provided fields will be updated.
 */
 data class UpdateUserProfileRequest(
     val name: String? = null,
     val gender: String? = null,
     val dateOfBirth: LocalDate? = null,
     @field:Positive(message = "currentWeightKg must be > 0")
     val currentWeightKg: BigDecimal? = null,
     @field:Positive(message = "heightCm must be > 0")
     val heightCm: BigDecimal? = null,
     /** Activity level may be enum name or display label; case-insensitive. */
     val activityLevel: String? = null,
     @field:Positive(message = "dailyCalorieGoalKcal must be > 0")
     val dailyCalorieGoalKcal: BigDecimal? = null,
     /** IANA timezone ID, e.g., Europe/Berlin */
     val timezone: String? = null
 )
