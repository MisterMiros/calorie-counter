package tech.miroslav.caloriecounter.exercise

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import tech.miroslav.caloriecounter.common.BadRequestException
import tech.miroslav.caloriecounter.common.NotFoundException
import java.time.OffsetDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class ExerciseServiceTests {

    @Mock lateinit var exerciseRepository: ExerciseRepository
    @Mock lateinit var exerciseTranslationRepository: ExerciseTranslationRepository
    @Mock lateinit var exerciseTagRepository: ExerciseTagRepository
    @Mock lateinit var exerciseTagLinkRepository: ExerciseTagLinkRepository
    @Mock lateinit var muscleRepository: MuscleRepository
    @Mock lateinit var exerciseMuscleLinkRepository: ExerciseMuscleLinkRepository

    @InjectMocks lateinit var service: ExerciseService

    @Captor lateinit var exCaptor: ArgumentCaptor<Exercise>

    @Test
    fun `create exercise with tags and muscles`() {
        val ownerId = UUID.randomUUID()
        val req = CreateExerciseRequest(
            translations = listOf(ExerciseTranslationPayload(locale = "en", name = "Push-up")),
            tags = listOf("strength", "bodyweight"),
            muscles = listOf("pectoralis major", "triceps brachii")
        )

        val savedExercise = Exercise(id = UUID.randomUUID(), ownerId = ownerId)
        `when`(exerciseRepository.save(Mockito.any(Exercise::class.java))).thenReturn(savedExercise)
        `when`(exerciseTranslationRepository.saveAll(Mockito.anyList())).thenAnswer { it.arguments[0] }

        // Tags: strength exists, bodyweight new
        val strength = ExerciseTag(id = UUID.randomUUID(), name = "strength")
        `when`(exerciseTagRepository.findByNameIgnoreCase("strength")).thenReturn(strength)
        `when`(exerciseTagRepository.findByNameIgnoreCase("bodyweight")).thenReturn(null)
        `when`(exerciseTagRepository.save(Mockito.any(ExerciseTag::class.java))).thenAnswer { it.arguments[0] }
        `when`(exerciseTagLinkRepository.saveAll(Mockito.anyList())).thenAnswer { it.arguments[0] }

        // Muscles exist
        val pec = Muscle(id = UUID.randomUUID(), name = "pectoralis major", groupName = "chest")
        val tri = Muscle(id = UUID.randomUUID(), name = "triceps brachii", groupName = "arms")
        `when`(muscleRepository.findByNameIgnoreCase("pectoralis major")).thenReturn(pec)
        `when`(muscleRepository.findByNameIgnoreCase("triceps brachii")).thenReturn(tri)
        `when`(exerciseMuscleLinkRepository.saveAll(Mockito.anyList())).thenAnswer { it.arguments[0] }


        val dto = service.create(ownerId, req)
        assertThat(dto.id).isEqualTo(savedExercise.id.toString())
        assertThat(dto.translations).hasSize(1)
        assertThat(dto.translations[0].name).isEqualTo("Push-up")
    }

    @Test
    fun `get throws not found when not owner`() {
        val ownerId = UUID.randomUUID(); val otherOwner = UUID.randomUUID(); val id = UUID.randomUUID()
        val ex = Exercise(id = id, ownerId = otherOwner)
        `when`(exerciseRepository.findById(id)).thenReturn(Optional.of(ex))
        assertThatThrownBy { service.get(ownerId, id) }.isInstanceOf(NotFoundException::class.java)
    }

    @Test
    fun `update forbidden for deleted exercise`() {
        val ownerId = UUID.randomUUID(); val id = UUID.randomUUID()
        val ex = Exercise(id = id, ownerId = ownerId, deletedAt = OffsetDateTime.now())
        `when`(exerciseRepository.findById(id)).thenReturn(Optional.of(ex))
        assertThatThrownBy { service.update(ownerId, id, UpdateExerciseRequest()) }
            .isInstanceOf(BadRequestException::class.java)
    }

    @Test
    fun `delete soft sets deletedAt`() {
        val ownerId = UUID.randomUUID(); val id = UUID.randomUUID()
        val ex = Exercise(id = id, ownerId = ownerId, deletedAt = null)
        `when`(exerciseRepository.findById(id)).thenReturn(Optional.of(ex))
        `when`(exerciseRepository.save(Mockito.any(Exercise::class.java))).thenAnswer { it.arguments[0] }
        service.delete(ownerId, id)
        assertThat(ex.deletedAt).isNotNull()
    }

    @Test
    fun `listOwned maps to dto`() {
        val ownerId = UUID.randomUUID()
        val e1 = Exercise(id = UUID.randomUUID(), ownerId = ownerId)
        val page = org.springframework.data.domain.PageImpl(listOf(e1))
        val pageRequest = org.springframework.data.domain.PageRequest.of(0, 50, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "updatedAt"))
        `when`(exerciseRepository.findByOwnerIdAndDeletedAtIsNull(ownerId, pageRequest)).thenReturn(page)
        `when`(exerciseTranslationRepository.findByExerciseId(e1.id)).thenReturn(listOf(ExerciseTranslation(id = UUID.randomUUID(), exerciseId = e1.id, locale = "en", name = "Bench Press")))
        `when`(exerciseTagLinkRepository.findByExerciseId(e1.id)).thenReturn(emptyList())
        `when`(exerciseMuscleLinkRepository.findByExerciseId(e1.id)).thenReturn(emptyList())

        val resp = service.listOwned(ownerId, null, 0)
        assertThat(resp.content).hasSize(1)
        assertThat(resp.content[0].translations[0].name).isEqualTo("Bench Press")
    }
}
