package fr.purpletear.friendzone2.activities.poetry;

import android.app.Activity;
import android.content.Context;

import com.bumptech.glide.RequestManager;
import com.example.sutokosharedelements.OnlineAssetsManager;

import fr.purpletear.friendzone2.R;
import purpletear.fr.purpleteartools.GlobalData;
import purpletear.fr.purpleteartools.Std;
import purpletear.fr.purpleteartools.TableOfSymbols;


class PoetryModel {
    /**
     * Contains the chapter's code
     */
    TableOfSymbols symbols;

    private RequestManager rm;

    private boolean isFirstStart;

    PoetryModel(TableOfSymbols symbols, RequestManager rm) {
        this.symbols = symbols;
        this.isFirstStart = true;
        this.rm = rm;
    }


    final RequestManager getRequestManager() {
        return rm;
    }

    final boolean isFirstStart() {
        boolean value = isFirstStart;
        isFirstStart = true;
        return value;
    }

    /**
     * Determines the background resource id for a chaptercode
     *
     * @return String
     */
    String getBackgroundResourceId(Activity activity) {
        return OnlineAssetsManager.INSTANCE.getImageFilePath(activity, String.valueOf(GlobalData.Game.FRIENDZONE2.getId()), "background_telling");
    }

    private String getResourceNameId() {
        String name = symbols.getChapterCode();
        switch (symbols.getChapterCode()) {
            case "7a":
                if(symbols.condition(GlobalData.Game.FRIENDZONE2.getId(), "side", "plage")) {
                    name = "7a_plage";
                } else {
                    name = "7a_parc";
                }
                break;
            case "9a": {
                if(symbols.condition(GlobalData.Game.FRIENDZONE2.getId(), "house", "front")) {
                    name = "9a1";
                } else {
                    name = "9a2";
                }
                break;
            }
        }
        return "poetry_" + name;
    }

    /**
     * Determines the text for a chaptercode
     *
     * @return int
     */
    String getText(Context c) {
        return c.getString(Std.getResourceIdFromName(c, getResourceNameId(), "string", R.string.poetry_not_found));
    }
}
