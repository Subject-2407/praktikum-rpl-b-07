package com.scapes.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PexelsSearchResponseDto(
    val photos: List<PexelsPhotoDto> = emptyList(),
)

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
data class UnsplashSearchResponseDto(
    val results: List<UnsplashPhotoDto> = emptyList(),
)

@Serializable
data class UnsplashPhotoDto(
    val id: String,
    val width: Int,
    val height: Int,
    val description: String? = null,
    @SerialName("alt_description")
    val altDescription: String? = null,
    val user: UnsplashUserDto = UnsplashUserDto(),
    val urls: UnsplashPhotoUrlsDto = UnsplashPhotoUrlsDto(),
)

@Serializable
data class UnsplashUserDto(
    val name: String = "",
)

@Serializable
data class UnsplashPhotoUrlsDto(
    val raw: String = "",
    val full: String = "",
    val regular: String = "",
    val small: String = "",
    val thumb: String = "",
)

@Serializable
data class PixabaySearchResponseDto(
    val hits: List<PixabayPhotoDto> = emptyList(),
)

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
