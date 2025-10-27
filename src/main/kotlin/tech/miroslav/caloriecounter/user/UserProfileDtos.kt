package tech.miroslav.caloriecounter.user

import java.math.BigDecimal
import java.time.LocalDate

/**
 * DTO returned by GET /api/v1/users/me
 */
data class UserProfileDto(
    val id: String,
    val authUserId: String,
    val name: String?,
    val gender: String?,
    val dateOfBirth: LocalDate?,
    val currentWeightKg: BigDecimal?,
    val heightCm: BigDecimal?,
    val activityLevel: String,
    val dailyCalorieGoalKcal: BigDecimal?,
    val timezone: String,
    val bmi: BigDecimal?,
    val estimatedDailyIntakeKcal: BigDecimal?
)
