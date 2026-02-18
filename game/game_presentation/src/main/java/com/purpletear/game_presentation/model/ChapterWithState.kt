package com.purpletear.game_presentation.model

import androidx.annotation.Keep
import com.purpletear.sutoko.game.model.Chapter

/**
 * Data class that combines a Chapter with its corresponding ChapterState.
 *
 * @property chapter The Chapter object.
 * @property state The state of the chapter (Played, Current, or Locked).
 */
@Keep
data class ChapterWithState(
    val chapter: Chapter,
    val state: ChapterState
)