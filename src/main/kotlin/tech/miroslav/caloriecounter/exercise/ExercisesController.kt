package tech.miroslav.caloriecounter.exercise

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import tech.miroslav.caloriecounter.common.PageResponse
import tech.miroslav.caloriecounter.common.UnauthorizedException
import tech.miroslav.caloriecounter.security.CurrentUser
import java.util.*

@RestController
@RequestMapping("/api/v1/exercises")
@Tag(name = "Exercises (User-owned)")
class ExercisesController(
    private val exerciseService: ExerciseService
) {

    @GetMapping
    @Operation(summary = "List user-owned exercise items (fixed size pagination)")
    fun list(
        @RequestParam(required = false)
        @Parameter(description = "Free-text query on name")
        query: String?,
        @RequestParam(defaultValue = "0")
        @Parameter(description = "0-based page index")
        page: Int
    ): ResponseEntity<PageResponse<ExerciseDto>> {
        val principal = CurrentUser.principalOrNull() ?: throw UnauthorizedException("Unauthorized")
        val resp = exerciseService.listOwned(principal.authUserId, query, page)
        return ResponseEntity.ok(resp)
    }

    @PostMapping
    @Operation(summary = "Create a user-owned exercise item")
    fun create(@RequestBody @Valid req: CreateExerciseRequest): ResponseEntity<ExerciseDto> {
        val principal = CurrentUser.principalOrNull() ?: throw UnauthorizedException("Unauthorized")
        val dto = exerciseService.create(principal.authUserId, req)
        return ResponseEntity.status(201).body(dto)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a user-owned exercise item by id (returns even if soft-deleted)")
    fun get(@PathVariable id: UUID): ResponseEntity<ExerciseDto> {
        val principal = CurrentUser.principalOrNull() ?: throw UnauthorizedException("Unauthorized")
        val dto = exerciseService.get(principal.authUserId, id)
        return ResponseEntity.ok(dto)
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a user-owned exercise item (forbidden if soft-deleted)")
    fun update(@PathVariable id: UUID, @RequestBody @Valid req: UpdateExerciseRequest): ResponseEntity<ExerciseDto> {
        val principal = CurrentUser.principalOrNull() ?: throw UnauthorizedException("Unauthorized")
        val dto = exerciseService.update(principal.authUserId, id, req)
        return ResponseEntity.ok(dto)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete a user-owned exercise item")
    fun delete(@PathVariable id: UUID): ResponseEntity<Void> {
        val principal = CurrentUser.principalOrNull() ?: throw UnauthorizedException("Unauthorized")
        exerciseService.delete(principal.authUserId, id)
        return ResponseEntity.noContent().build()
    }
}