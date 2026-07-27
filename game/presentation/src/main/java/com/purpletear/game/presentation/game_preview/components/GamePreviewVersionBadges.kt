package com.purpletear.game.presentation.game_preview.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.purpletear.game.presentation.R

/**
 * Displays the installed and available version badges in the top-left corner
 * of the GamePreview screen, for administrators only. The available-version badge is hidden when it
 * matches the installed version, and the current-version badge shows an
 * "up to date" suffix in that case.
 */
@Composable
internal fun GamePreviewVersionBadges(
    currentVersion: Int?,
    availableVersion: Int,
    modifier: Modifier = Modifier,
) {
    if (currentVersion == null) return

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        val isUpToDate = currentVersion == availableVersion
        GamePreviewLabel(
            text = stringResource(
                if (isUpToDate) {
                    R.string.game_presentation_game_preview_current_version_up_to_date
                } else {
                    R.string.game_presentation_game_preview_current_version
                },
                currentVersion,
            ),
        )

        if (!isUpToDate) {
            GamePreviewLabel(
                text = stringResource(
                    R.string.game_presentation_game_preview_available_version,
                    availableVersion,
                ),
            )
        }
    }
}
