package tech.miroslav.caloriecounter.diary

import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tech.miroslav.caloriecounter.common.ConflictException
import tech.miroslav.caloriecounter.common.NotFoundException
import tech.miroslav.caloriecounter.common.PageResponse
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.*

@Service
class DiaryService(
    private val diaryRepository: DiaryRepository,
    private val diaryEntryRepository: DiaryEntryRepository
) {
    private val pageSize: Int = 50

    @Transactional(readOnly = true)
    fun list(ownerId: UUID, date: LocalDate?, page: Int): PageResponse<DiaryDto> {
        val pageable = PageRequest.of(page.coerceAtLeast(0), pageSize, Sort.by(Sort.Direction.DESC, "date"))
        val pg = if (date != null) {
            diaryRepository.findByOwnerIdAndDateAndDeletedAtIsNull(ownerId, date, pageable)
        } else {
            diaryRepository.findByOwnerIdAndDeletedAtIsNull(ownerId, pageable)
        }
        return PageResponse(
            content = pg.content.map { it.toDto() },
            page = pg.number,
            size = pg.size,
            totalElements = pg.totalElements,
            totalPages = pg.totalPages
        )
    }

    @Transactional
    fun create(ownerId: UUID, req: CreateDiaryRequest): DiaryDto {
        val date = req.date ?: throw tech.miroslav.caloriecounter.common.BadRequestException("date is required")
        if (diaryRepository.existsByOwnerIdAndDateAndDeletedAtIsNull(ownerId, date)) {
            throw ConflictException("Diary for this date already exists")
        }
        val now = OffsetDateTime.now()
        val entity = Diary(
            ownerId = ownerId,
            date = date,
            comment = req.comment,
            createdAt = now,
            updatedAt = now
        )
        val saved = diaryRepository.save(entity)
        return saved.toDto()
    }

    @Transactional(readOnly = true)
    fun get(ownerId: UUID, id: UUID): DiaryDto {
        val diary = diaryRepository.findByIdAndOwnerIdAndDeletedAtIsNull(id, ownerId)
            ?: throw NotFoundException("Diary not found")
        return diary.toDto()
    }

    @Transactional
    fun update(ownerId: UUID, id: UUID, req: UpdateDiaryRequest): DiaryDto {
        val diary = diaryRepository.findByIdAndOwnerIdAndDeletedAtIsNull(id, ownerId)
            ?: throw NotFoundException("Diary not found")

        req.date?.let { newDate ->
            if (newDate != diary.date) {
                if (diaryRepository.existsByOwnerIdAndDateAndDeletedAtIsNull(ownerId, newDate)) {
                    throw ConflictException("Diary for this date already exists")
                }
                diary.date = newDate
            }
        }
        req.comment?.let { diary.comment = it }
        diary.updatedAt = OffsetDateTime.now()
        val saved = diaryRepository.save(diary)
        return saved.toDto()
    }

    @Transactional
    fun delete(ownerId: UUID, id: UUID) {
        val diary = diaryRepository.findByIdAndOwnerIdAndDeletedAtIsNull(id, ownerId)
            ?: throw NotFoundException("Diary not found")
        // soft-delete diary, hard-delete its entries
        val deletedAtTs = OffsetDateTime.now()
        diary.deletedAt = deletedAtTs
        diary.updatedAt = deletedAtTs
        diaryRepository.save(diary)
        diaryEntryRepository.deleteByDiaryId(diary.id)
    }
}

private fun Diary.toDto() = DiaryDto(
    id = this.id.toString(),
    date = this.date,
    comment = this.comment,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt
)
