package tech.miroslav.caloriecounter.exercise

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ExerciseTagRepository : JpaRepository<ExerciseTag, UUID> {
    fun findByNameIgnoreCase(name: String): ExerciseTag?
}
