package tech.miroslav.caloriecounter.diary

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
@RequestMapping("/api/v1/diaries")
@Tag(name = "Diaries")
class DiariesController(
    private val diaryService: DiaryService,
    private val diarySummaryService: DiarySummaryService
) {

    @GetMapping
    @Operation(summary = "List diaries for current user (fixed size pagination)")
    fun list(
        @RequestParam(required = false)
        @Parameter(description = "Filter by exact date (yyyy-MM-dd)")
        date: LocalDate?,
        @RequestParam(defaultValue = "0")
        @Parameter(description = "0-based page index")
        page: Int
    ): ResponseEntity<PageResponse<DiaryDto>> {
        val principal = CurrentUser.principalOrNull() ?: throw UnauthorizedException("Unauthorized")
        val resp = diaryService.list(principal.authUserId, date, page)
        return ResponseEntity.ok(resp)
    }

    @PostMapping
    @Operation(summary = "Create a diary for a date (unique per user+date)")
    fun create(@RequestBody @Valid req: CreateDiaryRequest): ResponseEntity<DiaryDto> {
        val principal = CurrentUser.principalOrNull() ?: throw UnauthorizedException("Unauthorized")
        val dto = diaryService.create(principal.authUserId, req)
        return ResponseEntity.status(201).body(dto)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a diary by id for current user")
    fun get(@PathVariable id: UUID): ResponseEntity<DiaryDto> {
        val principal = CurrentUser.principalOrNull() ?: throw UnauthorizedException("Unauthorized")
        val dto = diaryService.get(principal.authUserId, id)
        return ResponseEntity.ok(dto)
    }

    @GetMapping("/{id}/summary")
    @Operation(summary = "Get calculated summary for a diary")
    fun summary(@PathVariable id: UUID): ResponseEntity<DiarySummaryDto> {
        val principal = CurrentUser.principalOrNull() ?: throw UnauthorizedException("Unauthorized")
        val dto = diarySummaryService.summarize(principal.authUserId, id)
        return ResponseEntity.ok(dto)
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a diary (date and/or comment)")
    fun update(@PathVariable id: UUID, @RequestBody @Valid req: UpdateDiaryRequest): ResponseEntity<DiaryDto> {
        val principal = CurrentUser.principalOrNull() ?: throw UnauthorizedException("Unauthorized")
        val dto = diaryService.update(principal.authUserId, id, req)
        return ResponseEntity.ok(dto)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a diary (soft) and all its entries (hard)")
    fun delete(@PathVariable id: UUID): ResponseEntity<Void> {
        val principal = CurrentUser.principalOrNull() ?: throw UnauthorizedException("Unauthorized")
        diaryService.delete(principal.authUserId, id)
        return ResponseEntity.noContent().build()
    }
}
