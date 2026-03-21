package com.purpletear.game.presentation.smsgame.components.dev

sealed class SmsGameDevAction {
    data object Back : SmsGameDevAction()
    data object OpenDebugView : SmsGameDevAction()
    data object Restart : SmsGameDevAction()
    data object Update : SmsGameDevAction()
}