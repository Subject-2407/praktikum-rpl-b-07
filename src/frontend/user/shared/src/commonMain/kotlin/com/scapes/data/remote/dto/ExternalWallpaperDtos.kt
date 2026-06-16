package com.scapes.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiEnvelopeDto<T>(
    val success: Boolean = false,
    val message: String = "",
    val data: T,
    val meta: PaginationMetaDto? = null,
)

@Serializable
data class PaginationMetaDto(
    @SerialName("current_page") val currentPage: Int = 1,
    @SerialName("per_page") val perPage: Int = 20,
    val total: Int = 0,
    @SerialName("last_page") val lastPage: Int = 1,
)

@Serializable
data class ScapesWallpaperDto(
    val id: String,
    val title: String,
    val description: String? = null,
    @SerialName("file_path") val filePath: String = "",
    @SerialName("thumbnail_path") val thumbnailPath: String = "",
    val width: Int = 0,
    val height: Int = 0,
    @SerialName("target_device") val targetDevice: String = "desktop",
    val category: CategoryDto? = null,
    val tags: List<TagDto> = emptyList(),
    val contributor: ContributorDto? = null,
    @SerialName("published_at") val publishedAt: String? = null,
)

@Serializable data class CategoryDto(val id: Int = 0, val name: String = "", val slug: String = "")

@Serializable data class TagDto(val id: Int = 0, val name: String = "", val slug: String = "")

@Serializable
data class ContributorDto(
    val id: Int = 0,
    @SerialName("display_name") val displayName: String = "",
    val email: String = "",
)

@Serializable
data class ApiSourceDto(
    val id: Int = 0,
    val name: String = "",
    val slug: String = "",
    @SerialName("base_url") val baseUrl: String = "",
    @SerialName("is_default") val isDefault: Boolean = false,
)

@Serializable data class PexelsSearchResponseDto(val photos: List<PexelsPhotoDto> = emptyList())

@Serializable
data class PexelsPhotoDto(
    val id: Long,
    val width: Int,
    val height: Int,
    val url: String = "",
    val photographer: String = "",
    val alt: String = "",
    val src: PexelsPhotoSourcesDto = PexelsPhotoSourcesDto(),
)

@Serializable
data class PexelsPhotoSourcesDto(
    val original: String = "",
    val large2x: String = "",
    val large: String = "",
    val portrait: String = "",
    val medium: String = "",
    val small: String = "",
    val tiny: String = "",
)

@Serializable
data class UnsplashSearchResponseDto(val results: List<UnsplashPhotoDto> = emptyList())

@Serializable
data class UnsplashPhotoDto(
    val id: String,
    val width: Int,
    val height: Int,
    val description: String? = null,
    @SerialName("alt_description") val altDescription: String? = null,
    val user: UnsplashUserDto = UnsplashUserDto(),
    val urls: UnsplashPhotoUrlsDto = UnsplashPhotoUrlsDto(),
)

@Serializable data class UnsplashUserDto(val name: String = "")

@Serializable
data class UnsplashPhotoUrlsDto(
    val raw: String = "",
    val full: String = "",
    val regular: String = "",
    val small: String = "",
    val thumb: String = "",
)

@Serializable data class PixabaySearchResponseDto(val hits: List<PixabayPhotoDto> = emptyList())

@Serializable
data class PixabayPhotoDto(
    val id: Long,
    val tags: String = "",
    val user: String = "",
    val pageURL: String = "",
    val previewURL: String = "",
    val webformatURL: String = "",
    val largeImageURL: String = "",
    val fullHDURL: String = "",
    val imageWidth: Int = 0,
    val imageHeight: Int = 0,
)
