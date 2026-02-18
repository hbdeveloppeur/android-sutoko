/*
 * Copyright (C) PurpleTear, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Hocine Belbouab <hbdeveloppeur@gmail.com>, 1/10/19 11:38 AM
 *
 */

package fr.purpletear.friendzone.tables

import android.content.res.Resources
import fr.purpletear.friendzone.BuildConfig
import fr.purpletear.friendzone.R
import purpletear.fr.purpleteartools.GlobalData
import purpletear.fr.purpleteartools.TableOfSymbols
import java.util.*

class TableOfCharacters(private var code: String, symbols : TableOfSymbols) {
    /**
     * Contains the list of characters of the story
     */
    private var characters: List<Character> = ArrayList()

    init {
        characters = getCharacters(code, symbols)
    }

    /**
     * Returns a list of Characters for the given chapterCode
     *
     * @param chapterCode String
     * @return List Characters
     */
    @Throws
    private fun getCharacters(chapterCode: String, symbols : TableOfSymbols): List<Character> {
        val characters = ArrayList<Character>()
        characters.add(getCharacterByName(0, "Me", R.color.meBackground, symbols))
        when (chapterCode.lowercase(Locale.getDefault())) {
            "1a" -> {characters.add(getCharacterByName(27, "Eva Belle", R.color.mainBackground, symbols))}
            "2a" -> {characters.add(getCharacterByName(27, "Eva Belle", R.color.mainBackground, symbols))}
            "3a" -> {
                characters.add(getCharacterByName(27, "Eva Belle", R.color.mainBackground, symbols))
                characters.add(getCharacterByName(42, "Zoé Topaze", R.color.secondBackground, symbols))
            }
            "4a" -> {characters.add(getCharacterByName(43, "Christophe Belle", R.color.mainBackground, symbols))}
            "5a" -> {characters.add(getCharacterByName(42, "Inconnue", R.color.mainBackground, symbols))}
            "6a" -> {characters.add(getCharacterByName(27, "Eva Belle", R.color.mainBackground, symbols))}
            "7a" -> {characters.add(getCharacterByName(42, "Zoé Topaze 2", R.color.mainBackground, symbols))}
            "7b" -> {characters.add(getCharacterByName(42, "Zoé Topaze unknown", R.color.mainBackground, symbols))}
            "8a" -> {
                characters.add(getCharacterByName(42, "Zoé Topaze 2", R.color.mainBackground, symbols))
                characters.add(getCharacterByName(44, "Serveur", R.color.thirdBackground, symbols))
            }
            "8b" -> {
                characters.add(getCharacterByName(42, "Zoé Topaze 2", R.color.mainBackground, symbols))
                characters.add(getCharacterByName(45, "Bryan", R.color.thirdBackground, symbols))
            }
            "9c" -> {
                characters.add(getCharacterByName(42, "Zoé Topaze 2", R.color.mainBackground, symbols))
                characters.add(getCharacterByName(45, "Bryan", R.color.thirdBackground, symbols))
            }
            "12a" -> characters.add(getCharacterByName(46, "Eva Belle", R.color.mainBackground, symbols))
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
        if (BuildConfig.DEBUG) {
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
    private fun getCharacterByName(id: Int, name: String, colorId: Int, symbols : TableOfSymbols): Character {
        when (name) {
            "Me" -> return Character(id, R.color.transparent, R.color.transparent, name, colorId)
            "Eva Belle" -> return Character(id, R.drawable.eva, R.drawable.eva_seen, name, colorId)
            "Zoé Topaze" -> {
                if(symbols.condition(GlobalData.Game.FRIENDZONE.id, "zoepp", "1")) {
                    return Character(id, R.drawable.zoe1_profil, R.drawable.zoe1_seen_, name, colorId)
                } else if(symbols.condition(GlobalData.Game.FRIENDZONE.id, "zoepp","2")) {
                    return Character(id, R.drawable.zoe2_profil, R.drawable.zoe2_seen_ , name, colorId)
                }
                return Character(id, R.drawable.zoe, R.drawable.zoe_seen , name, colorId)

            }
            "Zoé Topaze 2" -> {

                if(symbols.condition(GlobalData.Game.FRIENDZONE.id, "zoepp", "1")) {
                    return Character(id, R.drawable.zoe1_profil, R.drawable.zoe1_seen_, name, colorId)
                } else if(symbols.condition(GlobalData.Game.FRIENDZONE.id, "zoepp","2")) {
                    return Character(id, R.drawable.zoe2_profil, R.drawable.zoe2_seen_ , name, colorId)
                }
                return Character(id, R.drawable.zoefutur, R.drawable.zoefutur_seen , name, colorId)
            }
             "Zoé Topaze unknown" -> {

                 if(symbols.condition(GlobalData.Game.FRIENDZONE.id, "zoepp", "1")) {
                     return Character(id, R.drawable.zoe1_profil, R.drawable.zoe1_seen_, name, colorId)
                 } else if(symbols.condition(GlobalData.Game.FRIENDZONE.id, "zoepp","2")) {
                     return Character(id, R.drawable.zoe2_profil, R.drawable.zoe2_seen_ , name, colorId)
                 }
                 return Character(id, R.drawable.no_avatar, R.drawable.no_avatar_seen, name, colorId)
             }
            "Inconnue" -> {
                return Character(id, R.drawable.inconnue, R.drawable.inconnue , name, colorId)
            }
            "Christophe Belle" -> {return Character(id, R.drawable.christophe, R.drawable.christophe_seen , name, colorId)}
            "Bryan" -> {
                return Character(id, R.color.transparent,  R.color.transparent , name, colorId)
            }
            "Serveur" -> {
            return Character(id,  R.color.transparent,  R.color.transparent , name, colorId)
        }
        }
        if (BuildConfig.DEBUG) {
            throw Resources.NotFoundException("Character $name not found for $code")
        }
        return Character.notFound(colorId)
    }
}
