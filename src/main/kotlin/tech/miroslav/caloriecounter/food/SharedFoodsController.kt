package tech.miroslav.caloriecounter.food

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import tech.miroslav.caloriecounter.common.AppConstants
import java.util.*

@RestController
@RequestMapping("/api/v1/shared-foods")
@Tag(name = "Foods: Shared (Read-only)")
class SharedFoodsController(
    private val foodService: FoodService
) {
    private val sharedOwnerId = AppConstants.SHARED_AUTH_USER_ID

    @GetMapping("/{id}")
    @Operation(summary = "Get a shared food item by id (read-only for all authenticated users)")
    fun get(@PathVariable id: UUID): ResponseEntity<FoodDto> {
        val dto = foodService.get(sharedOwnerId, id)
        return ResponseEntity.ok(dto)
    }
}
