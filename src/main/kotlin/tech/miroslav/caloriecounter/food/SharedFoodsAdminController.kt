package tech.miroslav.caloriecounter.food

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import tech.miroslav.caloriecounter.common.AppConstants

import java.util.*

@RestController
@RequestMapping("/api/v1/admin/shared-foods")
@Tag(name = "Foods: Shared (Admin)")
class SharedFoodsAdminController(
    private val foodService: FoodService
) {

    private val sharedOwnerId = AppConstants.SHARED_AUTH_USER_ID

    @PostMapping
    @Operation(summary = "Create a shared food item (admin)")
    fun create(@RequestBody @Valid req: CreateFoodRequest): ResponseEntity<FoodDto> {
        val dto = foodService.create(sharedOwnerId, req)
        return ResponseEntity.status(201).body(dto)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a shared food item by id (admin)")
    fun get(@PathVariable id: UUID): ResponseEntity<FoodDto> {
        val dto = foodService.get(sharedOwnerId, id)
        return ResponseEntity.ok(dto)
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a shared food item (admin)")
    fun update(@PathVariable id: UUID, @RequestBody @Valid req: UpdateFoodRequest): ResponseEntity<FoodDto> {
        val dto = foodService.update(sharedOwnerId, id, req)
        return ResponseEntity.ok(dto)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete a shared food item (admin)")
    fun delete(@PathVariable id: UUID): ResponseEntity<Void> {
        foodService.delete(sharedOwnerId, id)
        return ResponseEntity.noContent().build()
    }
}
