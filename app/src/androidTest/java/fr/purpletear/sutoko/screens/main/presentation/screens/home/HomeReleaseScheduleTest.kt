package fr.purpletear.sutoko.screens.main.presentation.screens.home

import android.content.res.Configuration
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import fr.purpletear.sutoko.R
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeReleaseScheduleTest {
    @Test
    fun allSupportedLocalesAnnounceSundayAtSixPmOnly() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val announcements = mapOf(
            "en" to "A chapter every Sunday at 6:00 PM",
            "fr" to "Un chapitre chaque dimanche à 18h00",
            "de" to "Ein Kapitel jeden Sonntag um 18:00 Uhr",
            "es" to "Un capítulo cada domingo a las 18:00",
            "es-ES" to "Un capítulo cada domingo a las 18:00",
        )
        announcements.forEach { (language, expected) ->
            val configuration = Configuration(context.resources.configuration).apply {
                setLocale(Locale.forLanguageTag(language))
            }
            val localizedContext = context.createConfigurationContext(configuration)
            assertEquals(language, expected, localizedContext.getString(
                R.string.sutoko_main_section_title_release_schedule
            ))
        }
    }
}
