package tech.miroslav.caloriecounter.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthFilter(
    private val jwtService: JwtService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val header = request.getHeader(HttpHeaders.AUTHORIZATION)
        if (header != null && header.startsWith("Bearer ")) {
            val token = header.substringAfter("Bearer ")
            try {
                val claims = jwtService.verify(token)
                val authorities = mutableListOf(SimpleGrantedAuthority("ROLE_USER"))
                if (claims.isAdmin) authorities.add(SimpleGrantedAuthority("ROLE_ADMIN"))
                val authentication = UsernamePasswordAuthenticationToken(
                    JwtPrincipal(
                        username = claims.username,
                        authUserId = claims.authUserId,
                        appUserId = claims.appUserId,
                        isAdmin = claims.isAdmin
                    ),
                    null,
                    authorities
                )
                SecurityContextHolder.getContext().authentication = authentication
            } catch (ex: Exception) {
                // Invalid token; clear context, proceed so entry points can handle 401 where required
                SecurityContextHolder.clearContext()
            }
        }
        filterChain.doFilter(request, response)
    }
}

/**
 * Principal stored in SecurityContext extracted from JWT claims.
 */
data class JwtPrincipal(
    val username: String,
    val authUserId: java.util.UUID,
    val appUserId: java.util.UUID,
    val isAdmin: Boolean
)
