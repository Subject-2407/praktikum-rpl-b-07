package com.scapes.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SearchQueryTest {
    @Test
    fun normalizeReturnsNullForBlankQuery() {
        assertNull(SearchQuery.normalize("   "))
    }

    @Test
    fun normalizeTrimsQuery() {
        assertEquals("nature", SearchQuery.normalize("  nature  "))
    }

    @Test
    fun normalizeLimitsQueryLength() {
        val query = "a".repeat(SearchQuery.MAX_LENGTH + 1)

        val normalizedQuery = SearchQuery.normalize(query)

        assertEquals(SearchQuery.MAX_LENGTH, normalizedQuery?.length)
        assertTrue(normalizedQuery?.all { character -> character == 'a' } == true)
    }
}
