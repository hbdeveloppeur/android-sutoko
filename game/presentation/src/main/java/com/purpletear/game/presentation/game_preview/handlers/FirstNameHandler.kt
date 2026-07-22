package com.purpletear.game.presentation.game_preview.handlers

import android.content.Context
import com.example.sharedelements.utils.UiText
import com.purpletear.game.presentation.R
import com.purpletear.sutoko.game.model.Chapter
import fr.purpletear.sutoko.popup.domain.EditTextPopUp
import fr.purpletear.sutoko.popup.domain.usecase.ShowPopUpUseCase
import purpletear.fr.purpleteartools.TableOfSymbols
import javax.inject.Inject

/**
 * Handles first name input logic for new games.
 * Shows a popup on chapter 1 if no first name is stored.
 */
class FirstNameHandler @Inject constructor(
    private val showPopUpUseCase: ShowPopUpUseCase,
    private val tableOfSymbols: TableOfSymbols,
) {

    /**
     * Result of first name handling.
     */
    sealed class Result {
        data object ShouldAsk : Result()
        data object AlreadySet : Result()
    }

    /**
     * Checks if we should ask for first name.
     * Only asks on chapter 1 when no name is stored.
     *
     * @param legacyId The legacy integer game ID used by [TableOfSymbols].
     * @param currentChapter The current chapter
     * @return Result indicating whether to ask or not
     */
    fun checkFirstNameNeeded(legacyId: Int, currentChapter: Chapter?): Result {
        if (currentChapter?.number != 1) return Result.AlreadySet

        val existing = try {
            tableOfSymbols.get(legacyId, "prenom")
        } catch (_: Exception) {
            null
        }

        return if (existing.isNullOrBlank()) Result.ShouldAsk else Result.AlreadySet
    }

    /**
     * Shows the first name input popup.
     * @return The popup tag for observing interactions
     */
    fun askFirstName(): String {
        val popUp = EditTextPopUp(
            title = UiText.StringResource(R.string.game_presentation_first_name_prompt_title),
            placeholder = UiText.StringResource(R.string.game_presentation_first_name_prompt_placeholder),
        )
        return showPopUpUseCase(popUp)
    }

    /**
     * Saves the first name for a game.
     *
     * @param legacyId The legacy integer game ID used by [TableOfSymbols].
     * @param name The first name to save
     * @param appContext Application context for saving
     * @return true if saved successfully
     */
    fun saveFirstName(legacyId: Int, name: String, appContext: Context): Boolean {
        val sanitized = sanitizeFirstName(name)
        if (sanitized.isEmpty()) return false

        return try {
            tableOfSymbols.addOrSet(legacyId, "prenom", sanitized)
            tableOfSymbols.save(appContext)
            true
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Removes the stored first name for a game.
     * Called when restarting a game.
     *
     * @param legacyId The legacy integer game ID used by [TableOfSymbols].
     * @param appContext Application context for saving
     */
    fun clearFirstName(legacyId: Int, appContext: Context) {
        try {
            tableOfSymbols.removeVar(legacyId, "prenom")
            tableOfSymbols.save(appContext)
        } catch (_: Exception) {
            // Silently fail
        }
    }

    private fun sanitizeFirstName(input: String): String {
        return input
            .trim()
            .replace(Regex("[^\\p{L}\\s'\\-]+"), "")
            .replace(Regex("\\s{2,}"), " ")
            .trim()
    }
}
