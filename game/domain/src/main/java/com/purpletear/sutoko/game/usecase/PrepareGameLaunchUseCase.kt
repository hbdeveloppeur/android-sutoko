package com.purpletear.sutoko.game.usecase

import com.purpletear.sutoko.game.model.FriendzonedLegacyIds
import com.purpletear.sutoko.game.model.game.GameCatalog
import com.purpletear.sutoko.game.repository.FriendzonedProgressRepository
import com.purpletear.sutoko.game.repository.UserGameProgressRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class PrepareGameLaunchUseCase @Inject constructor(
    private val userGameProgressRepository: UserGameProgressRepository,
    private val friendzonedProgressRepository: FriendzonedProgressRepository,
) {
    /** Returns true when a required nickname must be collected before any chapter can open. */
    suspend operator fun invoke(
        catalog: GameCatalog,
        requestNickName: Boolean = true,
    ): Result<Boolean> = try {
        val legacyId = catalog.legacyId
        val isFriendzoned = FriendzonedLegacyIds.isFriendzoned(legacyId)
        val shouldCheckName = catalog.userNickNameRequired && requestNickName
        val savedName = if (shouldCheckName || isFriendzoned) {
            userGameProgressRepository.get(catalog.id).heroName
        } else {
            ""
        }
        val needsName = shouldCheckName && savedName.isBlank()
        if (!needsName && isFriendzoned && savedName.isNotBlank()) {
            friendzonedProgressRepository.setFirstName(checkNotNull(legacyId), savedName)
        }
        Result.success(needsName)
    } catch (error: CancellationException) {
        throw error
    } catch (error: Exception) {
        Result.failure(error)
    }
}
