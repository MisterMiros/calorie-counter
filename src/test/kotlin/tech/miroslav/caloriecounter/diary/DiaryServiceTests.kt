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
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import tech.miroslav.caloriecounter.common.ConflictException
import tech.miroslav.caloriecounter.common.NotFoundException
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class DiaryServiceTests {

    @Mock
    lateinit var diaryRepository: DiaryRepository

    @Mock
    lateinit var diaryEntryRepository: DiaryEntryRepository

    @InjectMocks
    lateinit var service: DiaryService

    @Captor
    lateinit var diaryCaptor: ArgumentCaptor<Diary>

    private val ownerId = UUID.randomUUID()

    @Test
    fun `create diary succeeds when unique`() {
        val date = LocalDate.of(2025, 10, 27)
        `when`(diaryRepository.existsByOwnerIdAndDateAndDeletedAtIsNull(ownerId, date)).thenReturn(false)
        val entity = Diary(id = UUID.randomUUID(), ownerId = ownerId, date = date, comment = "cmt")
        `when`(diaryRepository.save(Mockito.any(Diary::class.java))).thenReturn(entity)

        val dto = service.create(ownerId, CreateDiaryRequest(date = date, comment = "cmt"))

        assertThat(dto.id).isEqualTo(entity.id.toString())
        assertThat(dto.date).isEqualTo(date)
    }

    @Test
    fun `create diary conflicts when date already exists`() {
        val date = LocalDate.of(2025, 10, 27)
        `when`(diaryRepository.existsByOwnerIdAndDateAndDeletedAtIsNull(ownerId, date)).thenReturn(true)
        assertThatThrownBy { service.create(ownerId, CreateDiaryRequest(date = date, comment = null)) }
            .isInstanceOf(ConflictException::class.java)
    }

    @Test
    fun `get throws not found when missing`() {
        val id = UUID.randomUUID()
        `when`(diaryRepository.findByIdAndOwnerIdAndDeletedAtIsNull(id, ownerId)).thenReturn(null)
        assertThatThrownBy { service.get(ownerId, id) }.isInstanceOf(NotFoundException::class.java)
    }

    @Test
    fun `update changing date checks uniqueness`() {
        val id = UUID.randomUUID()
        val oldDate = LocalDate.of(2025, 10, 26)
        val diary = Diary(id = id, ownerId = ownerId, date = oldDate)
        `when`(diaryRepository.findByIdAndOwnerIdAndDeletedAtIsNull(id, ownerId)).thenReturn(diary)
        val newDate = LocalDate.of(2025, 10, 27)
        `when`(diaryRepository.existsByOwnerIdAndDateAndDeletedAtIsNull(ownerId, newDate)).thenReturn(true)
        assertThatThrownBy { service.update(ownerId, id, UpdateDiaryRequest(date = newDate, comment = null)) }
            .isInstanceOf(ConflictException::class.java)
    }

    @Test
    fun `delete soft deletes diary and hard deletes entries`() {
        val id = UUID.randomUUID()
        val diary = Diary(id = id, ownerId = ownerId, date = LocalDate.of(2025, 10, 27))
        `when`(diaryRepository.findByIdAndOwnerIdAndDeletedAtIsNull(id, ownerId)).thenReturn(diary)
        `when`(diaryRepository.save(Mockito.any(Diary::class.java))).thenAnswer { it.arguments[0] }

        service.delete(ownerId, id)

        assertThat(diary.deletedAt).isNotNull()
        assertThat(diary.updatedAt).isNotNull()
        Mockito.verify(diaryEntryRepository).deleteByDiaryId(id)
    }

    @Test
    fun `list with and without date filters maps to response`() {
        val diary1 = Diary(id = UUID.randomUUID(), ownerId = ownerId, date = LocalDate.of(2025, 10, 25), createdAt = OffsetDateTime.now(), updatedAt = OffsetDateTime.now())
        val diary2 = Diary(id = UUID.randomUUID(), ownerId = ownerId, date = LocalDate.of(2025, 10, 26), createdAt = OffsetDateTime.now(), updatedAt = OffsetDateTime.now())
        val pageRequest = PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "date"))
        `when`(diaryRepository.findByOwnerIdAndDeletedAtIsNull(ownerId, pageRequest))
            .thenReturn(PageImpl(listOf(diary1, diary2), pageRequest, 2))

        val respAll = service.list(ownerId, null, 0)
        assertThat(respAll.content).hasSize(2)
        assertThat(respAll.totalElements).isEqualTo(2)
        assertThat(respAll.page).isEqualTo(0)

        val date = LocalDate.of(2025, 10, 26)
        `when`(diaryRepository.findByOwnerIdAndDateAndDeletedAtIsNull(ownerId, date, pageRequest))
            .thenReturn(PageImpl(listOf(diary2), pageRequest, 1))
        val respDate = service.list(ownerId, date, 0)
        assertThat(respDate.content).hasSize(1)
        assertThat(respDate.content[0].date).isEqualTo(date)
    }
}
