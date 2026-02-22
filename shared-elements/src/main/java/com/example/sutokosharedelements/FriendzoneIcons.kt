package com.example.sutokosharedelements

object FriendzonedIcons {

    fun get(gameId: Int): Int? {
        return when (gameId) {
            159 -> R.drawable.logo_card_159
            161 -> R.drawable.logo_card_161
            162 -> R.drawable.logo_card_162
            163 -> R.drawable.logo_card_163
            else -> null
        }
    }
}