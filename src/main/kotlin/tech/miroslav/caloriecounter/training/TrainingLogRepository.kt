package tech.miroslav.caloriecounter.training

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.*

@Repository
interface TrainingLogRepository : JpaRepository<TrainingLog, UUID> {
    fun findByIdAndOwnerIdAndDeletedAtIsNull(id: UUID, ownerId: UUID): TrainingLog?
    fun existsByOwnerIdAndDateAndDeletedAtIsNull(ownerId: UUID, date: LocalDate): Boolean
    fun findByOwnerIdAndDeletedAtIsNull(ownerId: UUID, pageable: Pageable): Page<TrainingLog>
    fun findByOwnerIdAndDateAndDeletedAtIsNull(ownerId: UUID, date: LocalDate, pageable: Pageable): Page<TrainingLog>
}
