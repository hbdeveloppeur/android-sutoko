package com.purpletear.sutoko.game.usecase

import com.purpletear.sutoko.game.repository.UserGameProgressRepository
import javax.inject.Inject

/**
 * Sanitizes a user-provided nickname.
 *
 * Rules:
 * - trimmed
 * - no double spaces
 * - no emojis, underscores, digits or special characters
 * - allows letters, spaces, hyphens and apostrophes
 * - length between 3 and 15 characters
 *
 * Returns the sanitized name, or null if the input is invalid.
 */
object UserNickNameSanitizer {
    const val MIN_LENGTH = 3
    const val MAX_LENGTH = 15

    fun sanitize(input: String): String? {
        val sanitized = input
            .trim()
            .replace(Regex("\\s{2,}"), " ")
            .replace(Regex("[^\\p{L}\\s'\\-]+"), "")
            .trim()

        return sanitized.takeIf { it.length in MIN_LENGTH..MAX_LENGTH }
    }
}

/**
 * Saves the user's nickname for a game.
 *
 * The sanitized name is written to [UserGameProgressRepository] as the hero name.
 * A null [nickName] falls back to the default hero name.
 */
class SaveUserNickNameUseCase @Inject constructor(
    private val userGameProgressRepository: UserGameProgressRepository,
) {
    /**
     * Saves [nickName] for [gameId] after sanitization.
     *
     * If [nickName] is null, the default hero name is saved.
     *
     * @return [Result.success] if the name was saved, [Result.failure] if the name
     * is invalid or the repository failed.
     */
    suspend operator fun invoke(gameId: String, nickName: String?): Result<Unit> = runCatching {
        val sanitized = nickName?.let { UserNickNameSanitizer.sanitize(it) }

        if (nickName != null && sanitized == null) {
            throw IllegalArgumentException("Invalid nickname: $nickName")
        }

        val current = userGameProgressRepository.get(gameId)
        userGameProgressRepository.save(
            current.copy(
                gameId = gameId,
                heroName = sanitized ?: DEFAULT_HERO_NAME,
            )
        )
    }

    companion object {
        const val DEFAULT_HERO_NAME = "Nick"
    }
}
