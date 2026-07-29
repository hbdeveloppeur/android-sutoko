package com.purpletear.sutoko.shop.presentation.components

import android.content.Context
import android.media.MediaPlayer
import com.purpletear.sutoko.shop.R

/**
 * Plays the coin reward sound once. The player releases itself on completion.
 * No-op if the sound resource cannot be decoded.
 */
fun playCoinSound(context: Context) {
    val player = MediaPlayer.create(context, R.raw.shop_sutoko_coins_buy) ?: return
    player.setOnCompletionListener { it.release() }
    player.start()
}
