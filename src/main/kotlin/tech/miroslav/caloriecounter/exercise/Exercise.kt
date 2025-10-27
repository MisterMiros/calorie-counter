package tech.miroslav.caloriecounter.exercise

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

/** Minimal Exercise entity to support training log referential integrity and soft delete checks. */
@Entity
@Table(name = "exercise")
class Exercise(
    @Id
    var id: UUID = UUID.randomUUID(),

    @Column(name = "owner_id", nullable = false)
    var ownerId: UUID,

    @Column(name = "deleted_at")
    var deletedAt: OffsetDateTime? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now()
)
