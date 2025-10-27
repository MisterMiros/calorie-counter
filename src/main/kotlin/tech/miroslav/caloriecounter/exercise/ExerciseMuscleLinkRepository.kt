package tech.miroslav.caloriecounter.exercise

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ExerciseMuscleLinkRepository : JpaRepository<ExerciseMuscleLink, UUID> {
    fun findByExerciseId(exerciseId: UUID): List<ExerciseMuscleLink>
    fun deleteByExerciseId(exerciseId: UUID)
}
