package tech.miroslav.caloriecounter.user

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "weight_history")
class WeightHistory(
    @Id
    var id: UUID = UUID.randomUUID(),

    @Column(name = "app_user_id", nullable = false)
    var appUserId: UUID,

    @Column(name = "ts", nullable = false)
    var ts: Instant,

    @Column(name = "weight_kg", nullable = false)
    var weightKg: BigDecimal,

    var comment: String? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now()
)
