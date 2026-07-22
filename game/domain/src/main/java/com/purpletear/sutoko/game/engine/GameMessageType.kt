package com.purpletear.sutoko.game.engine

import androidx.annotation.Keep

@Keep
enum class GameMessageType {
    Text,
    Info,
    Typing,
    ChapterEnd,
    Image,
    Vocal,
    MangaPage,
}