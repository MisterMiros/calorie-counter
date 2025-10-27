package tech.miroslav.caloriecounter.user

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
import java.time.Instant
import java.time.OffsetDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class WeightHistoryServiceTests {

    @Mock
    lateinit var weightHistoryRepository: WeightHistoryRepository

    @Mock
    lateinit var appUserRepository: AppUserRepository

    @InjectMocks
    lateinit var service: WeightHistoryService

    @Captor
    lateinit var whCaptor: ArgumentCaptor<WeightHistory>

    @Test
    fun `add entry updates current weight and saves history`() {
        val appUserId = UUID.randomUUID()
        val appUser = AppUser(id = appUserId, authUserId = UUID.randomUUID())
        `when`(appUserRepository.findById(appUserId)).thenReturn(Optional.of(appUser))

        val saved = WeightHistory(
            id = UUID.randomUUID(),
            appUserId = appUserId,
            ts = Instant.now(),
            weightKg = BigDecimal("80.5"),
            comment = "evening"
        )
        `when`(weightHistoryRepository.save(Mockito.any())).thenReturn(saved)

        val dto = service.add(appUserId, CreateWeightHistoryRequest(ts = saved.ts, weightKg = saved.weightKg, comment = saved.comment))

        assertThat(dto.id).isEqualTo(saved.id.toString())
        assertThat(dto.weightKg).isEqualTo(saved.weightKg)
        assertThat(appUser.currentWeightKg).isEqualTo(saved.weightKg)
    }

    @Test
    fun `add entry requires positive weight`() {
        val appUserId = UUID.randomUUID()
        assertThatThrownBy { service.add(appUserId, CreateWeightHistoryRequest(weightKg = BigDecimal.ZERO)) }
            .isInstanceOf(BadRequestException::class.java)
        assertThatThrownBy { service.add(appUserId, CreateWeightHistoryRequest(weightKg = BigDecimal("-1"))) }
            .isInstanceOf(BadRequestException::class.java)
    }

    @Test
    fun `delete checks ownership`() {
        val appUserId = UUID.randomUUID()
        val entryId = UUID.randomUUID()
        `when`(weightHistoryRepository.existsByIdAndAppUserId(entryId, appUserId)).thenReturn(false)
        assertThatThrownBy { service.delete(appUserId, entryId) }
            .isInstanceOf(NotFoundException::class.java)
    }

    @Test
    fun `list maps entities to dtos`() {
        val appUserId = UUID.randomUUID()
        val e = WeightHistory(id = UUID.randomUUID(), appUserId = appUserId, ts = Instant.now(), weightKg = BigDecimal("70.0"), comment = null)
        `when`(weightHistoryRepository.findByAppUserIdOrderByTsDesc(appUserId)).thenReturn(listOf(e))
        val list = service.list(appUserId)
        assertThat(list).hasSize(1)
        assertThat(list[0].id).isEqualTo(e.id.toString())
        assertThat(list[0].weightKg).isEqualTo(e.weightKg)
    }
}
