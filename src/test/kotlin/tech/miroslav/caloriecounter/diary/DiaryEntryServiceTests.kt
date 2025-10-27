package tech.miroslav.caloriecounter.diary

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
import tech.miroslav.caloriecounter.food.Food
import tech.miroslav.caloriecounter.food.FoodRepository
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class DiaryEntryServiceTests {

    @Mock lateinit var diaryRepository: DiaryRepository
    @Mock lateinit var diaryEntryRepository: DiaryEntryRepository
    @Mock lateinit var foodRepository: FoodRepository

    @InjectMocks lateinit var service: DiaryEntryService

    @Captor lateinit var entryCaptor: ArgumentCaptor<DiaryEntry>

    private fun stubDiary(ownerId: UUID, diaryId: UUID): Diary {
        val d = Diary(id = diaryId, ownerId = ownerId, date = LocalDate.now())
        `when`(diaryRepository.findByIdAndOwnerIdAndDeletedAtIsNull(diaryId, ownerId)).thenReturn(d)
        return d
    }

    @Test
    fun `list requires diary ownership and returns dtos`() {
        val ownerId = UUID.randomUUID(); val diaryId = UUID.randomUUID();
        stubDiary(ownerId, diaryId)
        val e = DiaryEntry(id = UUID.randomUUID(), diaryId = diaryId, foodId = UUID.randomUUID(), amount = BigDecimal("100"), unit = "g", meal = "breakfast")
        `when`(diaryEntryRepository.findByDiaryIdOrderByCreatedAtAsc(diaryId)).thenReturn(listOf(e))

        val list = service.list(ownerId, diaryId)
        assertThat(list).hasSize(1)
        assertThat(list[0].id).isEqualTo(e.id.toString())
        assertThat(list[0].unit).isEqualTo("g")
    }

    @Test
    fun `create validates unit meal and food existence`() {
        val ownerId = UUID.randomUUID(); val diaryId = UUID.randomUUID();
        stubDiary(ownerId, diaryId)
        // invalid unit
        assertThatThrownBy {
            service.create(ownerId, diaryId, CreateDiaryEntryRequest(foodId = UUID.randomUUID().toString(), amount = BigDecimal("10"), unit = "invalid", meal = "breakfast"))
        }.isInstanceOf(BadRequestException::class.java)

        // invalid meal
        assertThatThrownBy {
            service.create(ownerId, diaryId, CreateDiaryEntryRequest(foodId = UUID.randomUUID().toString(), amount = BigDecimal("10"), unit = "g", meal = "invalid"))
        }.isInstanceOf(BadRequestException::class.java)

        // food not found
        val fid = UUID.randomUUID()
        `when`(foodRepository.findById(fid)).thenReturn(Optional.empty())
        assertThatThrownBy {
            service.create(ownerId, diaryId, CreateDiaryEntryRequest(foodId = fid.toString(), amount = BigDecimal("10"), unit = "g", meal = "breakfast"))
        }.isInstanceOf(NotFoundException::class.java)
    }

    @Test
    fun `create rejects soft-deleted food`() {
        val ownerId = UUID.randomUUID(); val diaryId = UUID.randomUUID();
        stubDiary(ownerId, diaryId)
        val fid = UUID.randomUUID()
        val food = Food(id = fid, ownerId = ownerId, deletedAt = OffsetDateTime.now())
        `when`(foodRepository.findById(fid)).thenReturn(Optional.of(food))
        assertThatThrownBy {
            service.create(ownerId, diaryId, CreateDiaryEntryRequest(foodId = fid.toString(), amount = BigDecimal("10"), unit = "g", meal = "breakfast"))
        }.isInstanceOf(BadRequestException::class.java)
    }

    @Test
    fun `create success saves entry`() {
        val ownerId = UUID.randomUUID(); val diaryId = UUID.randomUUID();
        stubDiary(ownerId, diaryId)
        val fid = UUID.randomUUID()
        val food = Food(id = fid, ownerId = ownerId, deletedAt = null)
        `when`(foodRepository.findById(fid)).thenReturn(Optional.of(food))
        val saved = DiaryEntry(id = UUID.randomUUID(), diaryId = diaryId, foodId = fid, amount = BigDecimal("50"), unit = "g", meal = "lunch")
        `when`(diaryEntryRepository.save(Mockito.any())).thenReturn(saved)

        val dto = service.create(ownerId, diaryId, CreateDiaryEntryRequest(foodId = fid.toString(), amount = BigDecimal("50"), unit = "g", meal = "lunch"))
        assertThat(dto.id).isEqualTo(saved.id.toString())
        assertThat(dto.foodId).isEqualTo(fid.toString())
        assertThat(dto.amount).isEqualTo(BigDecimal("50"))
    }

    @Test
    fun `get enforces ownership and returns dto`() {
        val ownerId = UUID.randomUUID(); val diaryId = UUID.randomUUID(); val entryId = UUID.randomUUID()
        stubDiary(ownerId, diaryId)
        val e = DiaryEntry(id = entryId, diaryId = diaryId, foodId = UUID.randomUUID(), amount = BigDecimal("1"), unit = "item", meal = "dinner")
        `when`(diaryEntryRepository.findByIdAndDiaryId(entryId, diaryId)).thenReturn(e)
        val dto = service.get(ownerId, diaryId, entryId)
        assertThat(dto.id).isEqualTo(entryId.toString())
    }

    @Test
    fun `update validates fields and saves`() {
        val ownerId = UUID.randomUUID(); val diaryId = UUID.randomUUID(); val entryId = UUID.randomUUID()
        stubDiary(ownerId, diaryId)
        val e = DiaryEntry(id = entryId, diaryId = diaryId, foodId = UUID.randomUUID(), amount = BigDecimal("100"), unit = "g", meal = "breakfast")
        `when`(diaryEntryRepository.findByIdAndDiaryId(entryId, diaryId)).thenReturn(e)

        // invalid unit
        assertThatThrownBy { service.update(ownerId, diaryId, entryId, UpdateDiaryEntryRequest(unit = "bad")) }
            .isInstanceOf(BadRequestException::class.java)

        // change food to deleted
        val fid = UUID.randomUUID()
        val food = Food(id = fid, ownerId = ownerId, deletedAt = OffsetDateTime.now())
        `when`(foodRepository.findById(fid)).thenReturn(Optional.of(food))
        assertThatThrownBy { service.update(ownerId, diaryId, entryId, UpdateDiaryEntryRequest(foodId = fid.toString())) }
            .isInstanceOf(BadRequestException::class.java)

        // happy path adjust amount and meal
        `when`(diaryEntryRepository.save(Mockito.any())).thenAnswer { it.arguments[0] }
        val dto = service.update(ownerId, diaryId, entryId, UpdateDiaryEntryRequest(amount = BigDecimal("200"), unit = "ml", meal = "snack"))
        assertThat(dto.amount).isEqualTo(BigDecimal("200"))
        assertThat(dto.unit).isEqualTo("ml")
        assertThat(dto.meal).isEqualTo("snack")
    }

    @Test
    fun `delete enforces ownership`() {
        val ownerId = UUID.randomUUID(); val diaryId = UUID.randomUUID(); val entryId = UUID.randomUUID()
        stubDiary(ownerId, diaryId)
        `when`(diaryEntryRepository.findByIdAndDiaryId(entryId, diaryId)).thenReturn(null)
        assertThatThrownBy { service.delete(ownerId, diaryId, entryId) }
            .isInstanceOf(NotFoundException::class.java)
    }
}
