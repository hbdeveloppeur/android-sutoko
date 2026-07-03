package com.purpletear.sutoko.game.repository.testing

/**
 * Provides a stable identifier for the current app install.
 *
 * The returned id must be non-empty and must not change for the lifetime of the install
 * under normal circumstances.
 */
interface DeviceIdProvider {
    suspend fun get(): String
}
