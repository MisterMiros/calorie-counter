package tech.miroslav.caloriecounter.training

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface TrainingLogEntryRepository : JpaRepository<TrainingLogEntry, UUID> {
    fun deleteByTrainingLogId(trainingLogId: UUID)
    fun countByTrainingLogId(trainingLogId: UUID): Long
    fun findByTrainingLogIdOrderByCreatedAtAsc(trainingLogId: UUID): List<TrainingLogEntry>
    fun findByIdAndTrainingLogId(id: UUID, trainingLogId: UUID): TrainingLogEntry?
}
