package fr.purpletear.sutoko.screens.main.presentation.screens.home

import com.purpletear.sutoko.game.model.Asset
import com.purpletear.sutoko.game.model.game.CardLayout
import com.purpletear.sutoko.game.model.game.GameCatalog
import com.purpletear.sutoko.game.model.game.GameMetadata
import fr.purpletear.sutoko.BuildConfig

/**
 * Debug-only showcase stories exercising the VERTICAL card layout (Netflix-style
 * poster row). Posters are bundled in the debug source set under assets/posters.
 * Empty in release builds: the row is driven by server data only.
 */
fun fakeVerticalStories(): List<GameCatalog> {
    if (!BuildConfig.DEBUG) return emptyList()
    return listOf(
        fakeVerticalStory(id = "fake_vertical_gang", title = "Gang", poster = "gang.jpg"),
        fakeVerticalStory(id = "fake_vertical_dalsoon", title = "Dalsoon", poster = "dalsoon.jpg"),
        fakeVerticalStory(
            id = "fake_vertical_close_the_door",
            title = "Close the Door",
            poster = "close_the_door.jpg",
        ),
    )
}

private fun fakeVerticalStory(id: String, title: String, poster: String): GameCatalog {
    return GameCatalog(
        id = id,
        isOfficial = true,
        cardLayout = CardLayout.VERTICAL,
        verticalBanner = bundledPosterAsset(poster),
        metadata = GameMetadata(title = title),
        canvasTechnologyRequiredVersion = 1,
    )
}

private fun bundledPosterAsset(fileName: String): Asset {
    return Asset(
        id = 0,
        originalFilename = fileName,
        width = 0,
        height = 0,
        createdAt = 0,
        fileSizeBytes = 0,
        mimeType = "image/jpeg",
        storagePath = "file:///android_asset/posters/$fileName",
        thumbnailStoragePath = "",
    )
}
