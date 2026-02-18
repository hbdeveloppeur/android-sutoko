/*
 * Copyright (C) PurpleTear, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Hocine Belbouab <hbdeveloppeur@gmail.com>, 1/10/19 11:38 AM
 *
 */

package fr.purpletear.friendzone.tables
import android.content.Context
import fr.purpletear.friendzone.R

class Character (
        /**
         * Contains the character's id
         */
        val id: Int,

        /**
         * Contains the id of the profilPicture
         */
        val imageId: Int,

        /**
         * Contains the small image id
         */
        val smallImageId: Int,

        /**
         * Contains the first name of the character
         */
        val name: String,

        /**
         * Contains the color's id
         * int
         */
        val colorId: Int) {

    val textColorId: Int
        get() {
            when (colorId) {
                R.color.mainBackground -> return R.color.mainText
                R.color.secondBackground -> return R.color.white
                R.color.noSeenBackground -> return R.color.white
                R.color.noSeenBackgroundDarker -> return R.color.white
                R.color.noSeenMeBackground -> return R.color.mainText
                R.color.thirdBackground -> return R.color.white
            }
            return R.color.mainText
        }


    fun getTypingAnim(isSms : Boolean): Int {
        if(isSms) {
            return R.drawable.anim_istyping_whitefix
        }
        return when (colorId) {
            R.color.secondBackground -> R.drawable.anim_istyping_whitefix
            else -> R.drawable.anim_istyping_gray
        }
    }

    override fun toString(): String {
        return "Character{" +
                "id=" + id +
                ", imageId=" + imageId +
                ", smallImageId=" + smallImageId +
                ", name='" + name + '\''.toString() +
                '}'.toString()
    }

    companion object {

        fun notFound(colorId: Int): Character {
            return Character(-1, R.color.transparent, R.color.transparent, "Personnage non trouvé", colorId)
        }

        /**
         * Updates the characters' name
         * @param context : Context
         * @param sentence : String, the original sentence to update
         * @return String, the update sentences
         */
        fun updateNames(context : Context, sentence : String) : String {

            when(Language.determineLangCode()) {
                Language.Code.JA -> {
                    var res = sentence.replace("Eva Belle".toRegex(), context.getString(R.string.alias_eva_belle))
                    res = res.replace("Eva".toRegex(), context.getString(R.string.alias_eva))
                    res = res.replace("Zoé Topaze".toRegex(), context.getString(R.string.alias_zoe_topaze))
                    res = res.replace("Zoe Topaze".toRegex(), context.getString(R.string.alias_zoe_topaze))
                    res = res.replace("Zoe".toRegex(), context.getString(R.string.zoe))
                    res = res.replace("Zoé".toRegex(), context.getString(R.string.zoe))
                    res = res.replace("Christophe Belle".toRegex(), context.getString(R.string.alias_christophe_belle))
                    res = res.replace("Christophe".toRegex(), context.getString(R.string.alias_christophe))
                    res = res.replace("Bryan".toRegex(), context.getString(R.string.alias_bryan))
                    res = res.replace("Nick".toRegex(), context.getString(R.string.default_name))
                    return res
                }

                Language.Code.DE -> {
                    var res = sentence.replace("\\bEva Belle\\b".toRegex(), context.getString(R.string.alias_eva_belle))
                    res = res.replace("\\bEva\\b".toRegex(), context.getString(R.string.alias_eva))
                    res = res.replace("\\bZoé Topaze\\b".toRegex(), context.getString(R.string.alias_zoe_topaze))
                    res = res.replace("\\bZoe Topaze\\b".toRegex(), context.getString(R.string.alias_zoe_topaze))
                    res = res.replace("\\bZoe\\b".toRegex(), context.getString(R.string.zoe))
                    res = res.replace("\\bZoé\\b".toRegex(), context.getString(R.string.zoe))
                    res = res.replace("\\bZoes\\b".toRegex(), context.getString(R.string.zoe) + "s")
                    res = res.replace("\\bZoés\\b".toRegex(), context.getString(R.string.zoe) + "s")
                    res = res.replace("\\bChristophe Belle\\b".toRegex(), context.getString(R.string.alias_christophe_belle))
                    res = res.replace("\\bChristophe\\b".toRegex(), context.getString(R.string.alias_christophe))
                    res = res.replace("\\bBryan\\b".toRegex(), context.getString(R.string.alias_bryan))
                    return res
                }

                else -> {
                    var res = sentence.replace("\\bEva Belle\\b".toRegex(), context.getString(R.string.alias_eva_belle))
                    res = res.replace("\\bEva\\b".toRegex(), context.getString(R.string.alias_eva))
                    res = res.replace("\\bZoé Topaze\\b".toRegex(), context.getString(R.string.alias_zoe_topaze))
                    res = res.replace("\\bZoe Topaze\\b".toRegex(), context.getString(R.string.alias_zoe_topaze))
                    res = res.replace("\\bZoe\\b".toRegex(), context.getString(R.string.zoe))
                    res = res.replace("\\bZoé\\b".toRegex(), context.getString(R.string.zoe))
                    res = res.replace("\\bChristophe Belle\\b".toRegex(), context.getString(R.string.alias_christophe_belle))
                    res = res.replace("\\bChristophe\\b".toRegex(), context.getString(R.string.alias_christophe))
                    res = res.replace("\\bBryan\\b".toRegex(), context.getString(R.string.alias_bryan))
                    return res
                }
            }
        }
    }
}
