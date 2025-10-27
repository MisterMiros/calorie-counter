package tech.miroslav.caloriecounter.exercise

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ExerciseTagLinkRepository : JpaRepository<ExerciseTagLink, UUID> {
    fun findByExerciseId(exerciseId: UUID): List<ExerciseTagLink>
    fun deleteByExerciseId(exerciseId: UUID)
}
