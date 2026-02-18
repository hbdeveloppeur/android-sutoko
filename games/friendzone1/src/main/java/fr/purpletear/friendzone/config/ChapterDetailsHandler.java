/*
 * Copyright (C) PurpleTear, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Hocine Belbouab <hbdeveloppeur@gmail.com>, 1/10/19 11:38 AM
 *
 */

package fr.purpletear.friendzone.config;

import android.content.Context;

import fr.purpletear.friendzone.R;

/**
 * The ChapterDetailHandler reads every chapters
 */
public class ChapterDetailsHandler {

    ChapterDetailsHandler() {

    }

    public static Chapter getChapter(Context c, String code) {
        switch (code) {
            case "12a": {
                return new Chapter(
                        code,
                        "Chapitre de test",
                        "Voici un test de la description",
                        c.getString(R.string.alias_eva_belle),
                        c.getString(R.string.mainactivity_offline),
                        true,
                        R.drawable.eva
                );
            }

            case "1a":
                return new Chapter(
                        code,
                        c.getString(R.string.chapter1_title),
                        c.getString(R.string.chapter1_description),
                        c.getString(R.string.alias_eva_belle),
                        c.getString(R.string.mainactivity_offline),
                        true,
                        R.drawable.eva
                );

            case "2a":

                return new Chapter(code,
                        c.getString(R.string.chapter2_title),
                        c.getString(R.string.chapter2_description),
                        c.getString(R.string.alias_eva_belle),
                        c.getString(R.string.mainactivity_online),
                        true,
                        R.drawable.eva);

            case "3a":

                return new Chapter(code,
                        c.getString(R.string.chapter3_title),
                        c.getString(R.string.chapter3_description),
                        c.getString(R.string.alias_eva) + " & " + c.getString(R.string.alias_zoe),
                        c.getString(R.string.mainactivity_online),
                        true,
                        R.drawable.eva_zoe);


            case "4a":
                return new Chapter(code,
                        c.getString(R.string.chapter4_title),
                        "",
                        c.getString(R.string.alias_christophe_belle),
                        c.getString(R.string.mainactivity_online),
                        true,
                        R.drawable.christophe
                );

            case "5a":
                return new Chapter(
                        code,
                        c.getString(R.string.chapter5_title),
                        c.getString(R.string.chapter5_description),
                        c.getString(R.string.inconnue),
                        c.getString(R.string.chapter5_scn),
                        false,
                        R.drawable.inconnue);

            case "6a":

                return new Chapter(
                        code,
                        c.getString(R.string.chapter6_title),
                        c.getString(R.string.chapter6_description),
                        c.getString(R.string.alias_eva_belle),
                        c.getString(R.string.mainactivity_online),
                        false,
                        R.drawable.eva);

            case "7a":

                return new Chapter(
                        code,
                        c.getString(R.string.chapter7a_title),
                        c.getString(R.string.chapter7a_description),
                        c.getString(R.string.alias_zoe_topaze),
                        c.getString(R.string.chapter7a_scn),
                        true,
                        R.drawable.zoefutur
                );

            case "7b":
                return new Chapter(
                        code,
                        c.getString(R.string.chapter7a_title),
                        c.getString(R.string.chapter7a_description),
                        c.getString(R.string.alias_zoe) + " T.6364",
                        c.getString(R.string.mainactivity_online),
                        true,
                        R.drawable.no_avatar
                );

            case "8a":
                return new Chapter(
                        code,
                        c.getString(R.string.chapter8_title),
                        c.getString(R.string.chapter8_description),
                        c.getString(R.string.chapter8_scn),
                        "",
                        false,
                        R.drawable.zoefutur
                );

            case "8b":
                return new Chapter(
                        code,
                        c.getString(R.string.chapter8_title),
                        c.getString(R.string.chapter8_description),
                        c.getString(R.string.chapter8_scn),
                        "",
                        false,
                        R.drawable.zoefutur
                );

            case "9c":
                return new Chapter(
                        code,
                        c.getString(R.string.chapter9_title),
                        c.getString(R.string.chapter9_description),
                        c.getString(R.string.chapter8_scn),
                        "",
                        false,
                        R.drawable.zoefutur
                );

            default:
                return new Chapter(
                        code,
                        c.getString(R.string.end),
                        "",
                        "",
                        "",
                        false,
                        R.color.transparent
                );
        }
    }
}
