package tech.miroslav.caloriecounter.food

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface FoodMacrosRepository : JpaRepository<FoodMacros, UUID>
