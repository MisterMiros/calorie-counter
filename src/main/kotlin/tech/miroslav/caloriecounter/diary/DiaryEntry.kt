package tech.miroslav.caloriecounter.diary

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "diary_entry")
class DiaryEntry(
    @Id
    var id: UUID = UUID.randomUUID(),

    @Column(name = "diary_id", nullable = false)
    var diaryId: UUID,

    @Column(name = "food_id", nullable = false)
    var foodId: UUID,

    @Column(name = "amount", nullable = false)
    var amount: BigDecimal,

    @Column(name = "unit", nullable = false)
    var unit: String,

    @Column(name = "meal", nullable = false)
    var meal: String,

    var comment: String? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now()
)
