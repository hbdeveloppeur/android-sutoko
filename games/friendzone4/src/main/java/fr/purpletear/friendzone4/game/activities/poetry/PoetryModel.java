package fr.purpletear.friendzone4.game.activities.poetry;

import android.content.Context;

import com.bumptech.glide.RequestManager;

import fr.purpletear.friendzone4.R;
import fr.purpletear.friendzone4.factories.Data;

class PoetryModel {
    /**
     * Contains the chapter's code
     */
    private String chapterCode;

    private RequestManager rm;

    private boolean isFirstStart;

    PoetryModel(String chapterCode, RequestManager rm) {
        this.chapterCode = chapterCode;
        this.isFirstStart = true;
        this.rm = rm;
    }

    final String getChapterCode() {
        return chapterCode;
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
     * @return int
     */
    String getBackgroundResourceId(Context context) {
        switch (chapterCode) {
            case "7a":
                return Data.getImage(context, "fz4_poetry_7a");
            case "7b":
                return Data.getImage(context, "fz4_poetry_7b");
            case "8a":
                return Data.getImage(context, "fz4_poetry_8a");
            case "8b":
                return Data.getImage(context, "fz4_poetry_8b");
            case "9a":
                return Data.getImage(context, "fz4_poetry_9a");
            case "9c":
                return Data.getImage(context, "fz4_poetry_9b");
        }
        throw new IllegalStateException();
    }

    /**
     * Determines the text for a chaptercode
     * @return int
     */
    String getText(Context c) {
        switch (chapterCode) {
            case "7a":
                return c.getString(R.string.fz4_poetry_7a);
            case "7b":
                return c.getString(R.string.fz4_poetry_7b);
            case "8a":
                return c.getString(R.string.fz4_poetry_8a);
            case "8b":
                return c.getString(R.string.fz4_poetry_8b);
            case "9a":
                return c.getString(R.string.fz4_poetry_9a);
            case "9c":
                return c.getString(R.string.fz4_poetry_9c);
        }
        throw new IllegalStateException();
    }
}
