package com.purpletear.sutoko.version.usecase

import com.purpletear.sutoko.version.model.Version
import com.purpletear.sutoko.version.repository.VersionRepository
import javax.inject.Inject

/**
 * Use case to fetch a Version by its name and language code.
 * Returns a Result<Version> to propagate errors to the caller.
 */
class GetVersionByNameUseCase @Inject constructor(
    private val repository: VersionRepository,
) {
    suspend operator fun invoke(name: String, languageCode: String): Result<Version> {
        return repository.getVersionByName(name, languageCode)
    }
}
