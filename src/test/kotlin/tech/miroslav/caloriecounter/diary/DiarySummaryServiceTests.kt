package tech.miroslav.caloriecounter.diary

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import tech.miroslav.caloriecounter.common.BadRequestException
import tech.miroslav.caloriecounter.food.Food
import tech.miroslav.caloriecounter.food.FoodMacros
import tech.miroslav.caloriecounter.food.FoodMacrosRepository
import tech.miroslav.caloriecounter.food.FoodRepository
import tech.miroslav.caloriecounter.food.FoodTranslation
import tech.miroslav.caloriecounter.food.FoodTranslationRepository
import tech.miroslav.caloriecounter.user.AppUser
import tech.miroslav.caloriecounter.user.AppUserRepository
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

@ExtendWith(MockitoExtension::class)
class DiarySummaryServiceTests {

    @Mock lateinit var diaryRepository: DiaryRepository
    @Mock lateinit var diaryEntryRepository: DiaryEntryRepository
    @Mock lateinit var foodRepository: FoodRepository
    @Mock lateinit var foodMacrosRepository: FoodMacrosRepository
    @Mock lateinit var foodTranslationRepository: FoodTranslationRepository
    @Mock lateinit var appUserRepository: AppUserRepository

    @InjectMocks lateinit var service: DiarySummaryService

    private fun setupUser(ownerId: UUID, goal: BigDecimal = BigDecimal("690")) {
        val app = AppUser(
            id = UUID.randomUUID(),
            authUserId = ownerId,
            name = "Alice",
            gender = "female",
            dateOfBirth = LocalDate.now().minusYears(25),
            currentWeightKg = BigDecimal("60"),
            heightCm = BigDecimal("165"),
            activityLevel = "SEDENTARY",
            dailyCalorieGoalKcal = goal,
            timezone = "UTC"
        )
        `when`(appUserRepository.findByAuthUserId(ownerId)).thenReturn(app)
    }

    @Test
    fun `summarize computes grams kcal and percentages`() {
        val ownerId = UUID.randomUUID()
        val diaryId = UUID.randomUUID()
        val diary = Diary(id = diaryId, ownerId = ownerId, date = LocalDate.of(2025, 10, 27))
        `when`(diaryRepository.findByIdAndOwnerIdAndDeletedAtIsNull(diaryId, ownerId)).thenReturn(diary)

        val food1Id = UUID.randomUUID()
        val entry1 = DiaryEntry(id = UUID.randomUUID(), diaryId = diaryId, foodId = food1Id, amount = BigDecimal("100"), unit = "g", meal = "breakfast")
        val food1 = Food(id = food1Id, ownerId = ownerId)
        val macros1 = FoodMacros(foodId = food1Id, proteinG = BigDecimal("10"), fatG = BigDecimal("5"), carbG = BigDecimal("20"))
        val tr1 = FoodTranslation(id = UUID.randomUUID(), foodId = food1Id, locale = "en", name = "Food1")

        val food2Id = UUID.randomUUID()
        val entry2 = DiaryEntry(id = UUID.randomUUID(), diaryId = diaryId, foodId = food2Id, amount = BigDecimal("1"), unit = "item", meal = "lunch")
        val food2 = Food(id = food2Id, ownerId = ownerId, itemG = BigDecimal("200"))
        val macros2 = FoodMacros(foodId = food2Id, proteinG = BigDecimal("0"), fatG = BigDecimal("10"), carbG = BigDecimal("0"))
        val tr2 = FoodTranslation(id = UUID.randomUUID(), foodId = food2Id, locale = "en", name = "Food2")

        `when`(diaryEntryRepository.findByDiaryIdOrderByCreatedAtAsc(diaryId)).thenReturn(listOf(entry1, entry2))
        `when`(foodRepository.findById(food1Id)).thenReturn(Optional.of(food1))
        `when`(foodRepository.findById(food2Id)).thenReturn(Optional.of(food2))
        `when`(foodMacrosRepository.findById(food1Id)).thenReturn(Optional.of(macros1))
        `when`(foodMacrosRepository.findById(food2Id)).thenReturn(Optional.of(macros2))
        `when`(foodTranslationRepository.findFirstByFoodId(food1Id)).thenReturn(tr1)
        `when`(foodTranslationRepository.findFirstByFoodId(food2Id)).thenReturn(tr2)

        setupUser(ownerId, goal = BigDecimal("690"))

        val summary = service.summarize(ownerId, diaryId)

        assertThat(summary.totalKcal).isEqualTo(BigDecimal("345.00")) // 165 + 180
        assertThat(summary.percentOfDailyGoal).isEqualTo(BigDecimal("0.5000"))
        assertThat(summary.macros.proteinG).isEqualTo(BigDecimal("10.00"))
        assertThat(summary.macros.fatG).isEqualTo(BigDecimal("25.00"))
        assertThat(summary.macros.carbG).isEqualTo(BigDecimal("20.00"))
        assertThat(summary.meals).hasSize(2)
        val breakfast = summary.meals.first { it.meal == "breakfast" }
        assertThat(breakfast.totalKcal).isEqualTo(BigDecimal("165.00"))
        assertThat(breakfast.entries[0].grams).isEqualTo(BigDecimal("100.00"))
        val lunch = summary.meals.first { it.meal == "lunch" }
        assertThat(lunch.entries[0].grams).isEqualTo(BigDecimal("200.00"))
    }

