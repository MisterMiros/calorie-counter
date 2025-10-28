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

    // FTS-based search (no trigram). Kept for backward compatibility but unused in new filtering flow.
    @Query(
        value = """
            SELECT e.* FROM exercise e
            JOIN exercise_translation t ON t.exercise_id = e.id
            WHERE e.owner_id = :ownerId
              AND e.deleted_at IS NULL
              AND ( :hasQuery = false OR t.search_vector @@ plainto_tsquery(:q) )
            GROUP BY e.id
            ORDER BY MAX(ts_rank(t.search_vector, plainto_tsquery(:q))) DESC NULLS LAST
            """,
        countQuery = """
            SELECT COUNT(DISTINCT e.id) FROM exercise e
            JOIN exercise_translation t ON t.exercise_id = e.id
            WHERE e.owner_id = :ownerId
              AND e.deleted_at IS NULL
              AND ( :hasQuery = false OR t.search_vector @@ plainto_tsquery(:q) )
            """,
        nativeQuery = true
    )
    fun searchOwned(
        @Param("ownerId") ownerId: UUID,
        @Param("q") q: String,
        @Param("hasQuery") hasQuery: Boolean,
        pageable: Pageable
    ): Page<Exercise>

    // New: FTS + optional multi-value OR filters for tags, muscles, and muscle groups.
    @Query(
        value = """
            SELECT e.* FROM exercise e
            JOIN exercise_translation t ON t.exercise_id = e.id
            WHERE e.owner_id = :ownerId
              AND e.deleted_at IS NULL
              AND ( :hasQuery = false OR t.search_vector @@ plainto_tsquery(:q) )
              AND ( :tagsCount = 0 OR EXISTS (
                    SELECT 1 FROM exercise_tags et
                    JOIN exercise_tag tg ON tg.id = et.tag_id
                    WHERE et.exercise_id = e.id
                      AND lower(tg.name) IN (:tagsLower)
                  ) )
              AND ( :musclesCount = 0 OR EXISTS (
                    SELECT 1 FROM exercise_muscles em
                    JOIN muscle m ON m.id = em.muscle_id
                    WHERE em.exercise_id = e.id
                      AND lower(m.name) IN (:musclesLower)
                  ) )
              AND ( :groupsCount = 0 OR EXISTS (
                    SELECT 1 FROM exercise_muscles em2
                    JOIN muscle m2 ON m2.id = em2.muscle_id
                    WHERE em2.exercise_id = e.id
                      AND lower(m2.group_name) IN (:groupsLower)
                  ) )
            GROUP BY e.id
            ORDER BY MAX(ts_rank(t.search_vector, plainto_tsquery(:q))) DESC NULLS LAST
            """,
        countQuery = """
            SELECT COUNT(DISTINCT e.id) FROM exercise e
            JOIN exercise_translation t ON t.exercise_id = e.id
            WHERE e.owner_id = :ownerId
              AND e.deleted_at IS NULL
              AND ( :hasQuery = false OR t.search_vector @@ plainto_tsquery(:q) )
              AND ( :tagsCount = 0 OR EXISTS (
                    SELECT 1 FROM exercise_tags et
                    JOIN exercise_tag tg ON tg.id = et.tag_id
                    WHERE et.exercise_id = e.id
                      AND lower(tg.name) IN (:tagsLower)
                  ) )
              AND ( :musclesCount = 0 OR EXISTS (
                    SELECT 1 FROM exercise_muscles em
                    JOIN muscle m ON m.id = em.muscle_id
                    WHERE em.exercise_id = e.id
                      AND lower(m.name) IN (:musclesLower)
                  ) )
              AND ( :groupsCount = 0 OR EXISTS (
                    SELECT 1 FROM exercise_muscles em2
                    JOIN muscle m2 ON m2.id = em2.muscle_id
                    WHERE em2.exercise_id = e.id
                      AND lower(m2.group_name) IN (:groupsLower)
                  ) )
            """,
        nativeQuery = true
    )
    fun searchOwnedFiltered(
        @Param("ownerId") ownerId: UUID,
        @Param("q") q: String,
        @Param("hasQuery") hasQuery: Boolean,
        @Param("tagsLower") tagsLower: List<String>,
        @Param("tagsCount") tagsCount: Int,
        @Param("musclesLower") musclesLower: List<String>,
        @Param("musclesCount") musclesCount: Int,
        @Param("groupsLower") groupsLower: List<String>,
        @Param("groupsCount") groupsCount: Int,
        pageable: Pageable
    ): Page<Exercise>
}
