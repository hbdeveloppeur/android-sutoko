package com.purpletear.game.data.database

import com.purpletear.sutoko.game.engine.*
import com.purpletear.sutoko.game.engine.message.*
import com.purpletear.sutoko.game.model.chapter.MemoryEntry
import org.junit.Assert.*
import org.junit.Test

class GameCheckpointConvertersTest {
    private val converter = GameCheckpointConverters()

    @Test fun everyStableMessageTypeAndDecisionRoundTripsWithoutLosingItsSubtype() {
        val messages=listOf(
            GameMessageText("text","Stay with Yumi",89,"#001122","#FFFFFF"),
            GameMessageInfo("info","Later"),GameMessageImage("image","/image.webp",94),
            GameMessageVocal("vocal","/voice.mp3",94),GameMessageAudioDialogue("audio","/speech.mp3",94,"Hello"),
            GameMessageMangaPage("manga","/win.webp",listOf(GameMessageMangaPage.TextOverlay("We stayed",26f,10f,20f,30f))),
            GameMessageNextChapter(),
        )
        val saved=GameCheckpoint("8A","fingerprint","node",GameCheckpoint.Gate.TAP,
            mapOf("IStayed" to MemoryEntry("true",8)),messages,sceneId=12,
            loopingSounds=listOf(HandlerEffect.PlaySound("music","/music.mp3",loop=true)))
        val restored=requireNotNull(converter.decode(converter.encode(saved)))
        assertEquals(saved.memories,restored.memories)
        assertEquals(messages.map { it.javaClass },restored.messages.map { it.javaClass })
        val text=restored.messages[0] as GameMessageText
        assertEquals("Stay with Yumi",text.text);assertEquals("#001122",text.backgroundColor)
        assertEquals(89,text.characterId)
        assertEquals("Hello",(restored.messages[4] as GameMessageAudioDialogue).text)
        assertEquals("We stayed",(restored.messages[5] as GameMessageMangaPage).overlays.single().text)
        assertEquals(saved.loopingSounds,restored.loopingSounds)
    }

    @Test fun malformedOrUnknownMessagesDoNotBreakDatabaseReads() {
        val cp=GameCheckpoint("8A","fp","node",GameCheckpoint.Gate.TAP,emptyMap(),listOf(GameMessageText("id","Text",1)))
        val json=requireNotNull(converter.encode(cp))
        assertNull(converter.decode("not json"))
        assertNull(converter.decode(json.replace("\"Text\"","\"UnknownType\"")))
        assertNull(converter.decode(json.replace("\"text\":\"Text\",","")))
        assertNull(converter.decode(null))
    }
}
