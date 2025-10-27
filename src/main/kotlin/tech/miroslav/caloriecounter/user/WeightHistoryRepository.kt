package tech.miroslav.caloriecounter.user

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface WeightHistoryRepository : JpaRepository<WeightHistory, UUID> {
    fun findByAppUserIdOrderByTsDesc(appUserId: UUID): List<WeightHistory>
    fun findFirstByAppUserIdOrderByTsDesc(appUserId: UUID): WeightHistory?
    fun existsByIdAndAppUserId(id: UUID, appUserId: UUID): Boolean
}