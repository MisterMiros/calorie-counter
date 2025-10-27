package tech.miroslav.caloriecounter.user

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import tech.miroslav.caloriecounter.common.UnauthorizedException
import tech.miroslav.caloriecounter.security.CurrentUser
import java.util.*

@RestController
@RequestMapping("/api/v1/users/me/weights")
@Tag(name = "Users: Weight History")
class UsersMeWeightsController(
    private val weightHistoryService: WeightHistoryService
) {

    @GetMapping
    @Operation(summary = "List current user's weight history (desc by time)")
    fun list(): ResponseEntity<List<WeightHistoryDto>> {
        val principal = CurrentUser.principalOrNull() ?: throw UnauthorizedException("Unauthorized")
        val items = weightHistoryService.list(principal.appUserId)
        return ResponseEntity.ok(items)
    }

    @PostMapping
    @Operation(summary = "Add a weight history entry; also updates current weight in profile")
    fun add(@RequestBody @Valid req: CreateWeightHistoryRequest): ResponseEntity<WeightHistoryDto> {
        val principal = CurrentUser.principalOrNull() ?: throw UnauthorizedException("Unauthorized")
        val dto = weightHistoryService.add(principal.appUserId, req)
        return ResponseEntity.status(201).body(dto)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a weight history entry by id")
    fun delete(@PathVariable id: UUID): ResponseEntity<Void> {
        val principal = CurrentUser.principalOrNull() ?: throw UnauthorizedException("Unauthorized")
        weightHistoryService.delete(principal.appUserId, id)
        return ResponseEntity.noContent().build()
    }
}
