package com.purpletear.sutoko.version.model

import androidx.annotation.Keep

/**
 * Metadata for a Version.
 *
 * Notes:
 * - DTO field for languageCode is named `language`.
 */
@Keep
data class VersionMetadata(
    val id: Int,
    val title: String,
    val subtitle: String,
    val languageCode: String,
)
