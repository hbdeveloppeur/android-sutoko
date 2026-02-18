package friendzone3.purpletear.fr.friendzon3.tables

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import friendzone3.purpletear.fr.friendzon3.Data
import friendzone3.purpletear.fr.friendzon3.config.Language
import java.lang.IllegalStateException
import java.util.ArrayList
import friendzone3.purpletear.fr.friendzon3.custom.Character

/**
 * A TableOfCharacters contains all the Story's Characters
 * @author Hocine Belbouab <hbdeveloppeur@gmail.com>
 * @see fr.purpletear.spellmysecrets.config.conversation.objects.Character
 */
class TableOfCharacters  {

    /**
     * Contains the list of characters of the story
     */
    var characters: List<Character> = ArrayList()
        private set


    /**
     * Reads the Story's characters table
     * @param context : Context
     * @param conversationCode : Int
     * @param langCode : String
     */
    fun read(context: Context, conversationCode : String) {
        val path = Data.getCharactersPath(conversationCode, Language.determineLangDirectory())

        val content = Data.getAssetContent(context.assets, path)
        val listType = object : TypeToken<List<Character>>() {}.type
        val newList = Gson().fromJson<List<Character>>(content, listType)

        characters = ArrayList(newList)
    }

    /**
     * Returns a Character given an Id
     * @param id : Int
     * @return Character
     */
    fun getCharacter(id: Int): Character {
        for (character in characters) {
            if (character.id == id) {
                return character
            }
        }
        if(id == 0) {
            return Character(0, "", "", true)
        }
        throw IllegalStateException("Couldn't find the character. {characterId:$id} in $characters")
    }

}
