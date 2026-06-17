package com.scapes.data.remote.api

import com.scapes.data.remote.config.WallpaperApiConfig
import com.scapes.data.remote.dto.ApiEnvelopeDto
import com.scapes.data.remote.dto.ApiEnvelopeWithMetaDto
import com.scapes.data.remote.dto.ApiSourceDto
import com.scapes.data.remote.dto.AcceptedDto
import com.scapes.data.remote.dto.CategoryDto
import com.scapes.data.remote.dto.PexelsPhotoDto
import com.scapes.data.remote.dto.PexelsSearchResponseDto
import com.scapes.data.remote.dto.PixabaySearchResponseDto
import com.scapes.data.remote.dto.ScapesWallpaperDto
import com.scapes.data.remote.dto.SearchRecommendationDto
import com.scapes.data.remote.dto.SearchRecommendationMetaDto
import com.scapes.data.remote.dto.TagDto
import com.scapes.data.remote.dto.TrendingCategoryDto
import com.scapes.data.remote.dto.UnsplashSearchResponseDto
import com.scapes.domain.model.ErrorCode
import com.scapes.domain.model.ScapesResult
import com.scapes.domain.model.SearchRecommendation
import com.scapes.domain.model.SearchRecommendationType
import com.scapes.domain.model.TargetDevice
import com.scapes.domain.model.TrendingCategory
import com.scapes.domain.model.TrendingCategoryOrigin
import com.scapes.domain.model.Wallpaper
import com.scapes.domain.model.WallpaperCategory
import com.scapes.domain.model.WallpaperSource
import com.scapes.domain.model.WallpaperSourceInfo
import com.scapes.domain.model.WallpaperTag
import com.scapes.domain.repository.ApiKeyRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.TimeMark
import kotlin.time.TimeSource

private const val PageSize = 24
private const val CacheMaxEntries = 96
private const val MaxDownloadBytes = 10 * 1024 * 1024
private const val DefaultRecommendationLimit = 10
private const val DefaultTrendingLimit = 10
private val CacheTtl = 30.minutes

