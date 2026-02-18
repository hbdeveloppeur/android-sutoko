/*
 * Copyright (C) PurpleTear, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Hocine Belbouab <hbdeveloppeur@gmail.com>, 1/10/19 11:38 AM
 *
 */

package fr.purpletear.friendzone2.tables
import android.content.Context
import fr.purpletear.friendzone2.R
class Character (
        /**
         * Contains the character's id
         */
        val id: Int,
        /**
         * Contains the id of the profilPicture
         */
        val imageId: Any,
        /**
         * Contains the small image id
         */
        val smallImageId: Any,
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
                R.color.meBackground -> return R.color.white
                R.color.mainBackground -> return R.color.mainText
                R.color.secondBackground -> return R.color.white
                R.color.noSeenBackground -> return R.color.white
                R.color.noSeenBackgroundDarker -> return R.color.white
                R.color.noSeenMeBackground -> return R.color.mainText
                R.color.thirdBackground -> return R.color.white
                R.color.mainBackgroundSms -> return R.color.white
                R.color.meBackgroundSms -> return R.color.mainText
            }
            return R.color.mainText
        }


    fun getTypingAnim(): Int {
        return when (colorId) {
            R.color.meBackground -> R.drawable.anim_istyping_whitefix
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
            when(Language.determinCode()) {
                Language.Companion.Code.DE -> {
                    var res = sentence.replace("\\bEva Belle\\b".toRegex(), context.getString(R.string.alias_eva_belle))
                    res = res.replace("\\bEva\\b".toRegex(), context.getString(R.string.alias_eva))
                    res = res.replace("\\bEVA\\b".toRegex(), context.getString(R.string.alias_eva).uppercase())
                    res = res.replace("\\bEvas\\b".toRegex(), context.getString(R.string.alias_eva) + "s")
                    res = res.replace("\\bZoé Topaze\\b".toRegex(), context.getString(R.string.alias_zoe_topaze))
                    res = res.replace("\\bZoe Topaze\\b".toRegex(), context.getString(R.string.alias_zoe_topaze))
                    res = res.replace("\\bZoe\\b".toRegex(), context.getString(R.string.alias_zoe))
                    res = res.replace("\\bZoé\\b".toRegex(), context.getString(R.string.alias_zoe))
                    res = res.replace("\\bZoes\\b".toRegex(), context.getString(R.string.alias_zoe) + "s")
                    res = res.replace("\\bZoés\\b".toRegex(), context.getString(R.string.alias_zoe) + "s")
                    res = res.replace("\\bChristophe Belle\\b".toRegex(), context.getString(R.string.alias_christophe_belle))
                    res = res.replace("\\bChristophe\\b".toRegex(), context.getString(R.string.alias_christophe))
                    res = res.replace("\\bChristophes\\b".toRegex(), context.getString(R.string.alias_christophe) + "s")
                    res = res.replace("\\bSylvie Belle\\b".toRegex(), context.getString(R.string.alias_sylvie_belle))
                    res = res.replace("\\bSylvie\\b".toRegex(), context.getString(R.string.alias_sylvie))
                    res = res.replace("\\bSylvies\\b".toRegex(), context.getString(R.string.alias_sylvie) + "s")
                    res = res.replace("\\bLucie Belle\\b".toRegex(), context.getString(R.string.alias_lucie_belle))
                    res = res.replace("\\bLucie\\b".toRegex(), context.getString(R.string.alias_lucie))
                    res = res.replace("\\bChloé Winsplit\\b".toRegex(), context.getString(R.string.alias_chloe_winsplit))
                    res = res.replace("\\bChloe Winsplit\\b".toRegex(), context.getString(R.string.alias_chloe_winsplit))
                    res = res.replace("\\bChloé\\b".toRegex(), context.getString(R.string.alias_chloe))
                    res = res.replace("\\bChloe\\b".toRegex(), context.getString(R.string.alias_chloe))
                    res = res.replace("\\bChloés\\b".toRegex(), context.getString(R.string.alias_chloe) + "s")
                    res = res.replace("\\bChloes\\b".toRegex(), context.getString(R.string.alias_chloe) + "s")
                    res = res.replace("\\bBryan\\b".toRegex(), context.getString(R.string.alias_bryan))
                    res = res.replace("\\bBryans\\b".toRegex(), context.getString(R.string.alias_bryan) + "s")
                    return res
                }
                else -> {
                    var res = sentence.replace("\\bEva Belle\\b".toRegex(), context.getString(R.string.alias_eva_belle))
                    res = res.replace("\\bEva\\b".toRegex(), context.getString(R.string.alias_eva))
                    res = res.replace("\\bEVA\\b".toRegex(), context.getString(R.string.alias_eva).uppercase())
                    res = res.replace("\\bZoé Topaze\\b".toRegex(), context.getString(R.string.alias_zoe_topaze))
                    res = res.replace("\\bZoe Topaze\\b".toRegex(), context.getString(R.string.alias_zoe_topaze))
                    res = res.replace("\\bZoe\\b".toRegex(), context.getString(R.string.alias_zoe))
                    res = res.replace("\\bZoé\\b".toRegex(), context.getString(R.string.alias_zoe))
                    res = res.replace("\\bChristophe Belle\\b".toRegex(), context.getString(R.string.alias_christophe_belle))
                    res = res.replace("\\bChristophe\\b".toRegex(), context.getString(R.string.alias_christophe))
                    res = res.replace("\\bSylvie Belle\\b".toRegex(), context.getString(R.string.alias_sylvie_belle))
                    res = res.replace("\\bSylvie\\b".toRegex(), context.getString(R.string.alias_sylvie))
                    res = res.replace("\\bLucie Belle\\b".toRegex(), context.getString(R.string.alias_lucie_belle))
                    res = res.replace("\\bLucie\\b".toRegex(), context.getString(R.string.alias_lucie))
                    res = res.replace("\\bBryan\\b".toRegex(), context.getString(R.string.alias_bryan))
                    res = res.replace("\\bChloé Winsplit\\b".toRegex(), context.getString(R.string.alias_chloe_winsplit))
                    res = res.replace("\\bChloe Winsplit\\b".toRegex(), context.getString(R.string.alias_chloe_winsplit))
                    res = res.replace("\\bChloé\\b".toRegex(), context.getString(R.string.alias_chloe))
                    res = res.replace("\\bChloe\\b".toRegex(), context.getString(R.string.alias_chloe))
                    return res
                }
            }
        }
    }
}
