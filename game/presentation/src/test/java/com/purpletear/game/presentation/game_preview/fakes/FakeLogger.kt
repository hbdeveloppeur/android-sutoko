package com.purpletear.game.presentation.game_preview.fakes

import com.purpletear.sutoko.core.domain.logger.Logger

class FakeLogger : Logger {
    data class ExceptionEntry(val throwable: Throwable, val message: String?, val data: Map<String, String>)
    data class WarningEntry(val message: String, val data: Map<String, String>)

    val exceptions = mutableListOf<ExceptionEntry>()
    val warnings = mutableListOf<WarningEntry>()

    override fun warning(message: String, data: Map<String, String>) {
        warnings.add(WarningEntry(message, data))
    }

    override fun exception(throwable: Throwable, message: String?, data: Map<String, String>) {
        exceptions.add(ExceptionEntry(throwable, message, data))
    }
}
