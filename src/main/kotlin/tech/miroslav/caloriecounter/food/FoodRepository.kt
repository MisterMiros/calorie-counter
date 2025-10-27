package tech.miroslav.caloriecounter.food

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface FoodRepository : JpaRepository<Food, UUID> {
    fun findByOwnerIdAndDeletedAtIsNull(ownerId: UUID, pageable: Pageable): Page<Food>

    @Query(
        value = """
            SELECT f.* FROM food f
            JOIN food_translation t ON t.food_id = f.id
            WHERE f.owner_id = :ownerId
              AND f.deleted_at IS NULL
              AND (
                   t.name ILIKE CONCAT('%', :q, '%')
                   OR COALESCE(t.producer, '') ILIKE CONCAT('%', :q, '%')
              )
            GROUP BY f.id
            """,
        countQuery = """
            SELECT COUNT(DISTINCT f.id) FROM food f
            JOIN food_translation t ON t.food_id = f.id
            WHERE f.owner_id = :ownerId
              AND f.deleted_at IS NULL
              AND (
                   t.name ILIKE CONCAT('%', :q, '%')
                   OR COALESCE(t.producer, '') ILIKE CONCAT('%', :q, '%')
              )
            """,
        nativeQuery = true
    )
    fun searchOwned(
        @Param("ownerId") ownerId: UUID,
        @Param("q") q: String,
        pageable: Pageable
    ): Page<Food>
}
