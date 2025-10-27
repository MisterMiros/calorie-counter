package tech.miroslav.caloriecounter.food

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import tech.miroslav.caloriecounter.common.BadRequestException
import tech.miroslav.caloriecounter.common.NotFoundException
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class FoodServiceTests {

    @Mock lateinit var foodRepository: FoodRepository
    @Mock lateinit var foodMacrosRepository: FoodMacrosRepository
    @Mock lateinit var foodTranslationRepository: FoodTranslationRepository

    @InjectMocks lateinit var service: FoodService

    @Captor lateinit var foodCaptor: ArgumentCaptor<Food>

    @Test
    fun `create food saves entity macros and translations`() {
        val ownerId = UUID.randomUUID()
        val req = CreateFoodRequest(
            type = "ingredient",
            densityGPerMl = BigDecimal("1.0"),
            packG = BigDecimal("500"),
            itemG = BigDecimal("100"),
            translations = listOf(FoodTranslationPayload(locale = "en", name = "Apple", producer = null)),
            proteinGPer100g = BigDecimal("0.3"),
            fatGPer100g = BigDecimal("0.2"),
            carbGPer100g = BigDecimal("14")
        )

        val savedFood = Food(id = UUID.randomUUID(), ownerId = ownerId, type = req.type)
        `when`(foodRepository.save(Mockito.any(Food::class.java))).thenReturn(savedFood)
        `when`(foodTranslationRepository.saveAll(Mockito.anyList())).thenAnswer { it.arguments[0] }
        `when`(foodMacrosRepository.save(Mockito.any(FoodMacros::class.java))).thenAnswer { it.arguments[0] }

        val dto = service.create(ownerId, req)
        assertThat(dto.id).isEqualTo(savedFood.id.toString())
        assertThat(dto.translations).hasSize(1)
        assertThat(dto.proteinGPer100g).isEqualTo(BigDecimal("0.3"))
    }

    @Test
    fun `get throws not found when not owner`() {
        val ownerId = UUID.randomUUID()
        val otherOwner = UUID.randomUUID()
        val id = UUID.randomUUID()
        val food = Food(id = id, ownerId = otherOwner, type = "ingredient")
        `when`(foodRepository.findById(id)).thenReturn(Optional.of(food))

        assertThatThrownBy { service.get(ownerId, id) }.isInstanceOf(NotFoundException::class.java)
    }

    @Test
    fun `update forbidden for deleted food`() {
        val ownerId = UUID.randomUUID(); val id = UUID.randomUUID()
        val food = Food(id = id, ownerId = ownerId, type = "ingredient", deletedAt = OffsetDateTime.now())
        `when`(foodRepository.findById(id)).thenReturn(Optional.of(food))
        assertThatThrownBy { service.update(ownerId, id, UpdateFoodRequest(type = "product")) }
            .isInstanceOf(BadRequestException::class.java)
    }

    @Test
    fun `update replaces translations when provided`() {
        val ownerId = UUID.randomUUID(); val id = UUID.randomUUID()
        val food = Food(id = id, ownerId = ownerId, type = "ingredient")
        val macros = FoodMacros(foodId = id, proteinG = BigDecimal("1"), fatG = BigDecimal("2"), carbG = BigDecimal("3"))
        `when`(foodRepository.findById(id)).thenReturn(Optional.of(food))
        `when`(foodRepository.save(Mockito.any(Food::class.java))).thenAnswer { it.arguments[0] }
        `when`(foodMacrosRepository.findById(id)).thenReturn(Optional.of(macros))
        `when`(foodMacrosRepository.save(Mockito.any(FoodMacros::class.java))).thenAnswer { it.arguments[0] }
        `when`(foodTranslationRepository.findByFoodId(id)).thenReturn(listOf(FoodTranslation(id = UUID.randomUUID(), foodId = id, locale = "en", name = "Old")))
        `when`(foodTranslationRepository.saveAll(Mockito.anyList())).thenAnswer { it.arguments[0] }

        val req = UpdateFoodRequest(
            translations = listOf(
                FoodTranslationPayload(locale = "en", name = "NewName"),
                FoodTranslationPayload(locale = "ru", name = "Новое имя")
            )
        )
        val dto = service.update(ownerId, id, req)
        assertThat(dto.translations).hasSize(2)
        assertThat(dto.translations[0].name).isIn("NewName", "Новое имя")
    }

    @Test
    fun `delete soft sets deletedAt`() {
        val ownerId = UUID.randomUUID(); val id = UUID.randomUUID()
        val food = Food(id = id, ownerId = ownerId, type = "ingredient", deletedAt = null)
        `when`(foodRepository.findById(id)).thenReturn(Optional.of(food))
        `when`(foodRepository.save(Mockito.any(Food::class.java))).thenAnswer { it.arguments[0] }

        service.delete(ownerId, id)
        assertThat(food.deletedAt).isNotNull()
    }
}
