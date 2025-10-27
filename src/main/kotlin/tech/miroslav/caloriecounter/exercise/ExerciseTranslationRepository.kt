package tech.miroslav.caloriecounter.exercise

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ExerciseTranslationRepository : JpaRepository<ExerciseTranslation, UUID> {
    fun findByExerciseId(exerciseId: UUID): List<ExerciseTranslation>
    fun findFirstByExerciseId(exerciseId: UUID): ExerciseTranslation?
}