class ExternalWallpaperApi(
    private val httpClient: HttpClient,
    private val apiKeyRepository: ApiKeyRepository? = null,
    private val config: WallpaperApiConfig = WallpaperApiConfig(),
) {
    private val cache = WallpaperSearchMemoryCache()

    suspend fun getWallpaperSources(): ScapesResult<List<WallpaperSourceInfo>> =
        runCatching {
                val response = httpClient.get("${config.scapesBaseUrl.trimEnd('/')}/sources")
                when (response.status) {
                    HttpStatusCode.OK -> {
                        val envelope = response.body<ApiEnvelopeDto<List<ApiSourceDto>>>()
                        ScapesResult.Success(envelope.data.mapNotNull(::sourceInfo))
                    }

                    else -> providerError("Scapes", response.status.value)
                }
            }
            .getOrElse { networkError("Scapes", it) }

    suspend fun getCategories(): ScapesResult<List<WallpaperCategory>> =
        runCatching {
                val response = httpClient.get("${config.scapesBaseUrl.trimEnd('/')}/categories")
                when (response.status) {
                    HttpStatusCode.OK -> {
                        val envelope = response.body<ApiEnvelopeDto<List<CategoryDto>>>()
                        ScapesResult.Success(envelope.data.map { item -> item.toWallpaperCategory() })
                    }

                    else -> providerError("Scapes", response.status.value)
                }
            }
            .getOrElse { networkError("Scapes", it) }

    suspend fun getTrendingCategories(
        source: WallpaperSource,
        limit: Int = DefaultTrendingLimit,
    ): ScapesResult<List<TrendingCategory>> =
        runCatching {
                val response =
                    httpClient.get("${config.scapesBaseUrl.trimEnd('/')}/categories/trending") {
                        parameter("limit", limit.coerceIn(1, 50))
                        parameter("include_system", source == WallpaperSource.SCAPES_API)
                    }

                when (response.status) {
                    HttpStatusCode.OK -> {
                        val envelope =
                            response.body<ApiEnvelopeDto<List<TrendingCategoryDto>>>()
                        ScapesResult.Success(
                            envelope.data.mapNotNull { item -> item.toTrendingCategory() }
                        )
                    }

                    HttpStatusCode.BadRequest ->
                        validationError("Scapes rejected the trending category request.")
                    else -> providerError("Scapes", response.status.value)
                }
            }
            .getOrElse { networkError("Scapes", it) }

    suspend fun getSearchRecommendations(
        query: String,
        source: WallpaperSource,
        limit: Int = DefaultRecommendationLimit,
    ): ScapesResult<List<SearchRecommendation>> =
        runCatching {
                val response =
                    httpClient.get("${config.scapesBaseUrl.trimEnd('/')}/recommendations/search") {
                        parameter("q", query)
                        parameter("source_slug", source.apiSlug())
                        parameter("limit", limit.coerceIn(1, 50))
                    }

                when (response.status) {
                    HttpStatusCode.OK -> {
                        val envelope =
                            response.body<
                                ApiEnvelopeWithMetaDto<
                                    List<SearchRecommendationDto>,
                                    SearchRecommendationMetaDto
                                >
                            >()
                        ScapesResult.Success(
                            envelope.data.mapNotNull { item -> item.toSearchRecommendation() }
                        )
                    }

                    HttpStatusCode.BadRequest ->
                        validationError("Scapes rejected the search recommendation request.")
                    else -> providerError("Scapes", response.status.value)
                }
            }
            .getOrElse { networkError("Scapes", it) }

    suspend fun logSearchEvent(
        query: String,
        source: WallpaperSource,
        resultCount: Int? = null,
    ): ScapesResult<Unit> =
        runCatching {
                val response =
                    httpClient.post("${config.scapesBaseUrl.trimEnd('/')}/search-logs") {
                        contentType(ContentType.Application.Json)
                        setBody(
                            SearchLogRequestDto(
                                keyword = query,
                                sourceSlug = source.apiSlug(),
                                resultCount = resultCount,
                            )
                        )
                    }

                when (response.status) {
                    HttpStatusCode.Accepted,
                    HttpStatusCode.OK -> {
                        response.body<ApiEnvelopeDto<AcceptedDto>>()
                        ScapesResult.Success(Unit)
                    }

                    HttpStatusCode.BadRequest ->
                        validationError("Scapes rejected the search analytics request.")
                    else -> providerError("Scapes", response.status.value)
                }
            }
            .getOrElse { networkError("Scapes", it) }

    suspend fun searchWallpapers(
        query: String,
        page: Int,
        source: WallpaperSource,
        targetDevice: TargetDevice = TargetDevice.DESKTOP,
        categorySlug: String? = null,
    ): ScapesResult<List<Wallpaper>> {
        val credential = resolveCredential(source) ?: return missingKey(providerName(source))
        val cacheKey =
            WallpaperSearchCacheKey(
                source = source,
                normalizedQuery = query.trim().lowercase(),
                page = page,
                targetDevice = targetDevice,
                normalizedCategorySlug = categorySlug?.trim()?.lowercase().orEmpty(),
                credentialFingerprint = credential.fingerprint,
            )
        cache.get(cacheKey)?.let { cachedWallpapers ->
            return ScapesResult.Success(cachedWallpapers)
        }

        val result =
            when (source) {
                WallpaperSource.SCAPES_API -> searchScapes(query, page, targetDevice, categorySlug)
                WallpaperSource.PEXELS -> searchPexels(query, page, credential.value, targetDevice)
                WallpaperSource.UNSPLASH ->
                    searchUnsplash(query, page, credential.value, targetDevice)
                WallpaperSource.PIXABAY ->
                    searchPixabay(query, page, credential.value, targetDevice)
            }

        if (result is ScapesResult.Success) {
            cache.put(cacheKey, result.data)
        }
        return result
    }

    suspend fun downloadWallpaper(wallpaper: Wallpaper): ScapesResult<WallpaperDownload> =
        runCatching {
                val response = httpClient.get(wallpaper.fullImageUrl)
                when (response.status) {
                    HttpStatusCode.OK -> {
                        val contentType =
                            response.headers[HttpHeaders.ContentType].orEmpty().substringBefore(";")
                        val extension = imageExtension(contentType, wallpaper.fullImageUrl)
                        if (extension == null) {
                            return@runCatching ScapesResult.Error(
                                code = ErrorCode.VALIDATION,
                                message = "Wallpaper format must be JPG, PNG, or WebP.",
                            )
                        }

                        val bytes = response.body<ByteArray>()
                        if (bytes.size > MaxDownloadBytes) {
                            return@runCatching ScapesResult.Error(
                                code = ErrorCode.VALIDATION,
                                message = "Wallpaper file must not exceed 10 MB.",
                            )
                        }

                        ScapesResult.Success(
                            WallpaperDownload(
                                bytes = bytes,
                                mimeType = contentType,
                                extension = extension,
                            )
                        )
                    }

                    else -> providerError(providerName(wallpaper.source), response.status.value)
                }
            }
            .getOrElse { networkError(providerName(wallpaper.source), it) }

    suspend fun validateApiKey(source: WallpaperSource, apiKey: String): ScapesResult<Unit> {
        val trimmedKey = apiKey.trim()
        if (trimmedKey.isBlank()) {
            return ScapesResult.Error(
                code = ErrorCode.VALIDATION,
                message = "API key cannot be empty.",
            )
        }

        return when (source) {
            WallpaperSource.PEXELS -> validatePexels(trimmedKey)
            WallpaperSource.UNSPLASH -> validateUnsplash(trimmedKey)
            WallpaperSource.PIXABAY -> validatePixabay(trimmedKey)
            WallpaperSource.SCAPES_API ->
                ScapesResult.Error(
                    code = ErrorCode.UNSUPPORTED_PLATFORM,
                    message = "Scapes does not use a personal API key.",
                )
        }
    }

    fun invalidateSource(source: WallpaperSource) {
        cache.invalidateSource(source)
    }

    private suspend fun searchScapes(
        query: String,
        page: Int,
        targetDevice: TargetDevice,
        categorySlug: String?,
    ): ScapesResult<List<Wallpaper>> =
        runCatching {
                val response =
                    httpClient.get("${config.scapesBaseUrl.trimEnd('/')}/wallpapers") {
                        parameter("q", query)
                        if (!categorySlug.isNullOrBlank()) {
                            parameter("category", categorySlug)
                        }
                        parameter("target_device", targetDevice.apiValue())
                        parameter("page", page)
                        parameter("per_page", PageSize)
                        parameter("sort_by", "published_at")
                        parameter("order", "desc")
                    }

                when (response.status) {
                    HttpStatusCode.OK -> {
                        val envelope = response.body<ApiEnvelopeDto<List<ScapesWallpaperDto>>>()
                        ScapesResult.Success(envelope.data.mapNotNull(::scapesWallpaper))
                    }

                    HttpStatusCode.BadRequest ->
                        validationError("Scapes rejected the search parameters.")
                    else -> providerError("Scapes", response.status.value)
                }
            }
            .getOrElse { networkError("Scapes", it) }

    private suspend fun searchPexels(
        query: String,
        page: Int,
        apiKey: String,
        targetDevice: TargetDevice,
    ): ScapesResult<List<Wallpaper>> =
        runCatching {
                val response =
                    httpClient.get("https://api.pexels.com/v1/search") {
                        header(HttpHeaders.Authorization, apiKey)
                        parameter("query", query)
                        parameter("orientation", targetDevice.providerOrientation())
                        parameter("size", "large")
                        parameter("page", page)
                        parameter("per_page", PageSize)
                    }

                when (response.status) {
                    HttpStatusCode.OK ->
                        ScapesResult.Success(
                            response
                                .body<PexelsSearchResponseDto>()
                                .photos
                                .filter { it.matches(targetDevice) }
                                .mapNotNull { photo -> pexelsWallpaper(photo, targetDevice) }
                        )

                    HttpStatusCode.Unauthorized,
                    HttpStatusCode.Forbidden -> unauthorized("Pexels")

                    else -> providerError("Pexels", response.status.value)
                }
            }
            .getOrElse { networkError("Pexels", it) }

    private suspend fun searchUnsplash(
        query: String,
        page: Int,
        accessKey: String,
        targetDevice: TargetDevice,
    ): ScapesResult<List<Wallpaper>> =
        runCatching {
                val response =
                    httpClient.get("https://api.unsplash.com/search/photos") {
                        header(HttpHeaders.Authorization, "Client-ID $accessKey")
                        header("Accept-Version", "v1")
                        parameter("query", query)
                        parameter("orientation", targetDevice.providerOrientation())
                        parameter("content_filter", "high")
                        parameter("page", page)
                        parameter("per_page", PageSize)
                    }

                when (response.status) {
                    HttpStatusCode.OK ->
                        ScapesResult.Success(
                            response
                                .body<UnsplashSearchResponseDto>()
                                .results
                                .filter { it.matches(targetDevice) }
                                .mapNotNull { photo ->
                                    val previewUrl =
                                        photo.urls.regular.ifBlank {
                                            photo.urls.small.ifBlank { photo.urls.thumb }
                                        }
                                    val fullUrl =
                                        photo.urls.full.ifBlank {
                                            photo.urls.regular.ifBlank { previewUrl }
                                        }

                                    if (previewUrl.isBlank() || fullUrl.isBlank()) {
                                        null
                                    } else {
                                        Wallpaper(
                                            id = "unsplash-${photo.id}",
                                            title =
                                                displayTitle(
                                                    photo.description
                                                        ?: photo.altDescription
                                                        ?: "Unsplash wallpaper"
                                                ),
                                            source = WallpaperSource.UNSPLASH,
                                            previewUrl = previewUrl,
                                            fullImageUrl = fullUrl,
                                            authorName = photo.user.name.ifBlank { "Unsplash" },
                                            width = photo.width,
                                            height = photo.height,
                                            targetDevice = targetDevice,
                                        )
                                    }
                                }
                        )

                    HttpStatusCode.Unauthorized,
                    HttpStatusCode.Forbidden -> unauthorized("Unsplash")

                    else -> providerError("Unsplash", response.status.value)
                }
            }
            .getOrElse { networkError("Unsplash", it) }

    private suspend fun searchPixabay(
        query: String,
        page: Int,
        apiKey: String,
        targetDevice: TargetDevice,
    ): ScapesResult<List<Wallpaper>> =
        runCatching {
                val response =
                    httpClient.get("https://pixabay.com/api/") {
                        parameter("key", apiKey)
                        parameter("q", query)
                        parameter("image_type", "photo")
                        parameter("orientation", targetDevice.providerOrientation())
                        parameter("safesearch", "true")
                        parameter("page", page)
                        parameter("per_page", PageSize)
                    }

                when (response.status) {
                    HttpStatusCode.OK ->
                        ScapesResult.Success(
                            response
                                .body<PixabaySearchResponseDto>()
                                .hits
                                .filter { it.matches(targetDevice) }
                                .mapNotNull { photo ->
                                    val previewUrl = photo.webformatURL.ifBlank { photo.previewURL }
                                    val fullUrl =
                                        photo.fullHDURL.ifBlank {
                                            photo.largeImageURL.ifBlank { photo.webformatURL }
                                        }

                                    if (previewUrl.isBlank() || fullUrl.isBlank()) {
                                        null
                                    } else {
                                        Wallpaper(
                                            id = "pixabay-${photo.id}",
                                            title = pixabayTitle(photo.tags),
                                            source = WallpaperSource.PIXABAY,
                                            previewUrl = previewUrl,
                                            fullImageUrl = fullUrl,
                                            authorName = photo.user.ifBlank { "Pixabay" },
                                            width = photo.imageWidth,
                                            height = photo.imageHeight,
                                            targetDevice = targetDevice,
                                        )
                                    }
                                }
                        )

                    HttpStatusCode.Unauthorized,
                    HttpStatusCode.Forbidden,
                    HttpStatusCode.BadRequest -> unauthorized("Pixabay")

                    else -> providerError("Pixabay", response.status.value)
                }
            }
            .getOrElse { networkError("Pixabay", it) }

    private suspend fun validatePexels(apiKey: String): ScapesResult<Unit> =
        runCatching {
                val response =
                    httpClient.get("https://api.pexels.com/v1/search") {
                        header(HttpHeaders.Authorization, apiKey)
                        parameter("query", "nature")
                        parameter("orientation", "portrait")
                        parameter("page", 1)
                        parameter("per_page", 1)
                    }
                validationResult("Pexels", response.status)
            }
            .getOrElse { networkError("Pexels", it) }

    private suspend fun validateUnsplash(accessKey: String): ScapesResult<Unit> =
        runCatching {
                val response =
                    httpClient.get("https://api.unsplash.com/search/photos") {
                        header(HttpHeaders.Authorization, "Client-ID $accessKey")
                        header("Accept-Version", "v1")
                        parameter("query", "nature")
                        parameter("orientation", "portrait")
                        parameter("page", 1)
                        parameter("per_page", 1)
                    }
                validationResult("Unsplash", response.status)
            }
            .getOrElse { networkError("Unsplash", it) }

    private suspend fun validatePixabay(apiKey: String): ScapesResult<Unit> =
        runCatching {
                val response =
                    httpClient.get("https://pixabay.com/api/") {
                        parameter("key", apiKey)
                        parameter("q", "nature")
                        parameter("image_type", "photo")
                        parameter("orientation", "vertical")
                        parameter("safesearch", "true")
                        parameter("page", 1)
                        parameter("per_page", 3)
                    }
                validationResult("Pixabay", response.status)
            }
            .getOrElse { networkError("Pixabay", it) }

    private suspend fun resolveCredential(source: WallpaperSource): ApiCredential? {
        if (source == WallpaperSource.SCAPES_API) {
            return ApiCredential(value = "public", fingerprint = "scapes-public")
        }

        val personalKey =
            when (val result = apiKeyRepository?.getApiKey(source)) {
                is ScapesResult.Success -> result.data?.value?.trim()
                else -> null
            }
        val fallbackKey =
            when (source) {
                WallpaperSource.PEXELS -> config.pexelsApiKey
                WallpaperSource.UNSPLASH -> config.unsplashAccessKey
                WallpaperSource.PIXABAY -> config.pixabayApiKey
                WallpaperSource.SCAPES_API -> ""
            }.trim()
        val value = personalKey?.takeIf { it.isNotBlank() } ?: fallbackKey

        return value
            .takeIf { it.isNotBlank() }
            ?.let { key ->
                ApiCredential(value = key, fingerprint = "${key.length}:${key.hashCode()}")
            }
    }

    private fun scapesWallpaper(photo: ScapesWallpaperDto): Wallpaper? {
        val previewUrl = photo.thumbnailPath.ifBlank { photo.filePath }
        val fullUrl = photo.filePath.ifBlank { previewUrl }
        if (previewUrl.isBlank() || fullUrl.isBlank()) {
            return null
        }

        return Wallpaper(
            id = "scapes-${photo.id}",
            title = displayTitle(photo.title.ifBlank { "Scapes wallpaper" }),
            source = WallpaperSource.SCAPES_API,
            previewUrl = previewUrl,
            fullImageUrl = fullUrl,
            description = photo.description,
            authorName = photo.contributor?.displayName?.ifBlank { null } ?: "Scapes",
            width = photo.width,
            height = photo.height,
            targetDevice = photo.targetDevice.toTargetDevice(),
            category = photo.category?.toWallpaperCategory(),
            tags = photo.tags.map { tag -> tag.toWallpaperTag() },
        )
    }

    private fun pexelsWallpaper(photo: PexelsPhotoDto, targetDevice: TargetDevice): Wallpaper? {
        val previewUrl =
            photo.src.large.ifBlank {
                photo.src.medium.ifBlank {
                    photo.src.portrait.ifBlank { photo.src.small.ifBlank { photo.src.tiny } }
                }
            }
        val fullUrl =
            photo.src.original.ifBlank {
                photo.src.large2x.ifBlank { photo.src.large.ifBlank { previewUrl } }
            }

        return if (previewUrl.isBlank() || fullUrl.isBlank()) {
            null
        } else {
            Wallpaper(
                id = "pexels-${photo.id}",
                title = pexelsTitle(photo),
                source = WallpaperSource.PEXELS,
                previewUrl = previewUrl,
                fullImageUrl = fullUrl,
                authorName = photo.photographer.ifBlank { "Pexels" },
                width = photo.width,
                height = photo.height,
                targetDevice = targetDevice,
            )
        }
    }

    private fun sourceInfo(dto: ApiSourceDto): WallpaperSourceInfo? {
        val source =
            when (dto.slug.lowercase()) {
                "scapes" -> WallpaperSource.SCAPES_API
                "pexels" -> WallpaperSource.PEXELS
                "unsplash" -> WallpaperSource.UNSPLASH
                "pixabay" -> WallpaperSource.PIXABAY
                else -> return null
            }

        return WallpaperSourceInfo(
            source = source,
            label = dto.name.ifBlank { providerName(source) },
            baseUrl = dto.baseUrl,
            isDefault = dto.isDefault,
            requiresPersonalApiKey = source != WallpaperSource.SCAPES_API,
        )
    }

    private fun validationResult(provider: String, status: HttpStatusCode): ScapesResult<Unit> =
        when (status) {
            HttpStatusCode.OK -> ScapesResult.Success(Unit)
            HttpStatusCode.Unauthorized,
            HttpStatusCode.Forbidden,
            HttpStatusCode.BadRequest -> unauthorized(provider)
            else -> providerError(provider, status.value)
        }

    private fun imageExtension(contentType: String, url: String): String? =
        when {
            contentType.equals("image/jpeg", ignoreCase = true) -> "jpg"
            contentType.equals("image/png", ignoreCase = true) -> "png"
            contentType.equals("image/webp", ignoreCase = true) -> "webp"
            url.substringBefore("?").endsWith(".jpg", ignoreCase = true) -> "jpg"
            url.substringBefore("?").endsWith(".jpeg", ignoreCase = true) -> "jpg"
            url.substringBefore("?").endsWith(".png", ignoreCase = true) -> "png"
            url.substringBefore("?").endsWith(".webp", ignoreCase = true) -> "webp"
            else -> null
        }

    private fun missingKey(providerName: String): ScapesResult.Error =
        ScapesResult.Error(
            code = ErrorCode.UNAUTHORIZED,
            message = "Add a $providerName API key in Settings before searching this source.",
        )

    private fun unauthorized(provider: String): ScapesResult.Error =
        ScapesResult.Error(
            code = ErrorCode.UNAUTHORIZED,
            message = "$provider rejected the configured API key.",
        )

    private fun validationError(message: String): ScapesResult.Error =
        ScapesResult.Error(code = ErrorCode.VALIDATION, message = message)

    private fun providerError(provider: String, statusCode: Int): ScapesResult.Error =
        ScapesResult.Error(
            code = ErrorCode.NETWORK,
            message = "$provider request failed with HTTP $statusCode.",
        )

    private fun networkError(provider: String, throwable: Throwable): ScapesResult.Error =
        ScapesResult.Error(
            code = ErrorCode.NETWORK,
            message = "$provider request failed: ${throwable.message.orEmpty()}",
        )

    private fun pexelsTitle(photo: PexelsPhotoDto): String =
        photo.alt.trim().takeIf { it.isNotBlank() }?.let(::displayTitle)
            ?: titleFromSlug(photo.url)
            ?: "Pexels wallpaper"

    private fun pixabayTitle(tags: String): String =
        tags.split(",").firstOrNull()?.trim()?.takeIf { it.isNotBlank() }?.let(::displayTitle)
            ?: "Pixabay wallpaper"

    private fun titleFromSlug(url: String): String? {
        val slug =
            url.trim()
                .trimEnd('/')
                .substringAfterLast("/")
                .replace(Regex("-?\\d+$"), "")
                .replace("-", " ")
                .trim()

        return slug.takeIf { it.isNotBlank() }?.let(::displayTitle)
    }

    private fun displayTitle(rawTitle: String): String =
        rawTitle.trim().replace(Regex("\\s+"), " ").take(MaxTitleLength).trim()

    private fun providerName(source: WallpaperSource): String =
        when (source) {
            WallpaperSource.PEXELS -> "Pexels"
            WallpaperSource.UNSPLASH -> "Unsplash"
            WallpaperSource.PIXABAY -> "Pixabay"
            WallpaperSource.SCAPES_API -> "Scapes"
        }

    private fun WallpaperSource.apiSlug(): String =
        when (this) {
            WallpaperSource.SCAPES_API -> "scapes"
            WallpaperSource.PEXELS -> "pexels"
            WallpaperSource.UNSPLASH -> "unsplash"
            WallpaperSource.PIXABAY -> "pixabay"
        }

    private fun String.toTargetDevice(): TargetDevice =
        when (lowercase()) {
            "mobile" -> TargetDevice.MOBILE
            "tablet" -> TargetDevice.TABLET
            else -> TargetDevice.DESKTOP
        }

    private fun TargetDevice.apiValue(): String =
        when (this) {
            TargetDevice.DESKTOP -> "desktop"
            TargetDevice.MOBILE -> "mobile"
            TargetDevice.TABLET -> "tablet"
        }

    private fun TargetDevice.providerOrientation(): String =
        when (this) {
            TargetDevice.DESKTOP -> "landscape"
            TargetDevice.MOBILE -> "portrait"
            TargetDevice.TABLET -> "landscape"
        }

    private fun PexelsPhotoDto.matches(targetDevice: TargetDevice): Boolean =
        when (targetDevice) {
            TargetDevice.MOBILE -> height > width
            TargetDevice.DESKTOP,
            TargetDevice.TABLET -> width >= height
        }

    private fun com.scapes.data.remote.dto.UnsplashPhotoDto.matches(
        targetDevice: TargetDevice
    ): Boolean =
        when (targetDevice) {
            TargetDevice.MOBILE -> height > width
            TargetDevice.DESKTOP,
            TargetDevice.TABLET -> width >= height
        }

    private fun com.scapes.data.remote.dto.PixabayPhotoDto.matches(
        targetDevice: TargetDevice
    ): Boolean =
        when (targetDevice) {
            TargetDevice.MOBILE -> imageHeight > imageWidth
            TargetDevice.DESKTOP,
            TargetDevice.TABLET -> imageWidth >= imageHeight
        }

    private fun CategoryDto.toWallpaperCategory(): WallpaperCategory =
        WallpaperCategory(id = id, name = name, slug = slug)

    private fun TagDto.toWallpaperTag(): WallpaperTag =
        WallpaperTag(id = id, name = name, slug = slug)

    private fun TrendingCategoryDto.toTrendingCategory(): TrendingCategory? {
        if (label.isBlank() || slug.isBlank()) {
            return null
        }

        return TrendingCategory(
            label = label,
            queryValue = label,
            slug = slug,
            origin =
                if (origin.equals("system", ignoreCase = true)) {
                    TrendingCategoryOrigin.SYSTEM
                } else {
                    TrendingCategoryOrigin.USER_KEYWORD
                },
            searchCount = searchCount,
            score = score,
            topKeywords = topKeywords,
        )
    }

    private fun SearchRecommendationDto.toSearchRecommendation(): SearchRecommendation? {
        if (label.isBlank()) {
            return null
        }

        val recommendationType =
            when (type.lowercase()) {
                "tag" -> SearchRecommendationType.TAG
                "system_category" -> SearchRecommendationType.SYSTEM_CATEGORY
                "user_keyword_category" -> SearchRecommendationType.USER_KEYWORD_CATEGORY
                else -> return null
            }

        val effectiveValue = value.ifBlank { label }
        val queryValue =
            when (recommendationType) {
                SearchRecommendationType.TAG -> "#$effectiveValue"
                SearchRecommendationType.SYSTEM_CATEGORY,
                SearchRecommendationType.USER_KEYWORD_CATEGORY -> label
            }

        return SearchRecommendation(
            type = recommendationType,
            label = label,
            queryValue = queryValue,
            score = score,
            matchReason = matchReason,
        )
    }

    private data class ApiCredential(val value: String, val fingerprint: String)

    private companion object {
        const val MaxTitleLength = 72
    }
}

