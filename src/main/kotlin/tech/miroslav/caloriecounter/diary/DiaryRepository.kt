package tech.miroslav.caloriecounter.diary

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.*

@Repository
interface DiaryRepository : JpaRepository<Diary, UUID> {
    fun findByIdAndOwnerIdAndDeletedAtIsNull(id: UUID, ownerId: UUID): Diary?
    fun existsByOwnerIdAndDate(ownerId: UUID, date: LocalDate): Boolean
    fun existsByOwnerIdAndDateAndDeletedAtIsNull(ownerId: UUID, date: LocalDate): Boolean
    fun findByOwnerIdAndDeletedAtIsNull(ownerId: UUID, pageable: Pageable): Page<Diary>
    fun findByOwnerIdAndDateAndDeletedAtIsNull(ownerId: UUID, date: LocalDate, pageable: Pageable): Page<Diary>
}
