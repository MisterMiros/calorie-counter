package tech.miroslav.caloriecounter.common

import java.math.BigDecimal
import java.util.UUID

/**
 * Centralized application constants and enums that are already decided in the spec.
 */
object AppConstants {
    /**
     * Fixed UUID for the system SHARED auth user (owner of shared catalog entries).
     */
    val SHARED_AUTH_USER_ID: UUID = UUID.fromString("7f761e74-2297-4b79-92f2-55307de133a4")

    /**
     * Supported PostgreSQL full-text search configurations/locales.
     */
    val SUPPORTED_FTS_CONFIGS: Set<String> = setOf("english", "russian")
}

/**
 * Activity levels and their multipliers for TDEE using Mifflin–St Jeor.
 */
enum class ActivityLevel(
    val display: String,
    val multiplier: BigDecimal
) {
    SEDENTARY("Sedentary", BigDecimal("1.2")),
    LIGHTLY_ACTIVE("Lightly active", BigDecimal("1.375")),
    MODERATELY_ACTIVE("Moderately active", BigDecimal("1.55")),
    ACTIVE("Active", BigDecimal("1.725")),
    VERY_ACTIVE("Very active", BigDecimal("1.9"));

    companion object {
        /**
         * Parse by enum name or display label (case-insensitive). Returns null if not matched.
         */
        fun fromString(value: String?): ActivityLevel? {
            if (value.isNullOrBlank()) return null
            val norm = value.trim()
            // Try enum name first
            values().firstOrNull { it.name.equals(norm, ignoreCase = true) }?.let { return it }
            // Then try display label
            return values().firstOrNull { it.display.equals(norm, ignoreCase = true) }
        }
    }
}
