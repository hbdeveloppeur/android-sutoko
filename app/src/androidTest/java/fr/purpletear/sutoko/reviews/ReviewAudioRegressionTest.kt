package fr.purpletear.sutoko.reviews

import android.content.ContextWrapper
import android.media.MediaPlayer
import android.os.SystemClock
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import friendzone3.purpletear.fr.friendzon3.Data
import friendzone3.purpletear.fr.friendzon3.handlers.SoundHandler as Fz3SoundHandler
import fr.purpletear.friendzone4.purpleTearTools.SoundHandler as Fz4SoundHandler
import fr.purpletear.friendzone4.purpleTearTools.SinglePlayer
import fr.purpletear.friendzone4.game.activities.audiocinematic.AudioCinematic
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class ReviewAudioRegressionTest {
    @Test fun friendzoned3DownloadedAndBundledSoundsPrepareAndCompleteSafely() {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val empty=File(context.cacheDir,"review-audio-empty").apply {deleteRecursively();mkdirs()}
        val isolated=object:ContextWrapper(context){override fun getFilesDir()=empty}
        try {
            val assets=context.assets.list("friendzone3_assets/sound")!!.filter { it.endsWith(".mp3") }
            assertEquals(13,assets.size)
            for(file in assets){
                val player=MediaPlayer()
                try {Data.setSoundSource(isolated,player,file.removeSuffix(".mp3"));player.prepare();assertTrue(player.duration>0)}
                finally {player.release()}
            }
            ActivityScenario.launch(AudioCinematic::class.java).use { scenario ->
                var pool:Fz3SoundHandler?=null
                scenario.onActivity { activity ->
                    assertTrue(Data.selectSound(activity,"bg_cinematic").endsWith("fz3_bg_cinematic.mp3"))
                    pool=Fz3SoundHandler().generate("glitch_01",activity,false)
                    assertTrue(pool!!.exist("glitch_01"));pool!!.play("glitch_01",null)
                }
                SystemClock.sleep(1800)
                scenario.onActivity { activity ->
                    assertFalse(pool!!.exist("glitch_01"))
                    repeat(3){pool!!.generate("bg_cinematic",activity,true).play("bg_cinematic")}
                    pool!!.pause("bg_cinematic");pool!!.play("bg_cinematic")
                    pool!!.clear();pool!!.clear();assertFalse(pool!!.exist("bg_cinematic"))
                    println("FZ3_AUDIO_FIXED: 13 fallback assets prepared; downloaded effect completed; regeneration/clear safe")
                }
            }
        } finally {empty.deleteRecursively()}
    }

    @Test fun friendzoned4AllChapter6LanguagesAndEffectCompletionAreSafe() {
        ActivityScenario.launch(AudioCinematic::class.java).use { scenario ->
            var effects:Fz4SoundHandler?=null
            scenario.onActivity { activity ->
                for(language in listOf("fr-FR","en-GB","de-DE","es-ES","es-419")){
                    val player=SinglePlayer().generate("intro_6/intro_$language",activity,false)
                    try {
                        assertTrue("Missing audio for $language",player.duration>0)
                        println("FZ4_AUDIO_PREPARED: $language duration=${player.duration}")
                        if(language=="fr-FR"){
                            player.play(null);SystemClock.sleep(700);assertTrue(player.time>0)
                        }
                    } finally {player.release()}
                }
                effects=Fz4SoundHandler().generate("message",activity,false)
                assertTrue(effects!!.exist("message"));effects!!.play("message",null)
            }
            SystemClock.sleep(1200)
            scenario.onActivity {
                assertFalse(effects!!.exist("message"));assertEquals(0,effects!!.getCurrentPosition("message"))
                effects!!.clear();effects!!.clear()
            }
        }
    }
}
