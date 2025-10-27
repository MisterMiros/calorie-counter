package tech.miroslav.caloriecounter.common

/** Generic page response with fixed server-side size. */
data class PageResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int
)
