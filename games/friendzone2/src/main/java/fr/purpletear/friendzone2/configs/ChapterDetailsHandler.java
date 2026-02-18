/*
 * Copyright (C) PurpleTear, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Hocine Belbouab <hbdeveloppeur@gmail.com>, 1/10/19 11:38 AM
 *
 */

package fr.purpletear.friendzone2.configs;

import android.content.Context;

import com.example.sharedelements.OnlineAssetsManager;

import fr.purpletear.friendzone2.R;
import purpletear.fr.purpleteartools.GlobalData;
import purpletear.fr.purpleteartools.TableOfSymbols;

/**
 * The ChapterDetailHandler reads every chapters
 */
public class ChapterDetailsHandler {

    ChapterDetailsHandler() {

    }

    public static Chapter getChapter(Context c, String code, TableOfSymbols symbols) {
        switch (code) {
            case "1a": {
                return new Chapter(
                        code,
                        c.getString(R.string.chapter_1a_title),
                        c.getString(R.string.chapter_1a_description),
                        c.getString(R.string.chapter_1a_scn),
                        c.getString(R.string.mainactivity_online),
                        true,
                        OnlineAssetsManager.INSTANCE.getImageFilePath(c, GlobalData.Game.FRIENDZONE2.getId(), "chloe")
                );
            }
            case "2a": {
                return new Chapter(
                        code,
                        c.getString(R.string.chapter_2a_title),
                        c.getString(R.string.chapter_2a_description),
                        c.getString(R.string.alias_sylvie_belle),
                        c.getString(R.string.mainactivity_online),
                        true,
                        OnlineAssetsManager.INSTANCE.getImageFilePath(c, GlobalData.Game.FRIENDZONE2.getId(), "sylvie")
                );
            }
            case "3a": {
                return new Chapter(
                        code,
                        c.getString(R.string.chapter_3a_title),
                        c.getString(R.string.chapter_3a_description),
                        c.getString(R.string.alias_chloe_winsplit),
                        "",
                        true,
                        OnlineAssetsManager.INSTANCE.getImageFilePath(c, GlobalData.Game.FRIENDZONE2.getId(), "chloe")
                );
            }
            case "4a": {
                return new Chapter(
                        code,
                        c.getString(R.string.chapter_4a_title),
                        c.getString(R.string.chapter_4a_description),
                        c.getString(R.string.chapter_4a_scn),
                        c.getString(R.string.chapter_4a_ssn),
                        false,
                        OnlineAssetsManager.INSTANCE.getImageFilePath(c, GlobalData.Game.FRIENDZONE2.getId(), "cave")
                );
            }

            case "5a": {
                return new Chapter(
                        code,
                        c.getString(R.string.chapter_5a_title),
                        c.getString(R.string.chapter_5a_description),
                        c.getString(R.string.alias_zoe_topaze),
                        c.getString(R.string.mainactivity_online),
                        true,
                        OnlineAssetsManager.INSTANCE.getImageFilePath(c, GlobalData.Game.FRIENDZONE2.getId(), "zoe1_profil")
                );
            }

            case "5b": {
                return new Chapter(
                        code,
                        c.getString(R.string.chapter_5b_title),
                        c.getString(R.string.chapter_5b_description),
                        c.getString(R.string.alias_zoe_topaze),
                        c.getString(R.string.mainactivity_online),
                        true,
                        OnlineAssetsManager.INSTANCE.getImageFilePath(c, GlobalData.Game.FRIENDZONE2.getId(), "zoe1_profil")
                );
            }

            case "6a": {
                return new Chapter(
                        code,
                        c.getString(R.string.chapter_6a_title),
                        c.getString(R.string.chapter_6a_description),
                        c.getString(R.string.chapter_6a_scn),
                        (symbols.condition(GlobalData.Game.FRIENDZONE2.getId(), "side", "plage")) ? c.getString(R.string.sea_status) : c.getString(R.string.parc_status),
                        false,
                        (symbols.condition(GlobalData.Game.FRIENDZONE2.getId(), "side", "plage")) ? OnlineAssetsManager.INSTANCE.getImageFilePath(c, GlobalData.Game.FRIENDZONE2.getId(), "sea_profil") : OnlineAssetsManager.INSTANCE.getImageFilePath(c, GlobalData.Game.FRIENDZONE2.getId(), "forest")
                );
            }

            case "6b": {
                return new Chapter(
                        code,
                        c.getString(R.string.chapter_6b_title),
                        c.getString(R.string.chapter_6b_description),
                        c.getString(R.string.chapter_6b_scn),
                        c.getString(R.string.chapter_6b_ssn),
                        false,
                        OnlineAssetsManager.INSTANCE.getImageFilePath(c, GlobalData.Game.FRIENDZONE2.getId(), "night_profil")
                );
            }

            case "7a": {
                return new Chapter(
                        code,
                        c.getString(R.string.chapter_7a_title),
                        c.getString(R.string.chapter_7a_description),
                        c.getString(R.string.chapter_7a_scn),
                        "",
                        false,
                        OnlineAssetsManager.INSTANCE.getImageFilePath(c, GlobalData.Game.FRIENDZONE2.getId(), "chloe_zoe")
                );
            }

            case "7b": {
                return new Chapter(
                        code,
                        c.getString(R.string.chapter_7b_title),
                        c.getString(R.string.chapter_7b_description),
                        c.getString(R.string.chapter_7b_scn),
                        "",
                        false,
                        OnlineAssetsManager.INSTANCE.getImageFilePath(c, GlobalData.Game.FRIENDZONE2.getId(), "chloe_lucie")
                );
            }


            case "8a": {
                return new Chapter(
                        code,
                        c.getString(R.string.forest),
                        "",
                        c.getString(R.string.alias_zoe),
                        c.getString(R.string.forest_as_status),
                        false,
                        OnlineAssetsManager.INSTANCE.getImageFilePath(c, GlobalData.Game.FRIENDZONE2.getId(), "zoe1_profil")
                );
            }

            case "8b": {
                return new Chapter(
                        code,
                        c.getString(R.string.forest),
                        "",
                        c.getString(R.string.alias_zoe),
                        c.getString(R.string.forest_as_status),
                        false,
                        OnlineAssetsManager.INSTANCE.getImageFilePath(c, GlobalData.Game.FRIENDZONE2.getId(), "zoe1_profil")
                );
            }

            case "8c": {
                return new Chapter(
                        code,
                        c.getString(R.string.main_path),
                        "",
                        c.getString(R.string.alias_lucie),
                        c.getString(R.string.main_path_as_status),
                        false,
                        OnlineAssetsManager.INSTANCE.getImageFilePath(c, GlobalData.Game.FRIENDZONE2.getId(), "lucie_profil")
                );
            }

            case "8d": {
                return new Chapter(
                        code,
                        c.getString(R.string.main_path),
                        "",
                        c.getString(R.string.alias_zoe),
                        c.getString(R.string.main_path_as_status),
                        false,
                        OnlineAssetsManager.INSTANCE.getImageFilePath(c, GlobalData.Game.FRIENDZONE2.getId(), "zoe1_profil")
                );
            }

            case "8e": {
                return new Chapter(
                        code,
                        c.getString(R.string.forest),
                        "",
                        c.getString(R.string.alias_lucie),
                        c.getString(R.string.forest_as_status),
                        false,
                        OnlineAssetsManager.INSTANCE.getImageFilePath(c, GlobalData.Game.FRIENDZONE2.getId(), "lucie_profil")
                );
            }

            case "8f": {
                return new Chapter(
                        code,
                        c.getString(R.string.forest),
                        "",
                        c.getString(R.string.alias_lucie),
                        c.getString(R.string.forest_as_status),
                        false,
                        OnlineAssetsManager.INSTANCE.getImageFilePath(c, GlobalData.Game.FRIENDZONE2.getId(), "lucie_profil")
                );
            }


            case "8g": {
                return new Chapter(
                        code,
                        c.getString(R.string.main_path),
                        "",
                        c.getString(R.string.alias_lucie),
                        c.getString(R.string.main_path_as_status),
                        false,
                        OnlineAssetsManager.INSTANCE.getImageFilePath(c, GlobalData.Game.FRIENDZONE2.getId(), "lucie_profil")
                );
            }


            case "8h": {
                return new Chapter(
                        code,
                        c.getString(R.string.main_path),
                        "",
                        c.getString(R.string.alias_zoe),
                        c.getString(R.string.main_path_as_status),
                        false,
                        OnlineAssetsManager.INSTANCE.getImageFilePath(c, GlobalData.Game.FRIENDZONE2.getId(), "zoe1_profil")
                );
            }

            case "9a": {
                return new Chapter(
                        code,
                        c.getString(R.string.chapter_9a_title),
                        c.getString(R.string.chapter_9a_description),
                        c.getString(R.string.chapter_9a_scn),
                        c.getString(R.string.chapter_9a_scs),
                        false,
                        OnlineAssetsManager.INSTANCE.getImageFilePath(c, GlobalData.Game.FRIENDZONE2.getId(), "cave")
                );
            }



            case "12a": {
                return new Chapter(
                        "1a",
                        "Chapitre de test",
                        "Voici un test de la description",
                        c.getString(R.string.alias_eva_belle),
                        c.getString(R.string.mainactivity_offline),
                        true,
                        OnlineAssetsManager.INSTANCE.getImageFilePath(c, GlobalData.Game.FRIENDZONE2.getId(), "zoe1_profil")
                );
            }
            default:
                return new Chapter(
                        code,
                        c.getString(R.string.end),
                        "",
                        "",
                        "",
                        false,
                        ""
                );
        }
    }
}
