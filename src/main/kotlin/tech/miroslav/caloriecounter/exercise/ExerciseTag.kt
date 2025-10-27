package tech.miroslav.caloriecounter.exercise

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "exercise_tag")
class ExerciseTag(
    @Id
    var id: UUID = UUID.randomUUID(),

    @Column(nullable = false, unique = true)
    var name: String
)
