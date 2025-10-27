package tech.miroslav.caloriecounter.food

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface FoodTranslationRepository : JpaRepository<FoodTranslation, UUID> {
    fun findFirstByFoodId(foodId: UUID): FoodTranslation?
}
