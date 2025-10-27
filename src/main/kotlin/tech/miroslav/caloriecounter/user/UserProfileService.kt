package tech.miroslav.caloriecounter.user

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tech.miroslav.caloriecounter.common.ActivityLevel
import tech.miroslav.caloriecounter.common.BadRequestException
import tech.miroslav.caloriecounter.common.HealthCalculator
import tech.miroslav.caloriecounter.common.NotFoundException
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.*

@Service
class UserProfileService(
    private val appUserRepository: AppUserRepository
) {
    @Transactional(readOnly = true)
    fun getMyProfile(authUserId: UUID): UserProfileDto {
        val app = appUserRepository.findByAuthUserId(authUserId)
            ?: throw NotFoundException("User profile not found")
        return buildDto(app)
    }

    @Transactional
    fun updateMyProfile(authUserId: UUID, req: UpdateUserProfileRequest): UserProfileDto {
        val app = appUserRepository.findByAuthUserId(authUserId)
            ?: throw NotFoundException("User profile not found")

        req.name?.let { app.name = it.trim() }
        req.gender?.let { app.gender = it.trim() }

        req.dateOfBirth?.let { dob ->
            if (dob.isAfter(LocalDate.now())) throw BadRequestException("dateOfBirth cannot be in the future")
            app.dateOfBirth = dob
        }

        req.currentWeightKg?.let { app.currentWeightKg = it }
        req.heightCm?.let { app.heightCm = it }
        req.dailyCalorieGoalKcal?.let { app.dailyCalorieGoalKcal = it }

        req.activityLevel?.let { lvlStr ->
            val lvl = ActivityLevel.fromString(lvlStr)
                ?: throw BadRequestException("Invalid activityLevel")
            app.activityLevel = lvl.name
        }

        req.timezone?.let { tz ->
            val trimmed = tz.trim()
            if (trimmed.isNotEmpty()) {
                try { ZoneId.of(trimmed) } catch (ex: Exception) { throw BadRequestException("Invalid timezone: $trimmed") }
                app.timezone = trimmed
            }
        }

        app.updatedAt = OffsetDateTime.now()
        appUserRepository.save(app)
        return buildDto(app)
    }

    private fun buildDto(app: AppUser): UserProfileDto {
        val bmi = HealthCalculator.bmi(app.currentWeightKg, app.heightCm)
        val bmr = HealthCalculator.bmrMifflinStJeor(app.gender, app.dateOfBirth, app.currentWeightKg, app.heightCm)
        val tdee = HealthCalculator.estimatedDailyIntake(bmr, app.activityLevel)
        return UserProfileDto(
            id = app.id.toString(),
            authUserId = app.authUserId.toString(),
            name = app.name,
            gender = app.gender,
            dateOfBirth = app.dateOfBirth,
            currentWeightKg = app.currentWeightKg,
            heightCm = app.heightCm,
            activityLevel = app.activityLevel,
            dailyCalorieGoalKcal = app.dailyCalorieGoalKcal,
            timezone = app.timezone,
            bmi = bmi,
            estimatedDailyIntakeKcal = tdee
        )
    }
}
