package com.scapes.domain.usecase

import com.scapes.domain.model.ApplyTarget
import com.scapes.domain.model.DownloadSettings
import com.scapes.domain.model.ErrorCode
import com.scapes.domain.model.ScapesResult
import com.scapes.domain.model.TargetDevice
import com.scapes.domain.model.Wallpaper
import com.scapes.domain.model.WallpaperSource
import com.scapes.domain.model.WallpaperSourceInfo
import com.scapes.domain.repository.SettingsRepository
import com.scapes.domain.repository.WallpaperRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest

class SearchWallpapersUseCaseTest {
    @Test
    fun searchUsesSelectedSourceAndNormalizesQuery() = runTest {
        val wallpaperRepository = FakeWallpaperRepository()
        val settingsRepository = FakeSettingsRepository(selectedSource = WallpaperSource.PIXABAY)
        val useCase = SearchWallpapersUseCase(wallpaperRepository, settingsRepository)

        val result = useCase("  forest  ")

        assertIs<ScapesResult.Success<List<Wallpaper>>>(result)
        assertEquals("forest", wallpaperRepository.lastQuery)
        assertEquals(WallpaperSource.PIXABAY, wallpaperRepository.lastSource)
        assertEquals(1, result.data.size)
        assertEquals(TargetDevice.DESKTOP, result.data.first().targetDevice)
    }

    @Test
    fun searchCanOverrideSelectedSource() = runTest {
        val wallpaperRepository = FakeWallpaperRepository()
        val settingsRepository = FakeSettingsRepository(selectedSource = WallpaperSource.PIXABAY)
        val useCase = SearchWallpapersUseCase(wallpaperRepository, settingsRepository)

        useCase(query = "city", source = WallpaperSource.SCAPES_API)

        assertEquals(WallpaperSource.SCAPES_API, wallpaperRepository.lastSource)
    }

    @Test
    fun searchRejectsBlankQuery() = runTest {
        val wallpaperRepository = FakeWallpaperRepository()
        val settingsRepository = FakeSettingsRepository()
        val useCase = SearchWallpapersUseCase(wallpaperRepository, settingsRepository)

        val result = useCase("   ")

        assertIs<ScapesResult.Error>(result)
        assertEquals(ErrorCode.VALIDATION, result.code)
        assertEquals(0, wallpaperRepository.searchCount)
    }
}

class UpdateDownloadSettingsUseCaseTest {
    @Test
    fun updateRejectsBlankFolder() = runTest {
        val settingsRepository = FakeSettingsRepository()
        val useCase = UpdateDownloadSettingsUseCase(settingsRepository)

        val result = useCase(DownloadSettings(folderPath = "   "))

        assertIs<ScapesResult.Error>(result)
        assertEquals(ErrorCode.VALIDATION, result.code)
    }

    @Test
    fun updateTrimsFolderBeforeSaving() = runTest {
        val settingsRepository = FakeSettingsRepository()
        val useCase = UpdateDownloadSettingsUseCase(settingsRepository)

        val result = useCase(DownloadSettings(folderPath = "  C:/Scapes  "))

        assertIs<ScapesResult.Success<Unit>>(result)
        assertEquals("C:/Scapes", settingsRepository.downloadSettings.folderPath)
    }
}

private class FakeWallpaperRepository : WallpaperRepository {
    var lastQuery: String? = null
    var lastSource: WallpaperSource? = null
    var searchCount: Int = 0

    override suspend fun getWallpaperSources(): ScapesResult<List<WallpaperSourceInfo>> =
        ScapesResult.Success(emptyList())

    override suspend fun searchWallpapers(
        query: String,
        page: Int,
        source: WallpaperSource,
        targetDevice: TargetDevice,
    ): ScapesResult<List<Wallpaper>> {
        searchCount += 1
        lastQuery = query
        lastSource = source
        return ScapesResult.Success(
            listOf(
                fakeWallpaper("desktop", TargetDevice.DESKTOP),
                fakeWallpaper("mobile", TargetDevice.MOBILE),
            )
        )
    }

    override suspend fun saveWallpaper(wallpaper: Wallpaper): ScapesResult<Wallpaper> =
        ScapesResult.Success(wallpaper)

    override suspend fun applyWallpaper(
        wallpaper: Wallpaper,
        target: ApplyTarget,
    ): ScapesResult<Unit> = ScapesResult.Success(Unit)

    override suspend fun validateApiKey(
        source: WallpaperSource,
        apiKey: String,
    ): ScapesResult<Unit> = ScapesResult.Success(Unit)

    override fun invalidateSource(source: WallpaperSource) = Unit

    private fun fakeWallpaper(id: String, targetDevice: TargetDevice): Wallpaper =
        Wallpaper(
            id = id,
            title = id,
            source = WallpaperSource.SCAPES_API,
            previewUrl = "https://scapes.my.id/$id.webp",
            fullImageUrl = "https://scapes.my.id/$id.jpg",
            targetDevice = targetDevice,
        )
}

private class FakeSettingsRepository(
    private var selectedSource: WallpaperSource = WallpaperSource.SCAPES_API,
    var downloadSettings: DownloadSettings = DownloadSettings(folderPath = "Scapes"),
) : SettingsRepository {
    override suspend fun getSelectedSource(): ScapesResult<WallpaperSource> =
        ScapesResult.Success(selectedSource)

    override suspend fun setSelectedSource(source: WallpaperSource): ScapesResult<Unit> {
        selectedSource = source
        return ScapesResult.Success(Unit)
    }

    override suspend fun getDownloadSettings(): ScapesResult<DownloadSettings> =
        ScapesResult.Success(downloadSettings)

    override suspend fun setDownloadSettings(settings: DownloadSettings): ScapesResult<Unit> {
        downloadSettings = settings
        return ScapesResult.Success(Unit)
    }
}
