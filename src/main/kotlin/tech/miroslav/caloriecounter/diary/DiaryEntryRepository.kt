package tech.miroslav.caloriecounter.diary

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface DiaryEntryRepository : JpaRepository<DiaryEntry, UUID> {
    fun deleteByDiaryId(diaryId: UUID)
    fun countByDiaryId(diaryId: UUID): Long
    fun findByDiaryIdOrderByCreatedAtAsc(diaryId: UUID): List<DiaryEntry>
    fun findByIdAndDiaryId(id: UUID, diaryId: UUID): DiaryEntry?
}
