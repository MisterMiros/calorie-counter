package tech.miroslav.caloriecounter.diary

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tech.miroslav.caloriecounter.common.BadRequestException
import tech.miroslav.caloriecounter.common.NotFoundException
import tech.miroslav.caloriecounter.food.FoodRepository
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

@Service
class DiaryEntryService(
    private val diaryRepository: DiaryRepository,
    private val diaryEntryRepository: DiaryEntryRepository,
    private val foodRepository: FoodRepository
) {

    private val allowedUnits = setOf("g", "ml", "cup", "tablespoon", "teaspoon", "pack", "item")
    private val allowedMeals = setOf("breakfast", "lunch", "dinner", "snack")

    @Transactional(readOnly = true)
    fun list(ownerId: UUID, diaryId: UUID): List<DiaryEntryDto> {
        // Ensure diary belongs to owner and is not soft-deleted
        val diary = diaryRepository.findByIdAndOwnerIdAndDeletedAtIsNull(diaryId, ownerId)
            ?: throw NotFoundException("Diary not found")
        return diaryEntryRepository.findByDiaryIdOrderByCreatedAtAsc(diary.id)
            .map { it.toDto() }
    }

    @Transactional
    fun create(ownerId: UUID, diaryId: UUID, req: CreateDiaryEntryRequest): DiaryEntryDto {
        val diary = diaryRepository.findByIdAndOwnerIdAndDeletedAtIsNull(diaryId, ownerId)
            ?: throw NotFoundException("Diary not found")

        val foodId = parseUuid(req.foodId, "foodId")
        validateAmount(req.amount)
        val unit = req.unit?.trim() ?: throw BadRequestException("unit is required")
        if (!allowedUnits.contains(unit)) throw BadRequestException("Invalid unit")
        val meal = req.meal?.trim() ?: throw BadRequestException("meal is required")
        if (!allowedMeals.contains(meal)) throw BadRequestException("Invalid meal")

        val food = foodRepository.findById(foodId).orElseThrow { NotFoundException("Food not found") }
        if (food.deletedAt != null) throw BadRequestException("Cannot reference a deleted food")

        val now = OffsetDateTime.now()
        val entity = DiaryEntry(
            diaryId = diary.id,
            foodId = food.id,
            amount = req.amount!!,
            unit = unit,
            meal = meal,
            comment = req.comment,
            createdAt = now,
            updatedAt = now
        )
        val saved = diaryEntryRepository.save(entity)
        return saved.toDto()
    }

    @Transactional(readOnly = true)
    fun get(ownerId: UUID, diaryId: UUID, entryId: UUID): DiaryEntryDto {
        ensureDiaryOwnership(ownerId, diaryId)
        val entry = diaryEntryRepository.findByIdAndDiaryId(entryId, diaryId)
            ?: throw NotFoundException("Diary entry not found")
        return entry.toDto()
    }

    @Transactional
    fun update(ownerId: UUID, diaryId: UUID, entryId: UUID, req: UpdateDiaryEntryRequest): DiaryEntryDto {
        ensureDiaryOwnership(ownerId, diaryId)
        val entry = diaryEntryRepository.findByIdAndDiaryId(entryId, diaryId)
            ?: throw NotFoundException("Diary entry not found")

        req.foodId?.let {
            val fid = parseUuid(it, "foodId")
            val food = foodRepository.findById(fid).orElseThrow { NotFoundException("Food not found") }
            if (food.deletedAt != null) throw BadRequestException("Cannot reference a deleted food")
            entry.foodId = food.id
        }
        req.amount?.let { validateAmount(it); entry.amount = it }
        req.unit?.let {
            if (!allowedUnits.contains(it)) throw BadRequestException("Invalid unit")
            entry.unit = it
        }
        req.meal?.let {
            if (!allowedMeals.contains(it)) throw BadRequestException("Invalid meal")
            entry.meal = it
        }
        req.comment?.let { entry.comment = it }

        entry.updatedAt = OffsetDateTime.now()
        val saved = diaryEntryRepository.save(entry)
        return saved.toDto()
    }

    @Transactional
    fun delete(ownerId: UUID, diaryId: UUID, entryId: UUID) {
        ensureDiaryOwnership(ownerId, diaryId)
        val entry = diaryEntryRepository.findByIdAndDiaryId(entryId, diaryId)
            ?: throw NotFoundException("Diary entry not found")
        diaryEntryRepository.delete(entry)
    }

    private fun ensureDiaryOwnership(ownerId: UUID, diaryId: UUID) {
        val exists = diaryRepository.findByIdAndOwnerIdAndDeletedAtIsNull(diaryId, ownerId) != null
        if (!exists) throw NotFoundException("Diary not found")
    }

    private fun parseUuid(value: String?, field: String): UUID {
        try {
            return UUID.fromString(value)
        } catch (ex: Exception) {
            throw BadRequestException("Invalid $field")
        }
    }

    private fun validateAmount(amount: BigDecimal?) {
        if (amount == null) throw BadRequestException("amount is required")
        if (amount <= BigDecimal.ZERO) throw BadRequestException("amount must be > 0")
    }
}

private fun DiaryEntry.toDto() = DiaryEntryDto(
    id = this.id.toString(),
    foodId = this.foodId.toString(),
    amount = this.amount,
    unit = this.unit,
    meal = this.meal,
    comment = this.comment,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt
)
