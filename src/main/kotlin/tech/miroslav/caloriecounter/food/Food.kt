package tech.miroslav.caloriecounter.food

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

/** Food entity with minimal fields used by diary entries and summary calculations. */
@Entity
@Table(name = "food")
class Food(
    @Id
    var id: UUID = UUID.randomUUID(),

    @Column(name = "owner_id", nullable = false)
    var ownerId: UUID,

    // Optional measurement mappings and density
    @Column(name = "density_g_per_ml")
    var densityGPerMl: BigDecimal? = null,

    @Column(name = "pack_g")
    var packG: BigDecimal? = null,

    @Column(name = "item_g")
    var itemG: BigDecimal? = null,

    @Column(name = "deleted_at")
    var deletedAt: OffsetDateTime? = null
)
