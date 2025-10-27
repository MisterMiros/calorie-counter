package tech.miroslav.caloriecounter.diary

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import tech.miroslav.caloriecounter.common.UnauthorizedException
import tech.miroslav.caloriecounter.security.CurrentUser
import java.util.*

@RestController
@RequestMapping("/api/v1/diaries/{diaryId}/entries")
@Tag(name = "Diaries: Entries")
class DiaryEntriesController(
    private val diaryEntryService: DiaryEntryService
) {

    @GetMapping
    @Operation(summary = "List entries for a diary")
    fun list(@PathVariable diaryId: UUID): ResponseEntity<List<DiaryEntryDto>> {
        val principal = CurrentUser.principalOrNull() ?: throw UnauthorizedException("Unauthorized")
        val items = diaryEntryService.list(principal.authUserId, diaryId)
        return ResponseEntity.ok(items)
    }

    @PostMapping
    @Operation(summary = "Create a diary entry")
    fun create(
        @PathVariable diaryId: UUID,
        @RequestBody @Valid req: CreateDiaryEntryRequest
    ): ResponseEntity<DiaryEntryDto> {
        val principal = CurrentUser.principalOrNull() ?: throw UnauthorizedException("Unauthorized")
        val dto = diaryEntryService.create(principal.authUserId, diaryId, req)
        return ResponseEntity.status(201).body(dto)
    }

    @GetMapping("/{entryId}")
    @Operation(summary = "Get a diary entry by id")
    fun get(@PathVariable diaryId: UUID, @PathVariable entryId: UUID): ResponseEntity<DiaryEntryDto> {
        val principal = CurrentUser.principalOrNull() ?: throw UnauthorizedException("Unauthorized")
        val dto = diaryEntryService.get(principal.authUserId, diaryId, entryId)
        return ResponseEntity.ok(dto)
    }

    @PutMapping("/{entryId}")
    @Operation(summary = "Update a diary entry")
    fun update(
        @PathVariable diaryId: UUID,
        @PathVariable entryId: UUID,
        @RequestBody @Valid req: UpdateDiaryEntryRequest
    ): ResponseEntity<DiaryEntryDto> {
        val principal = CurrentUser.principalOrNull() ?: throw UnauthorizedException("Unauthorized")
        val dto = diaryEntryService.update(principal.authUserId, diaryId, entryId, req)
        return ResponseEntity.ok(dto)
    }

    @DeleteMapping("/{entryId}")
    @Operation(summary = "Delete a diary entry")
    fun delete(@PathVariable diaryId: UUID, @PathVariable entryId: UUID): ResponseEntity<Void> {
        val principal = CurrentUser.principalOrNull() ?: throw UnauthorizedException("Unauthorized")
        diaryEntryService.delete(principal.authUserId, diaryId, entryId)
        return ResponseEntity.noContent().build()
    }
}
