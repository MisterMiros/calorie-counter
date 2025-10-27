package tech.miroslav.caloriecounter.training

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import tech.miroslav.caloriecounter.common.UnauthorizedException
import tech.miroslav.caloriecounter.security.CurrentUser
import java.util.*

@RestController
@RequestMapping("/api/v1/training-logs/{logId}/entries")
@Tag(name = "Training Logs: Entries")
class TrainingLogEntriesController(
    private val trainingLogEntryService: TrainingLogEntryService
) {

    @GetMapping
    @Operation(summary = "List entries for a training log")
    fun list(@PathVariable logId: UUID): ResponseEntity<List<TrainingLogEntryDto>> {
        val principal = CurrentUser.principalOrNull() ?: throw UnauthorizedException("Unauthorized")
        val items = trainingLogEntryService.list(principal.authUserId, logId)
        return ResponseEntity.ok(items)
    }

    @PostMapping
    @Operation(summary = "Create a training log entry")
    fun create(
        @PathVariable logId: UUID,
        @RequestBody @Valid req: CreateTrainingLogEntryRequest
    ): ResponseEntity<TrainingLogEntryDto> {
        val principal = CurrentUser.principalOrNull() ?: throw UnauthorizedException("Unauthorized")
        val dto = trainingLogEntryService.create(principal.authUserId, logId, req)
        return ResponseEntity.status(201).body(dto)
    }

    @GetMapping("/{entryId}")
    @Operation(summary = "Get a training log entry by id")
    fun get(@PathVariable logId: UUID, @PathVariable entryId: UUID): ResponseEntity<TrainingLogEntryDto> {
        val principal = CurrentUser.principalOrNull() ?: throw UnauthorizedException("Unauthorized")
        val dto = trainingLogEntryService.get(principal.authUserId, logId, entryId)
        return ResponseEntity.ok(dto)
    }

    @PutMapping("/{entryId}")
    @Operation(summary = "Update a training log entry")
    fun update(
        @PathVariable logId: UUID,
        @PathVariable entryId: UUID,
        @RequestBody @Valid req: UpdateTrainingLogEntryRequest
    ): ResponseEntity<TrainingLogEntryDto> {
        val principal = CurrentUser.principalOrNull() ?: throw UnauthorizedException("Unauthorized")
        val dto = trainingLogEntryService.update(principal.authUserId, logId, entryId, req)
        return ResponseEntity.ok(dto)
    }

    @DeleteMapping("/{entryId}")
    @Operation(summary = "Delete a training log entry")
    fun delete(@PathVariable logId: UUID, @PathVariable entryId: UUID): ResponseEntity<Void> {
        val principal = CurrentUser.principalOrNull() ?: throw UnauthorizedException("Unauthorized")
        trainingLogEntryService.delete(principal.authUserId, logId, entryId)
        return ResponseEntity.noContent().build()
    }
}
