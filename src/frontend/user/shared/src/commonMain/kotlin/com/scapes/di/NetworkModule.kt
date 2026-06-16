package com.scapes.di

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

private const val REQUEST_TIMEOUT_MILLIS = 10_000L
private const val CONNECT_TIMEOUT_MILLIS = 5_000L

/** Creates the shared JSON parser configuration. */
fun createJson(): Json = Json {
    ignoreUnknownKeys = true
    isLenient = false
}

/** Creates a platform-backed HTTP client. */
fun createHttpClient(): HttpClient = HttpClient { configureScapesClient() }

/** Creates an HTTP client with an injected [engine], mainly for tests. */
fun createHttpClient(engine: HttpClientEngine): HttpClient =
    HttpClient(engine) { configureScapesClient() }

private fun HttpClientConfig<*>.configureScapesClient() {
    install(ContentNegotiation) { json(createJson()) }

    install(HttpTimeout) {
        requestTimeoutMillis = REQUEST_TIMEOUT_MILLIS
        connectTimeoutMillis = CONNECT_TIMEOUT_MILLIS
    }

    install(Logging) {
        level = LogLevel.HEADERS
        sanitizeHeader { header -> header == HttpHeaders.Authorization }
    }
}
