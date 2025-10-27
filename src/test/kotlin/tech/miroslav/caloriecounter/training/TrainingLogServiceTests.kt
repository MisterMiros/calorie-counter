package tech.miroslav.caloriecounter.training

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
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import tech.miroslav.caloriecounter.common.ConflictException
import tech.miroslav.caloriecounter.common.NotFoundException
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class TrainingLogServiceTests {

    @Mock lateinit var trainingLogRepository: TrainingLogRepository
    @Mock lateinit var trainingLogEntryRepository: TrainingLogEntryRepository

    @InjectMocks lateinit var service: TrainingLogService

    @Captor lateinit var logCaptor: ArgumentCaptor<TrainingLog>

    private val ownerId = UUID.randomUUID()

    @Test
    fun `create training log succeeds when unique`() {
        val date = LocalDate.of(2025, 10, 27)
        `when`(trainingLogRepository.existsByOwnerIdAndDateAndDeletedAtIsNull(ownerId, date)).thenReturn(false)
        val entity = TrainingLog(id = UUID.randomUUID(), ownerId = ownerId, date = date, comment = "cmt")
        `when`(trainingLogRepository.save(Mockito.any(TrainingLog::class.java))).thenReturn(entity)

        val dto = service.create(ownerId, CreateTrainingLogRequest(date = date, comment = "cmt"))

        assertThat(dto.id).isEqualTo(entity.id.toString())
        assertThat(dto.date).isEqualTo(date)
    }

    @Test
    fun `create training log conflicts when date already exists`() {
        val date = LocalDate.of(2025, 10, 27)
        `when`(trainingLogRepository.existsByOwnerIdAndDateAndDeletedAtIsNull(ownerId, date)).thenReturn(true)
        assertThatThrownBy { service.create(ownerId, CreateTrainingLogRequest(date = date, comment = null)) }
            .isInstanceOf(ConflictException::class.java)
    }

    @Test
    fun `get throws not found when missing`() {
        val id = UUID.randomUUID()
        `when`(trainingLogRepository.findByIdAndOwnerIdAndDeletedAtIsNull(id, ownerId)).thenReturn(null)
        assertThatThrownBy { service.get(ownerId, id) }.isInstanceOf(NotFoundException::class.java)
    }

    @Test
    fun `update changing date checks uniqueness`() {
        val id = UUID.randomUUID()
        val oldDate = LocalDate.of(2025, 10, 26)
        val log = TrainingLog(id = id, ownerId = ownerId, date = oldDate)
        `when`(trainingLogRepository.findByIdAndOwnerIdAndDeletedAtIsNull(id, ownerId)).thenReturn(log)
        val newDate = LocalDate.of(2025, 10, 27)
        `when`(trainingLogRepository.existsByOwnerIdAndDateAndDeletedAtIsNull(ownerId, newDate)).thenReturn(true)
        assertThatThrownBy { service.update(ownerId, id, UpdateTrainingLogRequest(date = newDate, comment = null)) }
            .isInstanceOf(ConflictException::class.java)
    }

    @Test
    fun `delete soft deletes training log and hard deletes entries`() {
        val id = UUID.randomUUID()
        val log = TrainingLog(id = id, ownerId = ownerId, date = LocalDate.of(2025, 10, 27))
        `when`(trainingLogRepository.findByIdAndOwnerIdAndDeletedAtIsNull(id, ownerId)).thenReturn(log)
        `when`(trainingLogRepository.save(Mockito.any(TrainingLog::class.java))).thenAnswer { it.arguments[0] }

        service.delete(ownerId, id)

        assertThat(log.deletedAt).isNotNull()
        assertThat(log.updatedAt).isNotNull()
        Mockito.verify(trainingLogEntryRepository).deleteByTrainingLogId(id)
    }

    @Test
    fun `list with and without date filters maps to response`() {
        val log1 = TrainingLog(id = UUID.randomUUID(), ownerId = ownerId, date = LocalDate.of(2025, 10, 25), createdAt = OffsetDateTime.now(), updatedAt = OffsetDateTime.now())
        val log2 = TrainingLog(id = UUID.randomUUID(), ownerId = ownerId, date = LocalDate.of(2025, 10, 26), createdAt = OffsetDateTime.now(), updatedAt = OffsetDateTime.now())
        val pageRequest = PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "date"))
        `when`(trainingLogRepository.findByOwnerIdAndDeletedAtIsNull(ownerId, pageRequest))
            .thenReturn(PageImpl(listOf(log1, log2), pageRequest, 2))

        val respAll = service.list(ownerId, null, 0)
        assertThat(respAll.content).hasSize(2)
        assertThat(respAll.totalElements).isEqualTo(2)
        assertThat(respAll.page).isEqualTo(0)

        val date = LocalDate.of(2025, 10, 26)
        `when`(trainingLogRepository.findByOwnerIdAndDateAndDeletedAtIsNull(ownerId, date, pageRequest))
            .thenReturn(PageImpl(listOf(log2), pageRequest, 1))
        val respDate = service.list(ownerId, date, 0)
        assertThat(respDate.content).hasSize(1)
        assertThat(respDate.content[0].date).isEqualTo(date)
    }
}
