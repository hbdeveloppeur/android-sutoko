package com.purpletear.sutoko.game.model.game

import androidx.annotation.Keep
import com.purpletear.sutoko.game.model.Asset
import com.purpletear.sutoko.game.model.Author

@Keep
data class GameCatalog(
    val id: String,
    val version: Int = 0,
    val isCertified: Boolean = false,
    val price: Int = 0,
    val skus: List<String> = emptyList(),
    val videoUrl: String? = null,
    val menuBackground: Asset? = null,
    val chaptersCount: Int = 0,
    val banner: Asset? = null,
    val logo: Asset? = null,
    val title: Asset? = null,
    val metadata: GameMetadata,
    val author: Author? = null,
    val legacyId: Int? = null,
    val isOfficial: Boolean = false,
    val userNickNameRequired: Boolean = false,
    val minAppBuild: Int,
    val narrativeThemes: List<NarrativeTheme> = emptyList(),
)

fun GameCatalog.isPremium(): Boolean = price > 0