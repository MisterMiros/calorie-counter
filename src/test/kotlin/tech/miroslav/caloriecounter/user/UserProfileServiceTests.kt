package tech.miroslav.caloriecounter.user

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
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class UserProfileServiceTests {

    @Mock
    lateinit var appUserRepository: AppUserRepository

    @InjectMocks
    lateinit var service: UserProfileService

    @Test
    fun `getMyProfile returns dto`() {
        val authId = UUID.randomUUID()
        val app = AppUser(
            id = UUID.randomUUID(),
            authUserId = authId,
            name = "Alice",
            gender = "female",
            dateOfBirth = LocalDate.now().minusYears(25),
            currentWeightKg = BigDecimal("60"),
            heightCm = BigDecimal("165"),
            activityLevel = "SEDENTARY",
            dailyCalorieGoalKcal = BigDecimal("1800"),
            timezone = "UTC",
            createdAt = OffsetDateTime.now(),
            updatedAt = OffsetDateTime.now()
        )
        `when`(appUserRepository.findByAuthUserId(authId)).thenReturn(app)
        val dto = service.getMyProfile(authId)
        assertThat(dto.name).isEqualTo("Alice")
        assertThat(dto.bmi).isNotNull()
        assertThat(dto.estimatedDailyIntakeKcal).isNotNull()
    }

    @Test
    fun `updateMyProfile updates allowed fields and validates`() {
        val authId = UUID.randomUUID()
        val app = AppUser(id = UUID.randomUUID(), authUserId = authId, activityLevel = "SEDENTARY", timezone = "UTC")
        `when`(appUserRepository.findByAuthUserId(authId)).thenReturn(app)
        `when`(appUserRepository.save(Mockito.any())).thenAnswer { it.arguments[0] }

        val req = UpdateUserProfileRequest(
            name = "Bob",
            gender = "male",
            dateOfBirth = LocalDate.now().minusYears(30),
            currentWeightKg = BigDecimal("80.0"),
            heightCm = BigDecimal("180"),
            activityLevel = "Moderately active",
            dailyCalorieGoalKcal = BigDecimal("2200"),
            timezone = "Europe/Berlin"
        )
        val dto = service.updateMyProfile(authId, req)
        assertThat(dto.name).isEqualTo("Bob")
        assertThat(dto.gender).isEqualTo("male")
        assertThat(dto.activityLevel).isEqualTo("MODERATELY_ACTIVE")
        assertThat(dto.timezone).isEqualTo("Europe/Berlin")
        assertThat(dto.currentWeightKg).isEqualTo(BigDecimal("80.0"))
        assertThat(dto.heightCm).isEqualTo(BigDecimal("180"))
        assertThat(dto.dailyCalorieGoalKcal).isEqualTo(BigDecimal("2200"))
    }

    @Test
    fun `updateMyProfile rejects future dob`() {
        val authId = UUID.randomUUID()
        val app = AppUser(id = UUID.randomUUID(), authUserId = authId)
        `when`(appUserRepository.findByAuthUserId(authId)).thenReturn(app)
        assertThatThrownBy {
            service.updateMyProfile(authId, UpdateUserProfileRequest(dateOfBirth = LocalDate.now().plusDays(1)))
        }.isInstanceOf(BadRequestException::class.java)
    }

    @Test
    fun `updateMyProfile rejects invalid timezone`() {
        val authId = UUID.randomUUID()
        val app = AppUser(id = UUID.randomUUID(), authUserId = authId)
        `when`(appUserRepository.findByAuthUserId(authId)).thenReturn(app)
        assertThatThrownBy {
            service.updateMyProfile(authId, UpdateUserProfileRequest(timezone = "Not_A_Zone"))
        }.isInstanceOf(BadRequestException::class.java)
    }

    @Test
    fun `updateMyProfile rejects invalid activity level`() {
        val authId = UUID.randomUUID()
        val app = AppUser(id = UUID.randomUUID(), authUserId = authId)
        `when`(appUserRepository.findByAuthUserId(authId)).thenReturn(app)
        assertThatThrownBy {
            service.updateMyProfile(authId, UpdateUserProfileRequest(activityLevel = "Ultra Active"))
        }.isInstanceOf(BadRequestException::class.java)
    }

    @Test
    fun `getMyProfile 404 when not found`() {
        val authId = UUID.randomUUID()
        `when`(appUserRepository.findByAuthUserId(authId)).thenReturn(null)
        assertThatThrownBy { service.getMyProfile(authId) }.isInstanceOf(NotFoundException::class.java)
    }
}
