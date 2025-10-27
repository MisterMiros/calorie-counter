package tech.miroslav.caloriecounter.food

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.util.UUID

@Entity
@Table(name = "food_macros")
class FoodMacros(
    @Id
    @Column(name = "food_id")
    var foodId: UUID = UUID.randomUUID(),

    @Column(name = "protein_g", nullable = false)
    var proteinG: BigDecimal = BigDecimal.ZERO,

    @Column(name = "fat_g", nullable = false)
    var fatG: BigDecimal = BigDecimal.ZERO,

    @Column(name = "carb_g", nullable = false)
    var carbG: BigDecimal = BigDecimal.ZERO
)
