package tech.miroslav.caloriecounter.training

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "training_log_entry")
class TrainingLogEntry(
    @Id
    var id: UUID = UUID.randomUUID(),

    @Column(name = "training_log_id", nullable = false)
    var trainingLogId: UUID,

    @Column(name = "exercise_id", nullable = false)
    var exerciseId: UUID,

    @Column(name = "duration_min")
    var durationMin: BigDecimal? = null,

    var repetitions: Int? = null,

    @Column(name = "weight_kg")
    var weightKg: BigDecimal? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now()
)
