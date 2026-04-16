package com.purpletear.game.debug

import com.purpletear.sutoko.game.model.character.Character as StoryCharacter
import com.purpletear.sutoko.game.model.character.CharacterColor as StoryCharacterColor

val PreviewCharacter: StoryCharacter = StoryCharacter(
    id = 0,
    name = "Eva Belle",
    avatar = null,
    isMainCharacter = false,
    color = StoryCharacterColor(startingColor = "#FFFFFF", endingColor = "#FFFFFF")
)
