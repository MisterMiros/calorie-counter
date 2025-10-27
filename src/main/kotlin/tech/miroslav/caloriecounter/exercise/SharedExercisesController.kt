package tech.miroslav.caloriecounter.exercise

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import tech.miroslav.caloriecounter.common.PageResponse
import tech.miroslav.caloriecounter.common.AppConstants
import java.util.*

@RestController
@RequestMapping("/api/v1/shared-exercises")
@Tag(name = "Exercises: Shared (Read-only)")
class SharedExercisesController(
    private val exerciseService: ExerciseService
) {
    private val sharedOwnerId = AppConstants.SHARED_AUTH_USER_ID

    @GetMapping
    @Operation(summary = "List shared exercise items (fixed size pagination)")
    fun list(
        @RequestParam(required = false)
        @Parameter(description = "Free-text query on name")
        query: String?,
        @RequestParam(defaultValue = "0")
        @Parameter(description = "0-based page index")
        page: Int
    ): ResponseEntity<PageResponse<ExerciseDto>> {
        val resp = exerciseService.listShared(query, page)
        return ResponseEntity.ok(resp)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a shared exercise item by id (read-only for all authenticated users)")
    fun get(@PathVariable id: UUID): ResponseEntity<ExerciseDto> {
        val dto = exerciseService.get(sharedOwnerId, id)
        return ResponseEntity.ok(dto)
    }
}