package tech.miroslav.caloriecounter.user

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tech.miroslav.caloriecounter.common.HealthCalculator
import tech.miroslav.caloriecounter.common.NotFoundException
import java.util.*

@Service
class UserProfileService(
    private val appUserRepository: AppUserRepository
) {
    @Transactional(readOnly = true)
    fun getMyProfile(authUserId: UUID): UserProfileDto {
        val app = appUserRepository.findByAuthUserId(authUserId)
            ?: throw NotFoundException("User profile not found")
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
