/*
 * Copyright (C) PurpleTear, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Hocine Belbouab <hbdeveloppeur@gmail.com>, 1/10/19 11:38 AM
 *
 */

package fr.purpletear.friendzone2.tables

import android.content.Context
import android.content.res.Resources
import com.example.sharedelements.OnlineAssetsManager
import fr.purpletear.friendzone2.BuildConfig
import fr.purpletear.friendzone2.R
import purpletear.fr.purpleteartools.GlobalData
import purpletear.fr.purpleteartools.TableOfSymbols
import java.util.ArrayList

class TableOfCharacters(context: Context, private var code: String, symbols: TableOfSymbols = TableOfSymbols(GlobalData.Game.FRIENDZONE2.id)) {
    /**
     * Contains the list of characters of the story
     */
    private var characters: List<Character> = ArrayList()

    init {
        characters = getCharacters(context, code, symbols)
    }

    /**
     * Returns a list of Characters for the given chapterCode
     *
     * @param chapterCode String
     * @return List Characters
     */
    @Throws
    private fun getCharacters(context: Context, chapterCode: String, symbols: TableOfSymbols): List<Character> {
        val characters = ArrayList<Character>()
        characters.add(getCharacterByName(context,0, "Me", R.color.meBackground))
        when (chapterCode) {
            "1a" -> {
                characters.add(getCharacterByName(context,47, "Eva Belle", R.color.meBackground))
                characters.add(getCharacterByName(context,48, "Chloé Winsplit", R.color.mainBackground))
            }
            "2a" -> {
                characters.add(getCharacterByName(context,26, "Sylvie Belle", R.color.mainBackground))
            }
            "3a" -> {
                characters.add(getCharacterByName(context,48, "Chloé Winsplit", R.color.mainBackground))
            }
            "4a" -> {
                characters.add(getCharacterByName(context,49, "Etranger", R.color.mainBackground))
            }
            "5a" -> {
                characters.add(getCharacterByName(context,50, "Zoé Topaze", R.color.mainBackground))
            }
            "5b" -> {
                characters.add(getCharacterByName(context,50, "Zoé Topaze", R.color.mainBackground))
            }
            "6a" -> {
                characters.add(getCharacterByName(context,50, "Zoé Topaze", R.color.mainBackground))
            }
            "6b" -> {
                characters.add(getCharacterByName(context,50, "Zoé Topaze", R.color.mainBackground))
                characters.add(getCharacterByName(context,51, "Lucie Belle", R.color.thirdBackground))
            }
            "7a" -> {
                characters.add(getCharacterByName(context,50, "Zoé Topaze", R.color.mainBackground))
                characters.add(getCharacterByName(context,48, "Chloé Winsplit", R.color.thirdBackground))
            }
            "7b" -> {
                characters.add(getCharacterByName(context,48, "Chloé Winsplit", R.color.thirdBackground))
                characters.add(getCharacterByName(context,51, "Lucie Belle", R.color.mainBackground))
            }
            "8a" -> {
                characters.add(getCharacterByName(context,50, "Zoé Topaze", R.color.mainBackground))
                characters.add(getCharacterByName(context,48, "Chloé Winsplit", R.color.thirdBackground))
            }
            "8b" -> {
                characters.add(getCharacterByName(context,50, "Zoé Topaze", R.color.mainBackground))
                characters.add(getCharacterByName(context,48, "Chloé Winsplit", R.color.thirdBackground))
            }
            "8c" -> {
                characters.add(getCharacterByName(context,51, "Lucie Belle", R.color.mainBackground))
                characters.add(getCharacterByName(context,48, "Chloé Winsplit", R.color.thirdBackground))
            }
            "8d" -> {
                characters.add(getCharacterByName(context,50, "Zoé Topaze", R.color.mainBackground))
                characters.add(getCharacterByName(context,48, "Chloé Winsplit", R.color.thirdBackground))
            }
            "8e" -> {
                characters.add(getCharacterByName(context,51, "Lucie Belle", R.color.mainBackground))
                characters.add(getCharacterByName(context,48, "Chloé Winsplit", R.color.thirdBackground))
            }
            "8f" -> {
                characters.add(getCharacterByName(context,51, "Lucie Belle", R.color.mainBackground))
                characters.add(getCharacterByName(context,48, "Chloé Winsplit", R.color.thirdBackground))
            }
            "8g" -> {
                characters.add(getCharacterByName(context,51, "Lucie Belle", R.color.mainBackground))
                characters.add(getCharacterByName(context,48, "Chloé Winsplit", R.color.thirdBackground))
            }
            "8h" -> {
                characters.add(getCharacterByName(context,50, "Zoé Topaze", R.color.mainBackground))
                characters.add(getCharacterByName(context,48, "Chloé Winsplit", R.color.thirdBackground))
            }
            "9a" -> {
                if (symbols.condition(GlobalData.Game.FRIENDZONE2.id,"ZoeWithUs", "true")) {
                    characters.add(getCharacterByName(context,50, "Zoé Topaze", R.color.mainBackground))
                } else {
                    characters.add(getCharacterByName(context,51, "Lucie Belle", R.color.mainBackground))
                }
                characters.add(getCharacterByName(context,49, "Etranger", R.color.thirdBackground))
            }
            "11a" -> {
                characters.add(getCharacterByName(context,47, "Eva Belle", R.color.meBackgroundSms))
                characters.add(getCharacterByName(context,53, "No avatar", R.color.mainBackgroundSms))
            }
            "11b" -> {
                characters.add(getCharacterByName(context,47, "Eva Belle", R.color.meBackgroundSms))
                characters.add(getCharacterByName(context,53, "No avatar", R.color.mainBackgroundSms))
            }
            "11c" -> {
                characters.add(getCharacterByName(context,47, "Eva Belle", R.color.meBackgroundSms))
                characters.add(getCharacterByName(context,53, "No avatar", R.color.mainBackgroundSms))
            }
            "11d" -> {
                characters.add(getCharacterByName(context,47, "Eva Belle", R.color.meBackgroundSms))
                characters.add(getCharacterByName(context,53, "No avatar", R.color.mainBackgroundSms))
            }
            "11e" -> {
                characters.add(getCharacterByName(context,53, "No avatar", R.color.mainBackgroundSms))
            }
            "11f" -> {
                characters.add(getCharacterByName(context,53, "No avatar", R.color.mainBackgroundSms))
            }
            "12a" -> characters.add(getCharacterByName(context,47, "Eva Belle", R.color.mainBackground))
            else -> throw IllegalStateException("Unhandled code $chapterCode")
        }
        return characters
    }

