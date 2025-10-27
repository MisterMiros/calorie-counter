package tech.miroslav.caloriecounter.user

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tech.miroslav.caloriecounter.common.BadRequestException
import tech.miroslav.caloriecounter.common.NotFoundException
import java.math.BigDecimal
import java.time.Instant
import java.util.*

@Service
class WeightHistoryService(
    private val weightHistoryRepository: WeightHistoryRepository,
    private val appUserRepository: AppUserRepository
) {

    @Transactional(readOnly = true)
    fun list(appUserId: UUID): List<WeightHistoryDto> {
        return weightHistoryRepository.findByAppUserIdOrderByTsDesc(appUserId)
            .map { it.toDto() }
    }

    @Transactional
    fun add(appUserId: UUID, req: CreateWeightHistoryRequest): WeightHistoryDto {
        val weight = req.weightKg ?: throw BadRequestException("weightKg is required")
        if (weight <= BigDecimal.ZERO) throw BadRequestException("weightKg must be > 0")

        val app = appUserRepository.findById(appUserId).orElseThrow { NotFoundException("User profile not found") }

        val entity = WeightHistory(
            appUserId = appUserId,
            ts = req.ts ?: Instant.now(),
            weightKg = weight,
            comment = req.comment
        )
        val saved = weightHistoryRepository.save(entity)

        // Update current weight on profile
        app.currentWeightKg = weight
        app.updatedAt = java.time.OffsetDateTime.now()
        appUserRepository.save(app)

        return saved.toDto()
    }

    @Transactional
    fun delete(appUserId: UUID, entryId: UUID) {
        // Ensure entry belongs to user
        val exists = weightHistoryRepository.existsByIdAndAppUserId(entryId, appUserId)
        if (!exists) throw NotFoundException("Weight history entry not found")
        weightHistoryRepository.deleteById(entryId)
        // Spec allows simple delete; updating current weight after delete is not required now
    }
}

private fun WeightHistory.toDto() = WeightHistoryDto(
    id = this.id.toString(),
    ts = this.ts,
    weightKg = this.weightKg,
    comment = this.comment
)
