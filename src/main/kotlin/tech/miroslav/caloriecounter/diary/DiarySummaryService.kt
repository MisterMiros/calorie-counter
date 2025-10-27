package tech.miroslav.caloriecounter.diary

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tech.miroslav.caloriecounter.common.BadRequestException
import tech.miroslav.caloriecounter.common.HealthCalculator
import tech.miroslav.caloriecounter.common.NotFoundException
import tech.miroslav.caloriecounter.food.Food
import tech.miroslav.caloriecounter.food.FoodMacros
import tech.miroslav.caloriecounter.food.FoodMacrosRepository
import tech.miroslav.caloriecounter.food.FoodRepository
import tech.miroslav.caloriecounter.food.FoodTranslationRepository
import tech.miroslav.caloriecounter.user.AppUserRepository
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

@Service
class DiarySummaryService(
    private val diaryRepository: DiaryRepository,
    private val diaryEntryRepository: DiaryEntryRepository,
    private val foodRepository: FoodRepository,
    private val foodMacrosRepository: FoodMacrosRepository,
    private val foodTranslationRepository: FoodTranslationRepository,
    private val appUserRepository: AppUserRepository
) {
    private val HUNDRED = BigDecimal("100")
    private val CUP_ML = BigDecimal("240")
    private val TABLESPOON_ML = BigDecimal("15")
    private val TEASPOON_ML = BigDecimal("5")

    @Transactional(readOnly = true)
    fun summarize(ownerId: UUID, diaryId: UUID): DiarySummaryDto {
        val diary = diaryRepository.findByIdAndOwnerIdAndDeletedAtIsNull(diaryId, ownerId)
            ?: throw NotFoundException("Diary not found")
        val entries = diaryEntryRepository.findByDiaryIdOrderByCreatedAtAsc(diary.id)

        // Resolve foods and macros
        val foodById = HashMap<UUID, Food>()
        val macrosByFoodId = HashMap<UUID, FoodMacros>()
        entries.map { it.foodId }.distinct().forEach { fid ->
            val f = foodRepository.findById(fid).orElseThrow { NotFoundException("Food not found: $fid") }
            foodById[fid] = f
            val m = foodMacrosRepository.findById(fid).orElseThrow { NotFoundException("Food macros not found: $fid") }
            macrosByFoodId[fid] = m
        }

        val entrySummaries = entries.map { e ->
            val food = foodById[e.foodId]!!
            val m = macrosByFoodId[e.foodId]!!
            val grams = gramsFor(e.amount, e.unit, food)
            val macros = macrosFor(grams, m)
            val kcal = kcalFor(macros)
            val name = foodTranslationRepository.findFirstByFoodId(food.id)?.name
            DiaryEntrySummaryDto(
                id = e.id.toString(),
                foodId = food.id.toString(),
                foodName = name,
                amount = e.amount,
                unit = e.unit,
                grams = grams,
                kcal = kcal,
                macros = macros
            )
        }

        // Aggregate by meal
        val meals = entrySummaries.groupBy { entries.find { e -> e.id.toString() == it.id }!!.meal }
            .map { (mealName, items) ->
                val mealMacros = items.fold(MacrosDto(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO)) { acc, it2 ->
                    MacrosDto(
                        proteinG = acc.proteinG + it2.macros.proteinG,
                        fatG = acc.fatG + it2.macros.fatG,
                        carbG = acc.carbG + it2.macros.carbG
                    )
                }
                val mealKcal = items.fold(BigDecimal.ZERO) { acc, it2 -> acc + it2.kcal }.setScale(2, RoundingMode.HALF_UP)
                MealSummaryDto(
                    meal = mealName,
                    totalKcal = mealKcal,
                    percentOfDailyGoal = null, // filled later after we know goal
                    percentOfEstimatedIntake = null,
                    macros = roundMacros(mealMacros),
                    entries = items
                )
            }

        val totalMacros = meals.fold(MacrosDto(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO)) { acc, meal ->
            MacrosDto(
                proteinG = acc.proteinG + meal.macros.proteinG,
                fatG = acc.fatG + meal.macros.fatG,
                carbG = acc.carbG + meal.macros.carbG
            )
        }
        val totalKcal = meals.fold(BigDecimal.ZERO) { acc, meal -> acc + meal.totalKcal }.setScale(2, RoundingMode.HALF_UP)

        // Compute percentages based on user profile
        val app = appUserRepository.findByAuthUserId(ownerId)
            ?: throw NotFoundException("User profile not found")
        val bmr = HealthCalculator.bmrMifflinStJeor(app.gender, app.dateOfBirth, app.currentWeightKg, app.heightCm)
        val tdee = HealthCalculator.estimatedDailyIntake(bmr, app.activityLevel)
        val goal = app.dailyCalorieGoalKcal

        fun ratioOrNull(numerator: BigDecimal, denominator: BigDecimal?): BigDecimal? {
            if (denominator == null) return null
            if (denominator.compareTo(BigDecimal.ZERO) <= 0) return null
            return numerator.divide(denominator, 6, RoundingMode.HALF_UP).setScale(4, RoundingMode.HALF_UP)
        }

        val percentGoal = ratioOrNull(totalKcal, goal)
        val percentTdee = ratioOrNull(totalKcal, tdee)

        val mealsWithPercents = meals.map { m ->
            m.copy(
                percentOfDailyGoal = ratioOrNull(m.totalKcal, goal),
                percentOfEstimatedIntake = ratioOrNull(m.totalKcal, tdee)
            )
        }

        return DiarySummaryDto(
            diaryId = diary.id.toString(),
            ownerId = diary.ownerId.toString(),
            date = diary.date,
            totalKcal = totalKcal,
            percentOfDailyGoal = percentGoal,
            percentOfEstimatedIntake = percentTdee,
            macros = roundMacros(totalMacros),
            meals = mealsWithPercents
        )
    }

    private fun gramsFor(amount: BigDecimal, unit: String, food: Food): BigDecimal {
        return when (unit) {
            "g" -> amount
            "ml" -> {
                val density = food.densityGPerMl ?: throw BadRequestException("Food does not support ml unit (no density)")
                amount.multiply(density)
            }
            "cup" -> {
                val density = food.densityGPerMl ?: throw BadRequestException("Food does not support cup unit (no density)")
                amount.multiply(CUP_ML).multiply(density)
            }
            "tablespoon" -> {
                val density = food.densityGPerMl ?: throw BadRequestException("Food does not support tablespoon unit (no density)")
                amount.multiply(TABLESPOON_ML).multiply(density)
            }
            "teaspoon" -> {
                val density = food.densityGPerMl ?: throw BadRequestException("Food does not support teaspoon unit (no density)")
                amount.multiply(TEASPOON_ML).multiply(density)
            }
            "pack" -> {
                val packG = food.packG ?: throw BadRequestException("Food does not support pack unit")
                amount.multiply(packG)
            }
            "item" -> {
                val itemG = food.itemG ?: throw BadRequestException("Food does not support item unit")
                amount.multiply(itemG)
            }
            else -> throw BadRequestException("Unsupported unit: $unit")
        }.setScale(2, RoundingMode.HALF_UP)
    }

    private fun macrosFor(grams: BigDecimal, macros: FoodMacros): MacrosDto {
        val factor = grams.divide(HUNDRED, 12, RoundingMode.HALF_UP)
        val p = macros.proteinG.multiply(factor)
        val f = macros.fatG.multiply(factor)
        val c = macros.carbG.multiply(factor)
        return roundMacros(MacrosDto(p, f, c))
    }

    private fun roundMacros(m: MacrosDto): MacrosDto = MacrosDto(
        proteinG = m.proteinG.setScale(2, RoundingMode.HALF_UP),
        fatG = m.fatG.setScale(2, RoundingMode.HALF_UP),
        carbG = m.carbG.setScale(2, RoundingMode.HALF_UP)
    )

    private fun kcalFor(m: MacrosDto): BigDecimal {
        val kcal = m.proteinG.multiply(BigDecimal("4"))
            .add(m.fatG.multiply(BigDecimal("9")))
            .add(m.carbG.multiply(BigDecimal("4")))
        return kcal.setScale(2, RoundingMode.HALF_UP)
    }
}
