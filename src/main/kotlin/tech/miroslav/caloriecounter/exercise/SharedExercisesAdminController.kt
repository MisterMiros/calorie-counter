package tech.miroslav.caloriecounter.exercise

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import tech.miroslav.caloriecounter.common.AppConstants
import java.util.*

@RestController
@RequestMapping("/api/v1/admin/shared-exercises")
@Tag(name = "Exercises: Shared (Admin)")
class SharedExercisesAdminController(
    private val exerciseService: ExerciseService
) {

    private val sharedOwnerId = AppConstants.SHARED_AUTH_USER_ID

    @PostMapping
    @Operation(summary = "Create a shared exercise item (admin)")
    fun create(@RequestBody @Valid req: CreateExerciseRequest): ResponseEntity<ExerciseDto> {
        val dto = exerciseService.create(sharedOwnerId, req)
        return ResponseEntity.status(201).body(dto)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a shared exercise item by id (admin)")
    fun get(@PathVariable id: UUID): ResponseEntity<ExerciseDto> {
        val dto = exerciseService.get(sharedOwnerId, id)
        return ResponseEntity.ok(dto)
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a shared exercise item (admin)")
    fun update(@PathVariable id: UUID, @RequestBody @Valid req: UpdateExerciseRequest): ResponseEntity<ExerciseDto> {
        val dto = exerciseService.update(sharedOwnerId, id, req)
        return ResponseEntity.ok(dto)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete a shared exercise item (admin)")
    fun delete(@PathVariable id: UUID): ResponseEntity<Void> {
        exerciseService.delete(sharedOwnerId, id)
        return ResponseEntity.noContent().build()
    }
}
