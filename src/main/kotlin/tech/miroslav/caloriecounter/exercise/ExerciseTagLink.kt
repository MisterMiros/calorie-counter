package tech.miroslav.caloriecounter.exercise

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.*

/** Join table for exercise and tags */
@Entity
@Table(name = "exercise_tags")
class ExerciseTagLink(
    @Id
    var id: UUID = UUID.randomUUID(),

    @Column(name = "exercise_id", nullable = false)
    var exerciseId: UUID,

    @Column(name = "tag_id", nullable = false)
    var tagId: UUID
)
