package tech.miroslav.caloriecounter.diary

import jakarta.persistence.*
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(name = "diary")
class Diary(
    @Id
    var id: UUID = UUID.randomUUID(),

    @Column(name = "owner_id", nullable = false)
    var ownerId: UUID,

    @Column(nullable = false)
    var date: LocalDate,

    var comment: String? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "deleted_at")
    var deletedAt: OffsetDateTime? = null
)