class WallpaperDownload(val bytes: ByteArray, val mimeType: String, val extension: String)

@Serializable
private data class SearchLogRequestDto(
    val keyword: String,
    @SerialName("source_slug") val sourceSlug: String,
    @SerialName("result_count") val resultCount: Int? = null,
)

private data class WallpaperSearchCacheKey(
    val source: WallpaperSource,
    val normalizedQuery: String,
    val page: Int,
    val targetDevice: TargetDevice,
    val normalizedCategorySlug: String,
    val credentialFingerprint: String,
)

private data class WallpaperSearchCacheEntry(
    val storedAt: TimeMark,
    val wallpapers: List<Wallpaper>,
)

private class WallpaperSearchMemoryCache(
    private val ttl: Duration = CacheTtl,
    private val maxEntries: Int = CacheMaxEntries,
) {
    private val entries = LinkedHashMap<WallpaperSearchCacheKey, WallpaperSearchCacheEntry>()

    fun get(key: WallpaperSearchCacheKey): List<Wallpaper>? {
        val entry = entries[key] ?: return null
        if (entry.storedAt.elapsedNow() > ttl) {
            entries.remove(key)
            return null
        }
        return entry.wallpapers
    }

    fun put(key: WallpaperSearchCacheKey, wallpapers: List<Wallpaper>) {
        pruneExpired()
        entries[key] =
            WallpaperSearchCacheEntry(
                storedAt = TimeSource.Monotonic.markNow(),
                wallpapers = wallpapers,
            )
        while (entries.size > maxEntries) {
            entries.remove(entries.keys.first())
        }
    }

    fun invalidateSource(source: WallpaperSource) {
        entries.keys.filter { it.source == source }.forEach(entries::remove)
    }

    private fun pruneExpired() {
        entries.keys
            .filter { key -> entries[key]?.storedAt?.elapsedNow()?.let { it > ttl } == true }
            .forEach(entries::remove)
    }
}
