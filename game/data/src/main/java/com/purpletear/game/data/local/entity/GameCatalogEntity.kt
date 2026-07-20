package com.purpletear.game.data.local.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.purpletear.sutoko.game.model.Asset
import com.purpletear.sutoko.game.model.Author
import com.purpletear.sutoko.game.model.game.GameCatalog
import com.purpletear.sutoko.game.model.game.GameMetadata
import com.purpletear.sutoko.game.model.game.NarrativeTheme

@Keep
@Entity(tableName = "games")
data class GameCatalogEntity(
    @PrimaryKey
    val id: String,
    val version: Int = 0,
    val isCertified: Boolean = false,
    val price: Int = 0,
    val skuIdentifiers: List<String> = emptyList(),
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
    val minAppBuild: Int = 1,
    val narrativeThemes: List<NarrativeTheme> = emptyList(),
)


fun GameCatalogEntity.toDomain(): GameCatalog = GameCatalog(
    id = id,
    version = version,
    isCertified = isCertified,
    price = price,
    skus = skuIdentifiers,
    videoUrl = videoUrl,
    chaptersCount = chaptersCount,
    menuBackground = menuBackground,
    banner = banner,
    logo = logo,
    title = title,
    metadata = metadata,
    author = author,
    legacyId = legacyId,
    isOfficial = isOfficial,
    userNickNameRequired = userNickNameRequired,
    minAppBuild = minAppBuild,
    narrativeThemes = narrativeThemes,
)