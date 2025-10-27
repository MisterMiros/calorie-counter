package tech.miroslav.caloriecounter.training

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
import tech.miroslav.caloriecounter.common.NotFoundException
import tech.miroslav.caloriecounter.exercise.Exercise
import tech.miroslav.caloriecounter.exercise.ExerciseRepository
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

@ExtendWith(MockitoExtension::class)
class TrainingLogEntryServiceTests {

    @Mock lateinit var trainingLogRepository: TrainingLogRepository
    @Mock lateinit var trainingLogEntryRepository: TrainingLogEntryRepository
    @Mock lateinit var exerciseRepository: ExerciseRepository

    @InjectMocks lateinit var service: TrainingLogEntryService

    private fun stubLog(ownerId: UUID, logId: UUID): TrainingLog {
        val l = TrainingLog(id = logId, ownerId = ownerId, date = LocalDate.now())
        `when`(trainingLogRepository.findByIdAndOwnerIdAndDeletedAtIsNull(logId, ownerId)).thenReturn(l)
        return l
    }

    @Test
    fun `list requires log ownership and returns dtos`() {
        val ownerId = UUID.randomUUID(); val logId = UUID.randomUUID();
        stubLog(ownerId, logId)
        val e = TrainingLogEntry(id = UUID.randomUUID(), trainingLogId = logId, exerciseId = UUID.randomUUID(), durationMin = BigDecimal("30"))
        `when`(trainingLogEntryRepository.findByTrainingLogIdOrderByCreatedAtAsc(logId)).thenReturn(listOf(e))

        val list = service.list(ownerId, logId)
        assertThat(list).hasSize(1)
        assertThat(list[0].id).isEqualTo(e.id.toString())
        assertThat(list[0].durationMin).isEqualTo(BigDecimal("30"))
    }

    @Test
    fun `create validates exercise and fields`() {
        val ownerId = UUID.randomUUID(); val logId = UUID.randomUUID();
        stubLog(ownerId, logId)
        // invalid exercise id
        assertThatThrownBy {
            service.create(ownerId, logId, CreateTrainingLogEntryRequest(exerciseId = "bad", durationMin = BigDecimal("10")))
        }.isInstanceOf(BadRequestException::class.java)

        // exercise not found
        val exId = UUID.randomUUID()
        `when`(exerciseRepository.findById(exId)).thenReturn(Optional.empty())
        assertThatThrownBy {
            service.create(ownerId, logId, CreateTrainingLogEntryRequest(exerciseId = exId.toString(), durationMin = BigDecimal("10")))
        }.isInstanceOf(NotFoundException::class.java)

        // all fields null
        val ex = Exercise(id = exId, ownerId = ownerId)
        `when`(exerciseRepository.findById(exId)).thenReturn(Optional.of(ex))
        assertThatThrownBy {
            service.create(ownerId, logId, CreateTrainingLogEntryRequest(exerciseId = exId.toString()))
        }.isInstanceOf(BadRequestException::class.java)
    }

    @Test
    fun `create rejects soft-deleted exercise`() {
        val ownerId = UUID.randomUUID(); val logId = UUID.randomUUID();
        stubLog(ownerId, logId)
        val exId = UUID.randomUUID()
        val ex = Exercise(id = exId, ownerId = ownerId, deletedAt = java.time.OffsetDateTime.now())
        `when`(exerciseRepository.findById(exId)).thenReturn(Optional.of(ex))
        assertThatThrownBy {
            service.create(ownerId, logId, CreateTrainingLogEntryRequest(exerciseId = exId.toString(), durationMin = BigDecimal("10")))
        }.isInstanceOf(BadRequestException::class.java)
    }

    @Test
    fun `create success saves entry`() {
        val ownerId = UUID.randomUUID(); val logId = UUID.randomUUID();
        stubLog(ownerId, logId)
        val exId = UUID.randomUUID()
        val ex = Exercise(id = exId, ownerId = ownerId, deletedAt = null)
        `when`(exerciseRepository.findById(exId)).thenReturn(Optional.of(ex))
        val saved = TrainingLogEntry(id = UUID.randomUUID(), trainingLogId = logId, exerciseId = exId, durationMin = BigDecimal("45"), repetitions = 10, weightKg = BigDecimal("20"))
        `when`(trainingLogEntryRepository.save(Mockito.any())).thenReturn(saved)

        val dto = service.create(ownerId, logId, CreateTrainingLogEntryRequest(exerciseId = exId.toString(), durationMin = BigDecimal("45"), repetitions = 10, weightKg = BigDecimal("20")))
        assertThat(dto.id).isEqualTo(saved.id.toString())
        assertThat(dto.exerciseId).isEqualTo(exId.toString())
        assertThat(dto.durationMin).isEqualTo(BigDecimal("45"))
    }

    @Test
    fun `get enforces ownership and returns dto`() {
        val ownerId = UUID.randomUUID(); val logId = UUID.randomUUID(); val entryId = UUID.randomUUID()
        stubLog(ownerId, logId)
        val e = TrainingLogEntry(id = entryId, trainingLogId = logId, exerciseId = UUID.randomUUID())
        `when`(trainingLogEntryRepository.findByIdAndTrainingLogId(entryId, logId)).thenReturn(e)
        val dto = service.get(ownerId, logId, entryId)
        assertThat(dto.id).isEqualTo(entryId.toString())
    }

    @Test
    fun `update validates fields and saves`() {
        val ownerId = UUID.randomUUID(); val logId = UUID.randomUUID(); val entryId = UUID.randomUUID()
        stubLog(ownerId, logId)
        val e = TrainingLogEntry(id = entryId, trainingLogId = logId, exerciseId = UUID.randomUUID(), durationMin = BigDecimal("30"), repetitions = 8, weightKg = BigDecimal("15"))
        `when`(trainingLogEntryRepository.findByIdAndTrainingLogId(entryId, logId)).thenReturn(e)

        // invalid repetition (0)
        assertThatThrownBy { service.update(ownerId, logId, entryId, UpdateTrainingLogEntryRequest(repetitions = 0)) }
            .isInstanceOf(BadRequestException::class.java)

        // change exercise to soft-deleted
        val exId = UUID.randomUUID()
        val ex = Exercise(id = exId, ownerId = ownerId, deletedAt = java.time.OffsetDateTime.now())
        `when`(exerciseRepository.findById(exId)).thenReturn(Optional.of(ex))
        assertThatThrownBy { service.update(ownerId, logId, entryId, UpdateTrainingLogEntryRequest(exerciseId = exId.toString())) }
            .isInstanceOf(BadRequestException::class.java)

        // happy path adjust fields
        `when`(trainingLogEntryRepository.save(Mockito.any())).thenAnswer { it.arguments[0] }
        val dto = service.update(ownerId, logId, entryId, UpdateTrainingLogEntryRequest(durationMin = BigDecimal("60"), repetitions = 12, weightKg = BigDecimal("25")))
        assertThat(dto.durationMin).isEqualTo(BigDecimal("60"))
        assertThat(dto.repetitions).isEqualTo(12)
        assertThat(dto.weightKg).isEqualTo(BigDecimal("25"))
    }

    @Test
    fun `delete enforces ownership`() {
        val ownerId = UUID.randomUUID(); val logId = UUID.randomUUID(); val entryId = UUID.randomUUID()
        stubLog(ownerId, logId)
        `when`(trainingLogEntryRepository.findByIdAndTrainingLogId(entryId, logId)).thenReturn(null)
        assertThatThrownBy { service.delete(ownerId, logId, entryId) }
            .isInstanceOf(NotFoundException::class.java)
    }
}
