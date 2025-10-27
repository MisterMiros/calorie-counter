package tech.miroslav.caloriecounter.training

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
class TrainingLogService(
    private val trainingLogRepository: TrainingLogRepository,
    private val trainingLogEntryRepository: TrainingLogEntryRepository
) {
    private val pageSize: Int = 50

    @Transactional(readOnly = true)
    fun list(ownerId: UUID, date: LocalDate?, page: Int): PageResponse<TrainingLogDto> {
        val pageable = PageRequest.of(page.coerceAtLeast(0), pageSize, Sort.by(Sort.Direction.DESC, "date"))
        val pg = if (date != null) {
            trainingLogRepository.findByOwnerIdAndDateAndDeletedAtIsNull(ownerId, date, pageable)
        } else {
            trainingLogRepository.findByOwnerIdAndDeletedAtIsNull(ownerId, pageable)
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
    fun create(ownerId: UUID, req: CreateTrainingLogRequest): TrainingLogDto {
        val date = req.date ?: throw tech.miroslav.caloriecounter.common.BadRequestException("date is required")
        if (trainingLogRepository.existsByOwnerIdAndDateAndDeletedAtIsNull(ownerId, date)) {
            throw ConflictException("Training log for this date already exists")
        }
        val now = OffsetDateTime.now()
        val entity = TrainingLog(
            ownerId = ownerId,
            date = date,
            comment = req.comment,
            createdAt = now,
            updatedAt = now
        )
        val saved = trainingLogRepository.save(entity)
        return saved.toDto()
    }

    @Transactional(readOnly = true)
    fun get(ownerId: UUID, id: UUID): TrainingLogDto {
        val log = trainingLogRepository.findByIdAndOwnerIdAndDeletedAtIsNull(id, ownerId)
            ?: throw NotFoundException("Training log not found")
        return log.toDto()
    }

    @Transactional
    fun update(ownerId: UUID, id: UUID, req: UpdateTrainingLogRequest): TrainingLogDto {
        val log = trainingLogRepository.findByIdAndOwnerIdAndDeletedAtIsNull(id, ownerId)
            ?: throw NotFoundException("Training log not found")
        req.date?.let { newDate ->
            if (newDate != log.date) {
                if (trainingLogRepository.existsByOwnerIdAndDateAndDeletedAtIsNull(ownerId, newDate)) {
                    throw ConflictException("Training log for this date already exists")
                }
                log.date = newDate
            }
        }
        req.comment?.let { log.comment = it }
        log.updatedAt = OffsetDateTime.now()
        val saved = trainingLogRepository.save(log)
        return saved.toDto()
    }

    @Transactional
    fun delete(ownerId: UUID, id: UUID) {
        val log = trainingLogRepository.findByIdAndOwnerIdAndDeletedAtIsNull(id, ownerId)
            ?: throw NotFoundException("Training log not found")
        val deletedAtTs = OffsetDateTime.now()
        log.deletedAt = deletedAtTs
        log.updatedAt = deletedAtTs
        trainingLogRepository.save(log)
        trainingLogEntryRepository.deleteByTrainingLogId(log.id)
    }
}

private fun TrainingLog.toDto() = TrainingLogDto(
    id = this.id.toString(),
    date = this.date,
    comment = this.comment,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt
)
