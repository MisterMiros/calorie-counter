package tech.miroslav.caloriecounter.exercise

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ExerciseRepository : JpaRepository<Exercise, UUID> {
    fun findByOwnerIdAndDeletedAtIsNull(ownerId: UUID, pageable: Pageable): Page<Exercise>

    @Query(
        value = """
            SELECT e.* FROM exercise e
            JOIN exercise_translation t ON t.exercise_id = e.id
            WHERE e.owner_id = :ownerId
              AND e.deleted_at IS NULL
              AND (
                   t.name ILIKE CONCAT('%', :q, '%')
              )
            GROUP BY e.id
            """,
        countQuery = """
            SELECT COUNT(DISTINCT e.id) FROM exercise e
            JOIN exercise_translation t ON t.exercise_id = e.id
            WHERE e.owner_id = :ownerId
              AND e.deleted_at IS NULL
              AND (
                   t.name ILIKE CONCAT('%', :q, '%')
              )
            """,
        nativeQuery = true
    )
    fun searchOwned(
        @Param("ownerId") ownerId: UUID,
        @Param("q") q: String,
        pageable: Pageable
    ): Page<Exercise>
}
