package tech.miroslav.caloriecounter.user

import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "app_user")
class AppUser(
    @Id
    var id: UUID = UUID.randomUUID(),

    @Column(name = "auth_user_id", nullable = false, unique = true)
    var authUserId: UUID = UUID.randomUUID(),

    var name: String? = null,
    var gender: String? = null,

    @Column(name = "date_of_birth")
    var dateOfBirth: java.time.LocalDate? = null,

    @Column(name = "current_weight_kg")
    var currentWeightKg: java.math.BigDecimal? = null,

    @Column(name = "height_cm")
    var heightCm: java.math.BigDecimal? = null,

    @Column(name = "activity_level", nullable = false)
    var activityLevel: String = "SEDENTARY",

    @Column(name = "daily_calorie_goal_kcal")
    var dailyCalorieGoalKcal: java.math.BigDecimal? = null,

    @Column(nullable = false)
    var timezone: String = "UTC",

    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now()
)
