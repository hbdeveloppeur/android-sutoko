package fr.purpletear.sutoko.presentation.util

import android.content.Context
import coil.imageLoader

object DeleteCoilCache {
    fun clearCache(context: Context) {

        val imageLoader = context.imageLoader
        imageLoader.memoryCache?.clear()
    }
}