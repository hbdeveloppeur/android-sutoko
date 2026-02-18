package fr.purpletear.friendzone4.game.config;

import android.content.Context;

import fr.purpletear.friendzone4.R;
import purpletear.fr.purpleteartools.GlobalData;
import purpletear.fr.purpleteartools.TableOfSymbols;


/**
 * The ChapterDetailHandler reads every chapters
 * from the assets/json/chapters.json
 */
public class ChapterDetailsHandler {

    ChapterDetailsHandler() {

    }
    public static Chapter getChapter(Context c, TableOfSymbols symbols, String code) {
        switch (code.toLowerCase()) {
            case "1a": {
                return new Chapter(
                        "1a",
                        "default",
                        c.getString(R.string.fz4_chapter_1a_title),
                        c.getString(R.string.fz4_chapter_1a_description),
                        false,
                        "Zoé Topaze",
                        c.getString(R.string.online),
                        "zoe",
                        true
                );
            }
            case "2a": {
                return new Chapter(
                        "2a",
                        "default",
                        c.getString(R.string.fz4_chapter_2a_title),
                        c.getString(R.string.fz4_chapter_2a_description),
                        false,
                        "Zoé & Lucie",
                        c.getString(R.string.online),
                        "zoe_lucie",
                        true);
            }
            case "3a": {
                return new Chapter("3a",
                        "default",
                        c.getString(R.string.fz4_chapter_3a_title),
                        c.getString(R.string.fz4_chapter_3a_description),
                        false,
                        "Eva Belle",
                        c.getString(R.string.online),
                        "eva",
                        true);
            }

            case "4a": {
                return new Chapter("4a",
                        "default",
                        c.getString(R.string.fz4_chapter_4a_title),
                        "",
                        false,
                        c.getString(R.string.fz4_chapter_4a_cname),
                        c.getString(R.string.fz4_chapter_4a_cstatus),
                        "zoe",
                        false);
            }
            case "4b": {
                return new Chapter("4b",
                        "default",
                        c.getString(R.string.fz4_chapter_4b_title),
                        c.getString(R.string.fz4_chapter_4b_description),
                        false,
                        "Eva Belle",
                        c.getString(R.string.online),
                        "eva",
                        true
                );
            }
            case "5a": {
                if (symbols.condition(GlobalData.Game.FRIENDZONE4.getId(), "barWithZoe", "true")) {
                    return new Chapter(
                            "5a",
                            "barWithZoe==true",
                            c.getString(R.string.fz4_chapter_5a_title),
                            c.getString(R.string.fz4_chapter_5a_description),
                            false,
                            c.getString(R.string.fz4_chapter_5a_cname),
                            "",
                            "zoe",
                            false);
                }
                return new Chapter(
                        "5a",
                        "default",
                        c.getString(R.string.fz4_chapter_5a_title),
                        c.getString(R.string.fz4_chapter_5abis_description),
                        false,
                        c.getString(R.string.fz4_chapter_5abis_cname),
                        "",
                        "zoe", false);
            }
            case "6a": {
                return new Chapter(
                        "6a",
                        "default",
                        c.getString(R.string.fz4_chapter_6a_title),
                        c.getString(R.string.fz4_chapter_6a_description),
                        false,
                        c.getString(R.string.fz4_chapter_6a_cname),
                        "",
                        "lake",
                        false);
            }
            case "7a": {
                return new Chapter(
                        "7a",
                        "default",
                        c.getString(R.string.fz4_chapter_7a_title),
                        c.getString(R.string.fz4_chapter_7a_description),
                        false,
                        "Yorokobi",
                        c.getString(R.string.fz4_chapter_7a_cstatus),
                        "yorokobi",
                        false
                );
            }
            case "7b": {
                return new Chapter(
                        "7b",
                        "default",
                        c.getString(R.string.fz4_chapter_7b_title),
                        "",
                        false,
                        "Eva Belle",
                        c.getString(R.string.online),
                        "eva",
                        true
                );
            }
            case "8a": {
                return new Chapter(
                        "8a",
                        "default",
                        c.getString(R.string.fz4_chapter_8a_title),
                        c.getString(R.string.fz4_chapter_8a_description),
                        false,
                        "Yorokobi",
                        c.getString(R.string.fz4_chapter_8a_cstatus),
                        "yorokobi",
                        false);
            }

            case "8b": {
                return new Chapter (
                        "8b",
                        "default",
                        c.getString(R.string.fz4_chapter_8b_title),
                        c.getString(R.string.fz4_chapter_8b_description),
                        false,
                        c.getString(R.string.fz4_chapter_8b_cname),
                        c.getString(R.string.fz4_chapter_8b_cstatus),
                        "zoe",
                        false);
            }

            case "9a": {
                return new Chapter(
                        "9a",
                        "default",
                        c.getString(R.string.fz4_chapter_9a_title),
                        c.getString(R.string.fz4_chapter_9a_description),
                        false,
                        c.getString(R.string.fz4_chapter_9a_cname),
                        c.getString(R.string.fz4_chapter_9a_cstatus),
                        "transparent",
                        false);
            }
            case "9c" : {
                return new Chapter(
                        "9c",
                        "default",
                        c.getString(R.string.fz4_chapter_9c_title),
                        c.getString(R.string.fz4_chapter_9c_description),
                        false,
                        c.getString(R.string.fz4_chapter_9c_cname),
                        c.getString(R.string.fz4_chapter_9a_cstatus),
                        "thunder",
                        false);
            }

            case "10a" : {
                return new Chapter(
                        "10a",
                        "default",
                        c.getString(R.string.fz4_chapter_10a_title),
                        "",
                        false,
                        "Emma",
                        c.getString(R.string.online),
                        "emma",
                        true);
            }
            case "10b" : {
                return new Chapter(
                        "10b",
                        "default",
                        c.getString(R.string.fz4_chapter_10b_title),
                        "",
                        false,
                        "Kim Erika",
                        c.getString(R.string.online),
                        "kim",
                        true);
            }

            case "10c": {
                return new Chapter(
                        "10c",
                        "default",
                        c.getString(R.string.fz4_chapter_10c_title),
                        "",
                        false,
                        "Eva & Zoé",
                        c.getString(R.string.online),
                        "eva_zoe_candle",
                        true
                );
            }
            case "11a": {
                return new Chapter(
                        "11a",
                        "default",
                        c.getString(R.string.fz4_chapter_11a_title),
                        "",
                        false,
                        "",
                        "",
                        "kim",
                        true);
            }

            case "11b" : {
                return new Chapter("11b",
                        "default",
                        c.getString(R.string.fz4_chapter_11b_title),
                        "",
                        false,
                        "",
                        "",
                        "kim",
                        true);
            }

            case "11k" : {
                return new Chapter(
                        "11k",
                        "default",
                        c.getString(R.string.fz4_chapter_11k_title),
                        "",
                        false,
                        "",
                        "",
                        "kim",
                        true);
            }
            default:
                throw new IllegalStateException("Trying to find chapter " + code + " but not there.");
        }
    }
}
