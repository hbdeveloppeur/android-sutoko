package com.purpletear.smsgame.activities.smsgame.adapter

import com.purpletear.smsgame.activities.smsgame.objects.Phrase


interface SmsGamePhraseVocalListener {
    fun onPressed(phrase: Phrase, position: Int)
}