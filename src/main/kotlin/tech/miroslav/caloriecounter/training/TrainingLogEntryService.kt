package tech.miroslav.caloriecounter.training

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tech.miroslav.caloriecounter.common.BadRequestException
import tech.miroslav.caloriecounter.common.NotFoundException
import tech.miroslav.caloriecounter.exercise.ExerciseRepository
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

@Service
class TrainingLogEntryService(
    private val trainingLogRepository: TrainingLogRepository,
    private val trainingLogEntryRepository: TrainingLogEntryRepository,
    private val exerciseRepository: ExerciseRepository
) {

    @Transactional(readOnly = true)
    fun list(ownerId: UUID, trainingLogId: UUID): List<TrainingLogEntryDto> {
        val log = trainingLogRepository.findByIdAndOwnerIdAndDeletedAtIsNull(trainingLogId, ownerId)
            ?: throw NotFoundException("Training log not found")
        return trainingLogEntryRepository.findByTrainingLogIdOrderByCreatedAtAsc(log.id)
            .map { it.toDto() }
    }

    @Transactional
    fun create(ownerId: UUID, trainingLogId: UUID, req: CreateTrainingLogEntryRequest): TrainingLogEntryDto {
        val log = trainingLogRepository.findByIdAndOwnerIdAndDeletedAtIsNull(trainingLogId, ownerId)
            ?: throw NotFoundException("Training log not found")

        val exerciseId = parseUuid(req.exerciseId, "exerciseId")
        val exercise = exerciseRepository.findById(exerciseId).orElseThrow { NotFoundException("Exercise not found") }
        if (exercise.deletedAt != null) throw BadRequestException("Cannot reference a deleted exercise")

        validateFields(req.durationMin, req.repetitions, req.weightKg)

        val now = OffsetDateTime.now()
        val entity = TrainingLogEntry(
            trainingLogId = log.id,
            exerciseId = exercise.id,
            durationMin = req.durationMin,
            repetitions = req.repetitions,
            weightKg = req.weightKg,
            createdAt = now,
            updatedAt = now
        )
        val saved = trainingLogEntryRepository.save(entity)
        return saved.toDto()
    }

    @Transactional(readOnly = true)
    fun get(ownerId: UUID, trainingLogId: UUID, entryId: UUID): TrainingLogEntryDto {
        ensureLogOwnership(ownerId, trainingLogId)
        val entry = trainingLogEntryRepository.findByIdAndTrainingLogId(entryId, trainingLogId)
            ?: throw NotFoundException("Training log entry not found")
        return entry.toDto()
    }

    @Transactional
    fun update(ownerId: UUID, trainingLogId: UUID, entryId: UUID, req: UpdateTrainingLogEntryRequest): TrainingLogEntryDto {
        ensureLogOwnership(ownerId, trainingLogId)
        val entry = trainingLogEntryRepository.findByIdAndTrainingLogId(entryId, trainingLogId)
            ?: throw NotFoundException("Training log entry not found")

        req.exerciseId?.let {
            val exId = parseUuid(it, "exerciseId")
            val ex = exerciseRepository.findById(exId).orElseThrow { NotFoundException("Exercise not found") }
            if (ex.deletedAt != null) throw BadRequestException("Cannot reference a deleted exercise")
            entry.exerciseId = ex.id
        }
        if (req.durationMin != null || req.repetitions != null || req.weightKg != null) {
            validatePartial(req.durationMin, req.repetitions, req.weightKg)
        }
        req.durationMin?.let { entry.durationMin = it }
        req.repetitions?.let { entry.repetitions = it }
        req.weightKg?.let { entry.weightKg = it }

        entry.updatedAt = OffsetDateTime.now()
        val saved = trainingLogEntryRepository.save(entry)
        return saved.toDto()
    }

    @Transactional
    fun delete(ownerId: UUID, trainingLogId: UUID, entryId: UUID) {
        ensureLogOwnership(ownerId, trainingLogId)
        val entry = trainingLogEntryRepository.findByIdAndTrainingLogId(entryId, trainingLogId)
            ?: throw NotFoundException("Training log entry not found")
        trainingLogEntryRepository.delete(entry)
    }

    private fun ensureLogOwnership(ownerId: UUID, trainingLogId: UUID) {
        val exists = trainingLogRepository.findByIdAndOwnerIdAndDeletedAtIsNull(trainingLogId, ownerId) != null
        if (!exists) throw NotFoundException("Training log not found")
    }

    private fun parseUuid(value: String?, field: String): UUID = try {
        UUID.fromString(value)
    } catch (ex: Exception) {
        throw BadRequestException("Invalid $field")
    }

    private fun validateFields(duration: BigDecimal?, reps: Int?, weight: BigDecimal?) {
        if (duration == null && reps == null && weight == null) {
            throw BadRequestException("At least one of durationMin, repetitions or weightKg must be provided")
        }
        validatePartial(duration, reps, weight)
    }

    private fun validatePartial(duration: BigDecimal?, reps: Int?, weight: BigDecimal?) {
        duration?.let { if (it <= BigDecimal.ZERO) throw BadRequestException("durationMin must be > 0") }
        reps?.let { if (it < 1) throw BadRequestException("repetitions must be >= 1") }
        weight?.let { if (it <= BigDecimal.ZERO) throw BadRequestException("weightKg must be > 0") }
    }
}

private fun TrainingLogEntry.toDto() = TrainingLogEntryDto(
    id = this.id.toString(),
    exerciseId = this.exerciseId.toString(),
    durationMin = this.durationMin,
    repetitions = this.repetitions,
    weightKg = this.weightKg,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt
)
