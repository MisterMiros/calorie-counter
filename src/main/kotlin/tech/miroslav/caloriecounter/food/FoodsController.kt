package tech.miroslav.caloriecounter.food

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
@RequestMapping("/api/v1/foods")
@Tag(name = "Foods (User-owned)")
class FoodsController(
    private val foodService: FoodService
) {

    @GetMapping
    @Operation(summary = "List user-owned food items (fixed size pagination)")
    fun list(
        @RequestParam(required = false)
        @Parameter(description = "Free-text query on name or producer")
        query: String?,
        @RequestParam(defaultValue = "0")
        @Parameter(description = "0-based page index")
        page: Int
    ): ResponseEntity<PageResponse<FoodDto>> {
        val principal = CurrentUser.principalOrNull() ?: throw UnauthorizedException("Unauthorized")
        val resp = foodService.listOwned(principal.authUserId, query, page)
        return ResponseEntity.ok(resp)
    }

    @PostMapping
    @Operation(summary = "Create a user-owned food item")
    fun create(@RequestBody @Valid req: CreateFoodRequest): ResponseEntity<FoodDto> {
        val principal = CurrentUser.principalOrNull() ?: throw UnauthorizedException("Unauthorized")
        val dto = foodService.create(principal.authUserId, req)
        return ResponseEntity.status(201).body(dto)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a user-owned food item by id (returns even if soft-deleted)")
    fun get(@PathVariable id: UUID): ResponseEntity<FoodDto> {
        val principal = CurrentUser.principalOrNull() ?: throw UnauthorizedException("Unauthorized")
        val dto = foodService.get(principal.authUserId, id)
        return ResponseEntity.ok(dto)
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a user-owned food item (forbidden if soft-deleted)")
    fun update(@PathVariable id: UUID, @RequestBody @Valid req: UpdateFoodRequest): ResponseEntity<FoodDto> {
        val principal = CurrentUser.principalOrNull() ?: throw UnauthorizedException("Unauthorized")
        val dto = foodService.update(principal.authUserId, id, req)
        return ResponseEntity.ok(dto)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete a user-owned food item")
    fun delete(@PathVariable id: UUID): ResponseEntity<Void> {
        val principal = CurrentUser.principalOrNull() ?: throw UnauthorizedException("Unauthorized")
        foodService.delete(principal.authUserId, id)
        return ResponseEntity.noContent().build()
    }
}
