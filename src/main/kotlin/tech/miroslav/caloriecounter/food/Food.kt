package tech.miroslav.caloriecounter.food

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

/** Minimal Food entity used for referential validation in diary entries. */
@Entity
@Table(name = "food")
class Food(
    @Id
    var id: UUID = UUID.randomUUID(),

    @Column(name = "owner_id", nullable = false)
    var ownerId: UUID,

    @Column(name = "deleted_at")
    var deletedAt: OffsetDateTime? = null
)
