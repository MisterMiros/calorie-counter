package tech.miroslav.caloriecounter.food

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tech.miroslav.caloriecounter.common.AppConstants
import tech.miroslav.caloriecounter.common.BadRequestException
import tech.miroslav.caloriecounter.common.NotFoundException
import java.time.OffsetDateTime
import java.util.*

@Service
class FoodService(
    private val foodRepository: FoodRepository,
    private val foodMacrosRepository: FoodMacrosRepository,
    private val foodTranslationRepository: FoodTranslationRepository
) {
    private val allowedTypes = setOf("ingredient", "homemade", "restaurant", "product")

    @Transactional
    fun create(ownerId: UUID, req: CreateFoodRequest): FoodDto {
        validateType(req.type)
        validateMeasurements(req.packG, req.itemG, req.densityGPerMl)
        if (req.translations.isEmpty()) throw BadRequestException("At least one translation is required")

        val now = OffsetDateTime.now()
        val food = Food(
            ownerId = ownerId,
            type = req.type.trim(),
            densityGPerMl = req.densityGPerMl,
            packG = req.packG,
            itemG = req.itemG,
            createdAt = now,
            updatedAt = now,
            deletedAt = null
        )
        val savedFood = foodRepository.save(food)
        // macros
        val macros = FoodMacros(
            foodId = savedFood.id,
            proteinG = req.proteinGPer100g,
            fatG = req.fatGPer100g,
            carbG = req.carbGPer100g
        )
        foodMacrosRepository.save(macros)
        // translations
        val translations = req.translations.map { t ->
            FoodTranslation(
                foodId = savedFood.id,
                locale = t.locale.trim(),
                name = t.name.trim(),
                producer = t.producer?.trim()
            )
        }
        foodTranslationRepository.saveAll(translations)

        return toDto(savedFood, macros, translations)
    }

    @Transactional(readOnly = true)
    fun get(ownerId: UUID, id: UUID): FoodDto {
        val food = foodRepository.findById(id).orElseThrow { NotFoundException("Food not found") }
        if (food.ownerId != ownerId) throw NotFoundException("Food not found")
        val macros = foodMacrosRepository.findById(id).orElseThrow { NotFoundException("Food macros not found") }
        val translations = foodTranslationRepository.findByFoodId(id)
        return toDto(food, macros, translations)
    }

    @Transactional
    fun update(ownerId: UUID, id: UUID, req: UpdateFoodRequest): FoodDto {
        val food = foodRepository.findById(id).orElseThrow { NotFoundException("Food not found") }
        if (food.ownerId != ownerId) throw NotFoundException("Food not found")
        if (food.deletedAt != null) throw BadRequestException("Cannot update a deleted food")

        req.type?.let { validateType(it); food.type = it.trim() }
        validateMeasurements(req.packG, req.itemG, req.densityGPerMl)
        req.densityGPerMl?.let { food.densityGPerMl = it }
        req.packG?.let { food.packG = it }
        req.itemG?.let { food.itemG = it }

        val now = OffsetDateTime.now()
        food.updatedAt = now
        val savedFood = foodRepository.save(food)

        // macros update
        val macros = foodMacrosRepository.findById(id).orElseThrow { NotFoundException("Food macros not found") }
        req.proteinGPer100g?.let { macros.proteinG = it }
        req.fatGPer100g?.let { macros.fatG = it }
        req.carbGPer100g?.let { macros.carbG = it }
        val savedMacros = foodMacrosRepository.save(macros)

        // translations replace if provided
        val translations = if (req.translations != null) {
            val existing = foodTranslationRepository.findByFoodId(id)
            if (existing.isNotEmpty()) foodTranslationRepository.deleteAll(existing)
            val newOnes = req.translations.map { t ->
                FoodTranslation(
                    foodId = id,
                    locale = t.locale.trim(),
                    name = t.name.trim(),
                    producer = t.producer?.trim()
                )
            }
            foodTranslationRepository.saveAll(newOnes)
            newOnes
        } else {
            foodTranslationRepository.findByFoodId(id)
        }

        return toDto(savedFood, savedMacros, translations)
    }

    @Transactional
    fun delete(ownerId: UUID, id: UUID) {
        val food = foodRepository.findById(id).orElseThrow { NotFoundException("Food not found") }
        if (food.ownerId != ownerId) throw NotFoundException("Food not found")
        if (food.deletedAt == null) {
            val now = OffsetDateTime.now()
            food.deletedAt = now
            food.updatedAt = now
            foodRepository.save(food)
        }
    }

    private fun validateType(type: String) {
        val t = type.trim().lowercase()
        if (!allowedTypes.contains(t)) throw BadRequestException("Invalid type")
    }

    private fun validateMeasurements(packG: java.math.BigDecimal?, itemG: java.math.BigDecimal?, density: java.math.BigDecimal?) {
        // Values are validated via annotations (> 0) if provided; no cross-field constraints for now
        // Additional checks can be added when units are used
    }

    private fun toDto(food: Food, macros: FoodMacros, translations: List<FoodTranslation>): FoodDto = FoodDto(
        id = food.id.toString(),
        ownerId = food.ownerId.toString(),
        type = food.type,
        densityGPerMl = food.densityGPerMl,
        packG = food.packG,
        itemG = food.itemG,
        proteinGPer100g = macros.proteinG,
        fatGPer100g = macros.fatG,
        carbGPer100g = macros.carbG,
        translations = translations.map { FoodTranslationPayload(locale = it.locale, name = it.name, producer = it.producer) },
        createdAt = food.createdAt,
        updatedAt = food.updatedAt,
        deletedAt = food.deletedAt
    )
}
