package com.scapes.domain.model

/** Search-driven category entry used by the landing feed. */
data class TrendingCategory(
    val label: String,
    val queryValue: String,
    val slug: String,
    val origin: TrendingCategoryOrigin,
    val searchCount: Int = 0,
    val score: Double = 0.0,
    val topKeywords: List<String> = emptyList(),
)

/** Origin of a landing category entry. */
enum class TrendingCategoryOrigin {
    USER_KEYWORD,
    SYSTEM,
}