    @Test
    fun `ml conversions require density`() {
        val ownerId = UUID.randomUUID()
        val diaryId = UUID.randomUUID()
        val diary = Diary(id = diaryId, ownerId = ownerId, date = LocalDate.now())
        `when`(diaryRepository.findByIdAndOwnerIdAndDeletedAtIsNull(diaryId, ownerId)).thenReturn(diary)

        val foodId = UUID.randomUUID()
        val entry = DiaryEntry(id = UUID.randomUUID(), diaryId = diaryId, foodId = foodId, amount = BigDecimal("100"), unit = "ml", meal = "snack")
        val food = Food(id = foodId, ownerId = ownerId, densityGPerMl = null)
        val macros = FoodMacros(foodId = foodId, proteinG = BigDecimal("1"), fatG = BigDecimal("1"), carbG = BigDecimal("1"))

        `when`(diaryEntryRepository.findByDiaryIdOrderByCreatedAtAsc(diaryId)).thenReturn(listOf(entry))
        `when`(foodRepository.findById(foodId)).thenReturn(Optional.of(food))
        `when`(foodMacrosRepository.findById(foodId)).thenReturn(Optional.of(macros))
        // Do not stub user profile: service should fail before accessing it due to missing density
        assertThatThrownBy { service.summarize(ownerId, diaryId) }
            .isInstanceOf(BadRequestException::class.java)
    }

    @Test
    fun `volume measures convert via density`() {
        val ownerId = UUID.randomUUID()
        val diaryId = UUID.randomUUID()
        val diary = Diary(id = diaryId, ownerId = ownerId, date = LocalDate.now())
        `when`(diaryRepository.findByIdAndOwnerIdAndDeletedAtIsNull(diaryId, ownerId)).thenReturn(diary)

        val foodId = UUID.randomUUID()
        val density = BigDecimal("1.0") // water-like
        val food = Food(id = foodId, ownerId = ownerId, densityGPerMl = density)
        val macros = FoodMacros(foodId = foodId, proteinG = BigDecimal("0"), fatG = BigDecimal("0"), carbG = BigDecimal("10"))

        val eCup = DiaryEntry(id = UUID.randomUUID(), diaryId = diaryId, foodId = foodId, amount = BigDecimal("1"), unit = "cup", meal = "breakfast")
        val eTbsp = DiaryEntry(id = UUID.randomUUID(), diaryId = diaryId, foodId = foodId, amount = BigDecimal("2"), unit = "tablespoon", meal = "breakfast")
        val eTsp = DiaryEntry(id = UUID.randomUUID(), diaryId = diaryId, foodId = foodId, amount = BigDecimal("3"), unit = "teaspoon", meal = "breakfast")

        `when`(diaryEntryRepository.findByDiaryIdOrderByCreatedAtAsc(diaryId)).thenReturn(listOf(eCup, eTbsp, eTsp))
        `when`(foodRepository.findById(foodId)).thenReturn(Optional.of(food))
        `when`(foodMacrosRepository.findById(foodId)).thenReturn(Optional.of(macros))
        `when`(foodTranslationRepository.findFirstByFoodId(foodId)).thenReturn(null)
        setupUser(ownerId)

        val summary = service.summarize(ownerId, diaryId)
        val gramsList = summary.meals.first().entries.map { it.grams }
        assertThat(gramsList).containsExactly(
            BigDecimal("240.00"), // 1 cup * 240ml * 1 g/ml
            BigDecimal("30.00"),  // 2 tbsp * 15ml * 1 g/ml
            BigDecimal("15.00")   // 3 tsp * 5ml * 1 g/ml
        )
    }
}
