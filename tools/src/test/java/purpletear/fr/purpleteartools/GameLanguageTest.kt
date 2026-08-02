package purpletear.fr.purpleteartools

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Locale

class GameLanguageTest {

    private lateinit var original: Locale

    @Before
    fun setUp() {
        original = Locale.getDefault()
    }

    @After
    fun tearDown() {
        Locale.setDefault(original)
    }

    private fun directoryFor(language: String, country: String): String {
        Locale.setDefault(Locale(language, country))
        return GameLanguage.determineLangDirectory()
    }

    @Test
    fun `spain spanish resolves to es-ES`() {
        assertEquals("es-ES", directoryFor("es", "ES"))
    }

    @Test
    fun `mexican spanish resolves to es-419`() {
        assertEquals("es-419", directoryFor("es", "MX"))
    }

    @Test
    fun `us spanish resolves to es-419`() {
        assertEquals("es-419", directoryFor("es", "US"))
    }

    @Test
    fun `argentine spanish resolves to es-419`() {
        assertEquals("es-419", directoryFor("es", "AR"))
    }

    @Test
    fun `french resolves to fr-FR`() {
        assertEquals("fr-FR", directoryFor("fr", "FR"))
    }

    @Test
    fun `german resolves to de-DE`() {
        assertEquals("de-DE", directoryFor("de", "DE"))
    }

    @Test
    fun `english falls back to en-GB`() {
        assertEquals("en-GB", directoryFor("en", "US"))
    }

    @Test
    fun `determinCode matches determined directory`() {
        Locale.setDefault(Locale("es", "MX"))
        assertEquals(GameLanguage.Companion.Code.ES_419, GameLanguage.determinCode())

        Locale.setDefault(Locale("es", "ES"))
        assertEquals(GameLanguage.Companion.Code.ES_ES, GameLanguage.determinCode())

        Locale.setDefault(Locale("fr", "FR"))
        assertEquals(GameLanguage.Companion.Code.FR, GameLanguage.determinCode())

        Locale.setDefault(Locale("de", "DE"))
        assertEquals(GameLanguage.Companion.Code.DE, GameLanguage.determinCode())

        Locale.setDefault(Locale("en", "GB"))
        assertEquals(GameLanguage.Companion.Code.EN, GameLanguage.determinCode())
    }
}
