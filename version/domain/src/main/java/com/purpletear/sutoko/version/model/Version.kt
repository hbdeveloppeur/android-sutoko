package com.purpletear.sutoko.version.model

import androidx.annotation.Keep
import com.purpletear.sutoko.core.domain.model.MediaImage

/**
 * Domain model for app Version.
 *
 * Notes:
 * - DTO field for publishDate is named `publishDateAndroid`.
 */
@Keep
data class Version(
    val id: Int,
    val isOnline: Boolean,
    val name: String,
    val versionNumber: Int,
    val versionCode: String,
    val publishDate: Long,
    val backgroundImage: MediaImage,
    val metadata: VersionMetadata,
)
