/*
 * Copyright (C) PurpleTear, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Hocine Belbouab <hbdeveloppeur@gmail.com>, 1/10/19 11:38 AM
 *
 */

package friendzone3.purpletear.fr.friendzon3.custom

import android.app.Activity
import android.content.Context
import com.google.gson.annotations.SerializedName
import friendzone3.purpletear.fr.friendzon3.R
import friendzone3.purpletear.fr.friendzon3.config.Language
import purpletear.fr.purpleteartools.Std

class Character (
        /**
         * Contains the character's id
         */
        @SerializedName("id")
        val id: Int,

        /**
         * Contains the small image id
         */
        @SerializedName("fname")
        val firstName: String,

        /**
         * Contains the first name of the character
         */
        @SerializedName("lname")
        val lastName: String,

        /**
         * Contains the first name of the character
         */
        @SerializedName("isMainCharacter")
        val isMainCharacter: Boolean) {


    companion object {
        fun updateNames(context:Context, sentence:  String) : String {
            when (Language.determinCode()) {
                Language.Code.DE -> {
                    var res = sentence.replace("\\bEva Belle\\b".toRegex(), context.getString(R.string.fz3_personnage_eva_belle))
                    res = res.replace("\\bEva\\b".toRegex(), context.getString(R.string.fz3_personnage_eva))
                    res = res.replace("\\bEVA\\b".toRegex(), context.getString(R.string.fz3_personnage_eva).uppercase())
                    res = res.replace("\\bEvas\\b".toRegex(), context.getString(R.string.fz3_personnage_eva) + "s")
                    res = res.replace("\\bZoé Topaze\\b".toRegex(), context.getString(R.string.fz3_personnage_zoe_topaze))
                    res = res.replace("\\bZoe Topaze\\b".toRegex(), context.getString(R.string.fz3_personnage_zoe_topaze))
                    res = res.replace("\\bZoe\\b".toRegex(), context.getString(R.string.fz3_personnage_zoe))
                    res = res.replace("\\bZoé\\b".toRegex(), context.getString(R.string.fz3_personnage_zoe))
                    res = res.replace("\\bZoes\\b".toRegex(), context.getString(R.string.fz3_personnage_zoe) + "s")
                    res = res.replace("\\bZoés\\b".toRegex(), context.getString(R.string.fz3_personnage_zoe) + "s")
                    res = res.replace("\\bChristophe Belle\\b".toRegex(), context.getString(R.string.fz3_personnage_christophe_belle))
                    res = res.replace("\\bChristophe\\b".toRegex(), context.getString(R.string.fz3_personnage_christophe))
                    res = res.replace("\\bChristophes\\b".toRegex(), context.getString(R.string.fz3_personnage_christophe) + "s")
                    res = res.replace("\\bSylvie Belle\\b".toRegex(), context.getString(R.string.fz3_personnage_sylvie_belle))
                    res = res.replace("\\bSylvie\\b".toRegex(), context.getString(R.string.fz3_personnage_sylvie))
                    res = res.replace("\\bSylvies\\b".toRegex(), context.getString(R.string.fz3_personnage_sylvie) + "s")
                    res = res.replace("\\bLucie Belle\\b".toRegex(), context.getString(R.string.fz3_personnage_lucie_belle))
                    res = res.replace("\\bLucie\\b".toRegex(), context.getString(R.string.fz3_personnage_lucie))
                    res = res.replace("\\bChloé Winsplit\\b".toRegex(), context.getString(R.string.fz3_personnage_chloe_winsplit))
                    res = res.replace("\\bChloe Winsplit\\b".toRegex(), context.getString(R.string.fz3_personnage_chloe_winsplit))
                    res = res.replace("\\bChloé\\b".toRegex(), context.getString(R.string.fz3_personnage_chloe))
                    res = res.replace("\\bChloe\\b".toRegex(), context.getString(R.string.fz3_personnage_chloe))
                    res = res.replace("\\bChloés\\b".toRegex(), context.getString(R.string.fz3_personnage_chloe) + "s")
                    res = res.replace("\\bChloes\\b".toRegex(), context.getString(R.string.fz3_personnage_chloe) + "s")
                    res = res.replace("\\bBryan\\b".toRegex(), context.getString(R.string.fz3_personnage_bryan))
                    res = res.replace("\\bBryans\\b".toRegex(), context.getString(R.string.fz3_personnage_bryan) + "s")
                    res = res.replace("\\bLenas\\b".toRegex(), context.getString(R.string.fz3_personnage_lana) + "s")
                    res = res.replace("\\bLanas\\b".toRegex(), context.getString(R.string.fz3_personnage_lana) + "s")
                    res = res.replace("\\bLena\\b".toRegex(), context.getString(R.string.fz3_personnage_lana))
                    return res
                }
                else -> {
                    var res = sentence.replace("\\bEva Belle\\b".toRegex(), context.getString(R.string.fz3_personnage_eva_belle))
                    res = res.replace("\\bEva\\b".toRegex(), context.getString(R.string.fz3_personnage_eva))
                    res = res.replace("\\bEVA\\b".toRegex(), context.getString(R.string.fz3_personnage_eva).uppercase())
                    res = res.replace("\\bZoé Topaze\\b".toRegex(), context.getString(R.string.fz3_personnage_zoe_topaze))
                    res = res.replace("\\bZoe Topaze\\b".toRegex(), context.getString(R.string.fz3_personnage_zoe_topaze))
                    res = res.replace("\\bZoe\\b".toRegex(), context.getString(R.string.fz3_personnage_zoe))
                    res = res.replace("\\bZoé\\b".toRegex(), context.getString(R.string.fz3_personnage_zoe))
                    res = res.replace("\\bChristophe Belle\\b".toRegex(), context.getString(R.string.fz3_personnage_christophe_belle))
                    res = res.replace("\\bChristophe\\b".toRegex(), context.getString(R.string.fz3_personnage_christophe))
                    res = res.replace("\\bSylvie Belle\\b".toRegex(), context.getString(R.string.fz3_personnage_sylvie_belle))
                    res = res.replace("\\bSylvie\\b".toRegex(), context.getString(R.string.fz3_personnage_sylvie))
                    res = res.replace("\\bLucie Belle\\b".toRegex(), context.getString(R.string.fz3_personnage_lucie_belle))
                    res = res.replace("\\bLucy Belle\\b".toRegex(), context.getString(R.string.fz3_personnage_lucie_belle))
                    res = res.replace("\\bLucie\\b".toRegex(), context.getString(R.string.fz3_personnage_lucie))
                    res = res.replace("\\bLucy\\b".toRegex(), context.getString(R.string.fz3_personnage_lucie))
                    res = res.replace("\\bBryan\\b".toRegex(), context.getString(R.string.fz3_personnage_bryan))
                    res = res.replace("\\bChloé Winsplit\\b".toRegex(), context.getString(R.string.fz3_personnage_chloe_winsplit))
                    res = res.replace("\\bChloe Winsplit\\b".toRegex(), context.getString(R.string.fz3_personnage_chloe_winsplit))
                    res = res.replace("\\bChloé\\b".toRegex(), context.getString(R.string.fz3_personnage_chloe))
                    res = res.replace("\\bChloe\\b".toRegex(), context.getString(R.string.fz3_personnage_chloe))
                    res = res.replace("\\bLena\\b".toRegex(), context.getString(R.string.fz3_personnage_lana))
                    res = res.replace("\\bChristophe Belle\\b".toRegex(), context.getString(R.string.fz3_personnage_christophe_belle))
                    res = res.replace("\\bChristophe\\b".toRegex(), context.getString(R.string.fz3_personnage_christophe))
                    res = res.replace("\\bChristopher\\b".toRegex(), context.getString(R.string.fz3_personnage_christophe))
                    return res
                }
            }
        }
    }

    override fun toString(): String {
        return "Character{" +
                "id=" + id +
                ", firstName=" + firstName +
                ", lastName=" + lastName +
                ", isMainCharacter=" + isMainCharacter +
                '}'.toString()
    }
}
