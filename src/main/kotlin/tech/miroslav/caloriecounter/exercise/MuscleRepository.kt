package tech.miroslav.caloriecounter.exercise

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface MuscleRepository : JpaRepository<Muscle, UUID> {
    fun findByNameIgnoreCase(name: String): Muscle?
}
