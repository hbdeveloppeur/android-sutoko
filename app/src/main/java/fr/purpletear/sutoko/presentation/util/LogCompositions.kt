package fr.purpletear.sutoko.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.remember
import fr.purpletear.sutoko.BuildConfig
import fr.purpletear.sutoko.tools.Std

class Ref(var value: Int)

// Note the inline function below which ensures that this function is essentially
// copied at the call site to ensure that its logging only recompositions from the
// original call site.
@Composable
fun LogCompositions(name: String, level: Int = 1) {
    if (BuildConfig.DEBUG) {
        val ref = remember { Ref(1) }
        SideEffect { ref.value++ }
        // str builder to add as much - as needed
        val str = StringBuilder()
        str.append("JetPack Drawer |")
        for (i in 1..level) {
            str.append("-")
        }
        Std.debug("$str> $name (${ref.value} times)")
    }
}