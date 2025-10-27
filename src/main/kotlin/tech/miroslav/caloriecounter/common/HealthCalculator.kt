package tech.miroslav.caloriecounter.common

import tech.miroslav.caloriecounter.common.ActivityLevel
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.Period

object HealthCalculator {
    /**
     * BMI = weight_kg / (height_m^2). Returns null if weight or height is null or non-positive.
     */
    fun bmi(weightKg: BigDecimal?, heightCm: BigDecimal?): BigDecimal? {
        if (weightKg == null || heightCm == null) return null
        if (weightKg <= BigDecimal.ZERO || heightCm <= BigDecimal.ZERO) return null
        val heightM = heightCm.divide(BigDecimal(100), 8, RoundingMode.HALF_UP)
        if (heightM.compareTo(BigDecimal.ZERO) == 0) return null
        val bmi = weightKg.divide(heightM.multiply(heightM), 6, RoundingMode.HALF_UP)
        return bmi.setScale(2, RoundingMode.HALF_UP)
    }

    /**
     * Mifflin–St Jeor BMR.
     * gender: "male" or "female" (case-insensitive). Other values return null.
     * Returns null if any of age, weight, height is missing.
     */
    fun bmrMifflinStJeor(
        gender: String?,
        dateOfBirth: LocalDate?,
        weightKg: BigDecimal?,
        heightCm: BigDecimal?,
        today: LocalDate = LocalDate.now()
    ): BigDecimal? {
        if (dateOfBirth == null || weightKg == null || heightCm == null) return null
        val age = Period.between(dateOfBirth, today).years
        if (age < 0) return null
        val w = weightKg.toDouble()
        val h = heightCm.toDouble()
        val bmr = when (gender?.lowercase()) {
            "male", "m" -> 10 * w + 6.25 * h - 5 * age + 5
            "female", "f" -> 10 * w + 6.25 * h - 5 * age - 161
            else -> return null
        }
        return BigDecimal(bmr).setScale(2, RoundingMode.HALF_UP)
    }

    /**
     * Estimated daily intake (TDEE) = BMR * activity multiplier.
     * Returns null if BMR is null or activity cannot be parsed.
     */
    fun estimatedDailyIntake(
        bmr: BigDecimal?,
        activityLevel: String?
    ): BigDecimal? {
        if (bmr == null) return null
        val level = ActivityLevel.fromString(activityLevel) ?: ActivityLevel.SEDENTARY
        return bmr.multiply(level.multiplier).setScale(2, RoundingMode.HALF_UP)
    }
}
