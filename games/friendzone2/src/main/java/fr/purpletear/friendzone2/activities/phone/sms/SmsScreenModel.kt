package fr.purpletear.friendzone2.activities.phone.sms

import android.app.Activity
import android.content.Context
import com.bumptech.glide.RequestManager
import fr.purpletear.friendzone2.R
import fr.purpletear.friendzone2.activities.main.GameConversationAdapter
import fr.purpletear.friendzone2.configs.Phrase
import fr.purpletear.friendzone2.tables.Character
import fr.purpletear.friendzone2.tables.TableOfLinks
import fr.purpletear.friendzone2.tables.TableOfPhrases
import purpletear.fr.purpleteartools.GlobalData
import purpletear.fr.purpleteartools.TableOfSymbols

class SmsScreenModel(activity: Activity, requestManager: RequestManager, var signalActivated : Boolean) {
    var requestManager: RequestManager = requestManager
        private set
    private val substringNumber = 22
    val phrases = TableOfPhrases()
    val links = TableOfLinks()
    lateinit var adapter : GameConversationAdapter
    val symbols:TableOfSymbols = TableOfSymbols(GlobalData.Game.FRIENDZONE2.id)

    var currentPage : Page = Page.PREVIEWS

    enum class Contact {
        KLOMOBILE,
        CHRISTOPHE,
        ZOE,
        SYLVIE,
        LUCIE,
        BRYAN,
        OTHER_1,
        OTHER_2
    }


    enum class Page {
        PREVIEWS,
        CONVERSATION
    }

    /**
     * Determines if it is a first start
     * @return Boolean
     */
    private var isFirstStart = true

    init {
        symbols.read(activity)
    }

    fun isFirstStart(): Boolean {
        val value = isFirstStart
        isFirstStart = false
        return value
    }

    /**
     * Returns the conversation name given the contact
     * @param context : Context
     * @param contact : Contact
     * @return String
     */
    fun getConversationName(context: Context, contact: Contact): String {
        val name = when (contact) {
            Contact.KLOMOBILE -> context.getString(R.string.klo_mobile)
            Contact.CHRISTOPHE -> context.getString(R.string.alias_christophe)
            Contact.ZOE -> context.getString(R.string.alias_amourette)
            Contact.SYLVIE -> context.getString(R.string.alias_sylvie)
            Contact.LUCIE -> context.getString(R.string.alias_ma_petite_lucie)
            Contact.BRYAN -> context.getString(R.string.alias_bryan)
            Contact.OTHER_1 -> context.getString(R.string.number_format)
            Contact.OTHER_2 -> context.getString(R.string.number_format)
        }
        return Character.updateNames(context, name)
    }

    /**
     * Returns the conversation name given the contact
     * @param contact : Contact
     * @return String
     */
    fun getConversationPicture(contact: Contact): String {
        return when (contact) {
            Contact.KLOMOBILE -> "no_avatar_profil"
            Contact.CHRISTOPHE -> "parents_profil"
            Contact.ZOE -> "zoe"
            Contact.SYLVIE -> "parents_profil"
            Contact.LUCIE -> "lucie_profil"
            Contact.BRYAN -> "bryan_profil"
            Contact.OTHER_1 -> "no_avatar_profil"
            Contact.OTHER_2 -> "no_avatar_profil"
        }
    }

    /**
     * Returns the conversation's status from the files
     * @param context: Context
     * @param contact : Contact
     * @return String
     */
    private fun getConversationStatusFromFiles(context: Context, contact: Contact, symbols : TableOfSymbols): String {
        links.readEvaPhoneConversation(context, getChapterCodeFromContact(contact))
        phrases.readEvaPhoneConversation(context, getChapterCodeFromContact(contact))
        var currentItemId = 0
        while (true) {
            val p: Phrase = getNextPhrase(currentItemId, links, phrases) ?: break
            if(p.isSplit()) {
                if(!signalActivated) {
                    break
                }
            }
            currentItemId = p.id
        }

        val sentence = Character.updateNames(context, phrases.getPhrase(currentItemId).sentence)
        if (sentence.length > substringNumber) {
            return "${sentence.substring(0, substringNumber)}..."
        }

        return sentence
    }

    /**
     * Returns the conversation's status
     * @param context: Context
     * @param contact : Contact
     * @return String
     */
    fun getConversationStatus(context: Context, contact: Contact, symbols: TableOfSymbols): String {
        return when(contact) {
            Contact.OTHER_1 -> context.getString(R.string.message_preview_other_1)
            Contact.OTHER_2 -> context.getString(R.string.message_preview_other_2)
            else -> getConversationStatusFromFiles(context, contact, symbols)
        }
    }

    /**
     * Returns the next phrase or null if there is none
     * @param id : Int
     * @param links : TableOfLinks
     * @param phrases : TableOfPhrases
     * @return Phrase?
     */
    private fun getNextPhrase(id: Int, links: TableOfLinks, phrases: TableOfPhrases): Phrase? {
        val list = links.getDest(id)
        if (list.count() == 0) {
            return null
        }
        return phrases.getPhrase(list[0])
    }

    /**
     * Discuss
     */
    fun discuss(context : Context, contact : Contact, symbols: TableOfSymbols) {
        phrases.readEvaPhoneConversation(context, getChapterCodeFromContact(contact))
        links.readEvaPhoneConversation(context, getChapterCodeFromContact(contact))
        adapter.clear()
        discuss(context, phrases.getPhrase(links.getDest(0)[0]))
    }

    private fun discuss(context : Context, p : Phrase?) {
        if(p == null) {
            return
        }
        if(p.isSplit()) {
            if(signalActivated) {
                discuss(context, getNextPhrase(p.id, links, phrases))
            }
            return
        }
        val name = adapter.characters.getCharacter(p.id_author).name
        p.sentence = Character.updateNames(context, p.sentence)
        adapter.insert(p, if(name == "Eva Belle") Phrase.Type.me else p.getType())
        discuss(context, getNextPhrase(p.id, links, phrases))
    }

    companion object {

        /**
         * Returns the chapter's code from the Contact
         * @param contact : Contact
         * @return String
         */
        fun getChapterCodeFromContact(contact: Contact): String {
            return when (contact) {
                Contact.KLOMOBILE -> "11f"
                Contact.CHRISTOPHE -> "11a"
                Contact.ZOE -> "11b"
                Contact.SYLVIE -> "11c"
                Contact.LUCIE -> "11d"
                Contact.BRYAN -> "11e"
                else -> "11a"
            }
        }
    }
}
