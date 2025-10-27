package tech.miroslav.caloriecounter.training

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import tech.miroslav.caloriecounter.common.PageResponse
import tech.miroslav.caloriecounter.common.UnauthorizedException
import tech.miroslav.caloriecounter.security.CurrentUser
import java.time.LocalDate
import java.util.*

@RestController
@RequestMapping("/api/v1/training-logs")
@Tag(name = "Training Logs")
class TrainingLogsController(
    private val trainingLogService: TrainingLogService
) {

    @GetMapping
    @Operation(summary = "List training logs for current user (fixed size pagination)")
    fun list(
        @RequestParam(required = false)
        @Parameter(description = "Filter by exact date (yyyy-MM-dd)")
        date: LocalDate?,
        @RequestParam(defaultValue = "0")
        @Parameter(description = "0-based page index")
        page: Int
    ): ResponseEntity<PageResponse<TrainingLogDto>> {
        val principal = CurrentUser.principalOrNull() ?: throw UnauthorizedException("Unauthorized")
        val resp = trainingLogService.list(principal.authUserId, date, page)
        return ResponseEntity.ok(resp)
    }

    @PostMapping
    @Operation(summary = "Create a training log for a date (unique per user+date)")
    fun create(@RequestBody @Valid req: CreateTrainingLogRequest): ResponseEntity<TrainingLogDto> {
        val principal = CurrentUser.principalOrNull() ?: throw UnauthorizedException("Unauthorized")
        val dto = trainingLogService.create(principal.authUserId, req)
        return ResponseEntity.status(201).body(dto)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a training log by id for current user")
    fun get(@PathVariable id: UUID): ResponseEntity<TrainingLogDto> {
        val principal = CurrentUser.principalOrNull() ?: throw UnauthorizedException("Unauthorized")
        val dto = trainingLogService.get(principal.authUserId, id)
        return ResponseEntity.ok(dto)
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a training log (date and/or comment)")
    fun update(@PathVariable id: UUID, @RequestBody @Valid req: UpdateTrainingLogRequest): ResponseEntity<TrainingLogDto> {
        val principal = CurrentUser.principalOrNull() ?: throw UnauthorizedException("Unauthorized")
        val dto = trainingLogService.update(principal.authUserId, id, req)
        return ResponseEntity.ok(dto)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a training log (soft) and all its entries (hard)")
    fun delete(@PathVariable id: UUID): ResponseEntity<Void> {
        val principal = CurrentUser.principalOrNull() ?: throw UnauthorizedException("Unauthorized")
        trainingLogService.delete(principal.authUserId, id)
        return ResponseEntity.noContent().build()
    }
}
