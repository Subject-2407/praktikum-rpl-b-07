package com.scapes.domain.model

/** Search suggestion item surfaced to the shared UI. */
data class SearchRecommendation(
    val type: SearchRecommendationType,
    val label: String,
    val queryValue: String,
    val score: Double = 0.0,
    val matchReason: String = "",
)

/** Recommendation kind returned by search suggestion endpoints. */
enum class SearchRecommendationType {
    TAG,
    SYSTEM_CATEGORY,
    USER_KEYWORD_CATEGORY,
}
