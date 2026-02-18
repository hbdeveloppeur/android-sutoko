package com.purpletear.smsgame.activities.smsgame.objects

interface ChoiceControllerInterface {
    fun onClickSecretChoice(phrase: Phrase, diamonds: Int, action: () -> Unit, onError: () -> Unit)
    fun onClickChoice(phrase: Phrase)
}