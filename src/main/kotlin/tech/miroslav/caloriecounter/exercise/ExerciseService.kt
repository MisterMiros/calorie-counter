package tech.miroslav.caloriecounter.exercise

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tech.miroslav.caloriecounter.common.BadRequestException
import tech.miroslav.caloriecounter.common.NotFoundException
import tech.miroslav.caloriecounter.common.PageResponse
import tech.miroslav.caloriecounter.common.AppConstants
import java.time.OffsetDateTime
import java.util.*

@Service
class ExerciseService(
    private val exerciseRepository: ExerciseRepository,
    private val exerciseTranslationRepository: ExerciseTranslationRepository,
    private val exerciseTagRepository: ExerciseTagRepository,
    private val exerciseTagLinkRepository: ExerciseTagLinkRepository,
    private val muscleRepository: MuscleRepository,
    private val exerciseMuscleLinkRepository: ExerciseMuscleLinkRepository
) {
    private val pageSize: Int = 50

    @Transactional
    fun create(ownerId: UUID, req: CreateExerciseRequest): ExerciseDto {
        if (req.translations.isEmpty()) throw BadRequestException("At least one translation is required")
        val now = OffsetDateTime.now()
        val exercise = Exercise(ownerId = ownerId, createdAt = now, updatedAt = now)
        val saved = exerciseRepository.save(exercise)

        // translations
        val translations = req.translations.map { t ->
            ExerciseTranslation(
                exerciseId = saved.id,
                locale = t.locale.trim(),
                name = t.name.trim()
            )
        }
        exerciseTranslationRepository.saveAll(translations)

        // tags (free form): create tags if missing, then link
        val tagNames = (req.tags ?: emptyList()).map { it.trim() }.filter { it.isNotEmpty() }
        val tagLinks = if (tagNames.isNotEmpty()) {
            val tags = tagNames.map { name ->
                exerciseTagRepository.findByNameIgnoreCase(name) ?: exerciseTagRepository.save(ExerciseTag(name = name))
            }
            val links = tags.map { tag -> ExerciseTagLink(exerciseId = saved.id, tagId = tag.id) }
            exerciseTagLinkRepository.saveAll(links)
        } else emptyList()

        // muscles by name: must exist
        val muscleNames = (req.muscles ?: emptyList()).map { it.trim() }.filter { it.isNotEmpty() }
        val muscleLinks = if (muscleNames.isNotEmpty()) {
            val muscles = muscleNames.map { name ->
                muscleRepository.findByNameIgnoreCase(name)
                    ?: throw BadRequestException("Unknown muscle: $name")
            }
            val links = muscles.map { m -> ExerciseMuscleLink(exerciseId = saved.id, muscleId = m.id) }
            exerciseMuscleLinkRepository.saveAll(links)
        } else emptyList()

        return toDto(saved, translations, tagLinks.map { it.tagId }, muscleLinks.map { it.muscleId })
    }

    @Transactional(readOnly = true)
    fun get(ownerId: UUID, id: UUID): ExerciseDto {
        val ex = exerciseRepository.findById(id).orElseThrow { NotFoundException("Exercise not found") }
        if (ex.ownerId != ownerId) throw NotFoundException("Exercise not found")
        val translations = exerciseTranslationRepository.findByExerciseId(id)
        val tagIds = exerciseTagLinkRepository.findByExerciseId(id).map { it.tagId }
        val muscleIds = exerciseMuscleLinkRepository.findByExerciseId(id).map { it.muscleId }
        return toDto(ex, translations, tagIds, muscleIds)
    }

    @Transactional(readOnly = true)
    fun listOwned(ownerId: UUID, query: String?, page: Int): PageResponse<ExerciseDto> {
        // Backward-compatible method without filters. Delegates to filtered with empty filters.
        return listOwned(ownerId, query, emptyList(), emptyList(), emptyList(), page)
    }

    @Transactional(readOnly = true)
    fun listOwned(
        ownerId: UUID,
        query: String?,
        tags: List<String>?,
        muscles: List<String>?,
        groups: List<String>?,
        page: Int
    ): PageResponse<ExerciseDto> {
        val pageable = PageRequest.of(page.coerceAtLeast(0), pageSize, Sort.by(Sort.Direction.DESC, "updatedAt"))
        val q = (query ?: "").trim()
        val hasQuery = q.isNotEmpty()
        val tagsLower = (tags ?: emptyList()).mapNotNull { it?.trim() }.filter { it.isNotEmpty() }.map { it.lowercase() }
        val musclesLower = (muscles ?: emptyList()).mapNotNull { it?.trim() }.filter { it.isNotEmpty() }.map { it.lowercase() }
        val groupsLower = (groups ?: emptyList()).mapNotNull { it?.trim() }.filter { it.isNotEmpty() }.map { it.lowercase() }

        val noFilters = !hasQuery && tagsLower.isEmpty() && musclesLower.isEmpty() && groupsLower.isEmpty()
        val pg = if (noFilters) {
            exerciseRepository.findByOwnerIdAndDeletedAtIsNull(ownerId, pageable)
        } else {
            exerciseRepository.searchOwnedFiltered(
                ownerId = ownerId,
                q = q.ifEmpty { "" },
                hasQuery = hasQuery,
                tagsLower = if (tagsLower.isEmpty()) listOf("") else tagsLower,
                tagsCount = tagsLower.size,
                musclesLower = if (musclesLower.isEmpty()) listOf("") else musclesLower,
                musclesCount = musclesLower.size,
                groupsLower = if (groupsLower.isEmpty()) listOf("") else groupsLower,
                groupsCount = groupsLower.size,
                pageable = pageable
            )
        }
        val content = pg.content.map { e ->
            val translations = exerciseTranslationRepository.findByExerciseId(e.id)
            val tagIds = exerciseTagLinkRepository.findByExerciseId(e.id).map { it.tagId }
            val muscleIds = exerciseMuscleLinkRepository.findByExerciseId(e.id).map { it.muscleId }
            toDto(e, translations, tagIds, muscleIds)
        }
        return PageResponse(content, pg.number, pg.size, pg.totalElements, pg.totalPages)
    }

    @Transactional(readOnly = true)
    fun listShared(query: String?, page: Int): PageResponse<ExerciseDto> =
        listOwned(AppConstants.SHARED_AUTH_USER_ID, query, page)

    @Transactional(readOnly = true)
    fun listShared(
        query: String?,
        tags: List<String>?,
        muscles: List<String>?,
        groups: List<String>?,
        page: Int
    ): PageResponse<ExerciseDto> =
        listOwned(AppConstants.SHARED_AUTH_USER_ID, query, tags, muscles, groups, page)

    @Transactional
    fun update(ownerId: UUID, id: UUID, req: UpdateExerciseRequest): ExerciseDto {
        val ex = exerciseRepository.findById(id).orElseThrow { NotFoundException("Exercise not found") }
        if (ex.ownerId != ownerId) throw NotFoundException("Exercise not found")
        if (ex.deletedAt != null) throw BadRequestException("Cannot update a deleted exercise")

        var translations = exerciseTranslationRepository.findByExerciseId(id)
        if (req.translations != null) {
            if (translations.isNotEmpty()) exerciseTranslationRepository.deleteAll(translations)
            val newOnes = req.translations.map { t ->
                ExerciseTranslation(exerciseId = id, locale = t.locale.trim(), name = t.name.trim())
            }
            exerciseTranslationRepository.saveAll(newOnes)
            translations = newOnes
        }

        if (req.tags != null) {
            exerciseTagLinkRepository.deleteByExerciseId(id)
            val tagNames = req.tags.map { it.trim() }.filter { it.isNotEmpty() }
            val tags = tagNames.map { name ->
                exerciseTagRepository.findByNameIgnoreCase(name) ?: exerciseTagRepository.save(ExerciseTag(name = name))
            }
            val links = tags.map { tag -> ExerciseTagLink(exerciseId = id, tagId = tag.id) }
            exerciseTagLinkRepository.saveAll(links)
        }

        if (req.muscles != null) {
            exerciseMuscleLinkRepository.deleteByExerciseId(id)
            val muscleNames = req.muscles.map { it.trim() }.filter { it.isNotEmpty() }
            val muscles = muscleNames.map { name ->
                muscleRepository.findByNameIgnoreCase(name)
                    ?: throw BadRequestException("Unknown muscle: $name")
            }
            val links = muscles.map { m -> ExerciseMuscleLink(exerciseId = id, muscleId = m.id) }
            exerciseMuscleLinkRepository.saveAll(links)
        }

        ex.updatedAt = OffsetDateTime.now()
        val saved = exerciseRepository.save(ex)
        val tagIds = exerciseTagLinkRepository.findByExerciseId(id).map { it.tagId }
        val muscleIds = exerciseMuscleLinkRepository.findByExerciseId(id).map { it.muscleId }
        return toDto(saved, translations, tagIds, muscleIds)
    }

    @Transactional
    fun delete(ownerId: UUID, id: UUID) {
        val ex = exerciseRepository.findById(id).orElseThrow { NotFoundException("Exercise not found") }
        if (ex.ownerId != ownerId) throw NotFoundException("Exercise not found")
        if (ex.deletedAt == null) {
            val now = OffsetDateTime.now()
            ex.deletedAt = now
            ex.updatedAt = now
            exerciseRepository.save(ex)
        }
    }

    private fun toDto(
        ex: Exercise,
        translations: List<ExerciseTranslation>,
        tagIds: List<UUID>,
        muscleIds: List<UUID>
    ): ExerciseDto {
        val tagNames = if (tagIds.isEmpty()) emptyList() else {
            // load tag names by ids in batch not implemented -> reuse repository one by one via findAllById for simplicity
            exerciseTagRepository.findAllById(tagIds).map { it.name }
        }
        val muscleNames = if (muscleIds.isEmpty()) emptyList() else {
            muscleRepository.findAllById(muscleIds).map { it.name }
        }
        return ExerciseDto(
            id = ex.id.toString(),
            ownerId = ex.ownerId.toString(),
            translations = translations.map { ExerciseTranslationPayload(locale = it.locale, name = it.name) },
            tags = tagNames,
            muscles = muscleNames,
            createdAt = ex.createdAt,
            updatedAt = ex.updatedAt,
            deletedAt = ex.deletedAt
        )
    }
}
