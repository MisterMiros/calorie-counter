package tech.miroslav.caloriecounter.food

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "food_translation")
class FoodTranslation(
    @Id
    var id: UUID = UUID.randomUUID(),

    @Column(name = "food_id", nullable = false)
    var foodId: UUID,

    @Column(nullable = false)
    var locale: String,

    @Column(nullable = false)
    var name: String,

    var producer: String? = null
)
