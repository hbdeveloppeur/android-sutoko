package com.purpletear.sutoko.game.repository.testing

import com.purpletear.sutoko.game.model.testing.TestPackageManifest

/**
 * Repository for downloading and applying test packages.
 */
interface TestPackageRepository {

    /**
     * Downloads a test package ZIP to a local temporary directory and extracts it.
     *
     * @param packageUrl Full URL returned by the server (may already contain the inventory token).
     * @param gameId Game identifier, used for local directory layout.
     * @param chapterId Backend chapter UUID, used for local directory naming.
     * @param seed Package seed, used for local directory naming.
     * @return Result containing the path to the extracted directory, or a failure.
     */
    suspend fun downloadPackage(
        packageUrl: String,
        gameId: String,
        chapterId: String,
        seed: Int,
    ): Result<String>

    /**
     * Reads the extracted manifest, copies assets into the local test asset cache,
     * and returns a domain [TestPackageManifest].
     *
     * @param extractedDirectory Path returned by [downloadPackage].
     * @param gameId Game identifier used for cache layout.
     * @return Result containing the applied manifest, or a failure.
     */
    suspend fun applyPackage(
        extractedDirectory: String,
        gameId: String,
    ): Result<TestPackageManifest>
}
