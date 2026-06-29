package com.purpletear.sutoko.game.usecase.testing

import com.purpletear.sutoko.game.model.testing.TestPackageManifest
import com.purpletear.sutoko.game.repository.testing.TestPackageRepository
import javax.inject.Inject

class ApplyTestPackageUseCase @Inject constructor(
    private val repository: TestPackageRepository,
) {
    suspend operator fun invoke(extractedDirectory: String, gameId: String): Result<TestPackageManifest> {
        return repository.applyPackage(extractedDirectory, gameId)
    }
}
