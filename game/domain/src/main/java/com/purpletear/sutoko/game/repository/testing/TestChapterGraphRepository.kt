package com.purpletear.sutoko.game.repository.testing

import com.purpletear.sutoko.game.model.chapter.ChapterGraph

/**
 * Repository for loading a chapter graph from an extracted test package.
 */
interface TestChapterGraphRepository {

    /**
     * Loads the chapter graph described by the test package manifest.
     *
     * @param extractedDirectory Path returned by [TestPackageRepository.downloadPackage].
     * @param gameId Game identifier used to resolve cached test assets.
     * @return Result containing the [ChapterGraph] or a failure.
     */
    fun load(extractedDirectory: String, gameId: String): Result<ChapterGraph>
}