    /**
     * Returns a Character given a Character's id
     *
     * @param characterId int
     * @return Character
     * @see Character
     */
    fun getCharacter(characterId: Int): Character {
        for (character in characters) {
            if (character.id == characterId) {
                return character
            }
        }
        if (BuildConfig.DEBUG && characterId != -1) {
            throw Resources.NotFoundException("Character not found for $code id : $characterId")
        }
        return Character.notFound(R.color.mainBackground)
    }

    /**
     * Returns a character given its name.
     *
     * @param id   int
     * @param name String
     * @return Character
     * @see Character
     */
    private fun getCharacterByName(context: Context, id: Int, name: String, colorId: Int): Character {
        when (name) {
            "Eva Belle" -> return Character(id, getFilePath(context, "eva"), getFilePath(context, "eva_seen"), name, colorId)
            "Lucie Belle" -> return Character(id, getFilePath(context, "lucie"), getFilePath(context, "lucie_seen"), name, colorId)
            "Sylvie Belle" -> return Character(id, getFilePath(context, "sylvie"), getFilePath(context, "sylvie_seen"), name, colorId)
            "Chloé Winsplit" -> return Character(id, getFilePath(context, "chloe"), getFilePath(context, "chloe_seen"), name, colorId)
            "Etranger" -> return Character(id, getFilePath(context, "stranger"), getFilePath(context, "stranger"), name, colorId)
            "No avatar" -> return Character(id, R.color.transparent, R.color.transparent, name, colorId)
            "Zoé Topaze" -> return Character(id, getFilePath(context, "zoe1_profil"), getFilePath(context, "zoe1_seen_"), name, colorId)
            "Me" -> return Character(id, getFilePath(context, "stranger, getFilePath(context"), getFilePath(context,"stranger"), name, colorId)
        }
        if (BuildConfig.DEBUG) {
            throw Resources.NotFoundException("Character not found for $code")
        }
        return Character.notFound(colorId)
    }

    private fun getFilePath(context: Context, name : String) : String {
        return OnlineAssetsManager.getImageFilePath(context, GlobalData.Game.FRIENDZONE2.id, name)
    }
}
