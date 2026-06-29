package com.purpletear.sutoko.game.usecase.testing

import com.purpletear.sutoko.game.repository.testing.TestPackageRepository
import javax.inject.Inject

class DownloadTestPackageUseCase @Inject constructor(
    private val repository: TestPackageRepository,
) {
    suspend operator fun invoke(
        packageUrl: String,
        gameId: String,
        chapterId: String,
        seed: Int,
    ): Result<String> {
        return repository.downloadPackage(packageUrl, gameId, chapterId, seed)
    }
}
