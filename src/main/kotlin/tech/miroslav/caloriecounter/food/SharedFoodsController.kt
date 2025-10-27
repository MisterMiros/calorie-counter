package tech.miroslav.caloriecounter.food

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import tech.miroslav.caloriecounter.common.AppConstants
import tech.miroslav.caloriecounter.common.PageResponse
import java.util.*

@RestController
@RequestMapping("/api/v1/shared-foods")
@Tag(name = "Foods: Shared (Read-only)")
class SharedFoodsController(
    private val foodService: FoodService
) {
    private val sharedOwnerId = AppConstants.SHARED_AUTH_USER_ID

    @GetMapping
    @Operation(summary = "List shared food items (fixed size pagination)")
    fun list(
        @RequestParam(required = false)
        @Parameter(description = "Free-text query on name or producer")
        query: String?,
        @RequestParam(defaultValue = "0")
        @Parameter(description = "0-based page index")
        page: Int
    ): ResponseEntity<PageResponse<FoodDto>> {
        val resp = foodService.listShared(query, page)
        return ResponseEntity.ok(resp)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a shared food item by id (read-only for all authenticated users)")
    fun get(@PathVariable id: UUID): ResponseEntity<FoodDto> {
        val dto = foodService.get(sharedOwnerId, id)
        return ResponseEntity.ok(dto)
    }
}
