package tech.miroslav.caloriecounter.user

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import tech.miroslav.caloriecounter.common.UnauthorizedException
import tech.miroslav.caloriecounter.security.CurrentUser

@RestController
@RequestMapping("/api/v1/users/me")
@Tag(name = "Users")
class UsersMeController(
    private val userProfileService: UserProfileService
) {

    @GetMapping
    @Operation(summary = "Get current user's profile with BMI and estimated daily intake")
    fun getMe(): ResponseEntity<UserProfileDto> {
        val principal = CurrentUser.principalOrNull() ?: throw UnauthorizedException("Unauthorized")
        val dto = userProfileService.getMyProfile(principal.authUserId)
        return ResponseEntity.ok(dto)
    }

    @PutMapping
    @Operation(summary = "Update current user's profile (partial)")
    fun updateMe(@RequestBody @Valid req: UpdateUserProfileRequest): ResponseEntity<UserProfileDto> {
        val principal = CurrentUser.principalOrNull() ?: throw UnauthorizedException("Unauthorized")
        val dto = userProfileService.updateMyProfile(principal.authUserId, req)
        return ResponseEntity.ok(dto)
    }
}
