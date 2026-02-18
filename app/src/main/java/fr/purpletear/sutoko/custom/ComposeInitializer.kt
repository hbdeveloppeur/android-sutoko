package fr.purpletear.sutoko.custom

import android.content.Context
import androidx.annotation.Keep
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.ProcessLifecycleInitializer
import androidx.startup.Initializer

/**
 * Initializer for Compose UI components.
 * This class is used by the androidx.startup library to initialize Compose UI components.
 */
@Keep
class ComposeInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        ComposeView(context) // Initializes the component
        return Unit
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return listOf(ProcessLifecycleInitializer::class.java)
    }
}
