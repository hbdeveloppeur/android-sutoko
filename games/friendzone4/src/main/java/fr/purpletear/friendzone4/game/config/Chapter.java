package fr.purpletear.friendzone4.game.config;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Parcel;
import android.os.Parcelable;

import fr.purpletear.friendzone4.GameData;
import purpletear.fr.purpleteartools.TableOfSymbols;

/**
 * Represents a Chapter. (3 fields)
 * A Chapter has a :
 * - code (<number><alternative>)
 * - title
 * - description
 */
public final class Chapter implements Parcelable {
    /**
     * Contains the chapter's code
     */
    private String code;

    /**
     * Contains the chapter's title
     */
    private String title;

    /**
     * Contains the condition
     */
    private String when;

    /**
     * Contains the chapter's description
     */
    private String description;

    /**
     * Contains the starting conversation name
     */
    private String startingConversationName;

    /**
     * Contains the starting conversation status
     */
    private String startingConversationStatus;

    /**
     * Does the current chapter has an cinematic?
     */
    private boolean hasCinematic;

    private boolean isConversation;

    /**
     * Contains the conversationImageName
     */
    private String image;

    public Chapter() {
        code = "1a";
        title = "Aucun titre trouvé";
        when = "default";
        description = "Aucune description trouvée";
        startingConversationName = "None";
        startingConversationStatus = "None";
        image = "";
    }

    public Chapter(String code, String when, String title, String description, boolean hasCinematic, String startingConversationName, String startingConversationStatus, String image, boolean isConversation) {
        this.code = code;
        this.title = title;
        this.when = when;
        this.description = description;
        this.startingConversationName = startingConversationName;
        this.startingConversationStatus = startingConversationStatus;
        this.hasCinematic = hasCinematic;
        this.isConversation = isConversation;
        this.image = image;
    }

    @SuppressWarnings("WeakerAccess")
    protected Chapter(Parcel in) {
        code = in.readString();
        title = in.readString();
        when = in.readString();
        description = in.readString();
        startingConversationName = in.readString();
        startingConversationStatus = in.readString();
        image = in.readString();
        hasCinematic = in.readByte() == 1;
        isConversation = in.readByte() == 1;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(code);
        dest.writeString(title);
        dest.writeString(when);
        dest.writeString(description);
        dest.writeString(startingConversationName);
        dest.writeString(startingConversationStatus);
        dest.writeString(image);
        dest.writeByte((byte) (hasCinematic ? 1 : 0));
        dest.writeByte((byte) (isConversation ? 1 : 0));
    }


    /**
     * Returns the chapter's code
     *
     * @return String
     */
    public String getCode() {
        return code;
    }

    /**
     * Returns the chapter's title
     *
     * @return String
     */
    public String getTitle(Context c) {
        return GameData.INSTANCE.updateNames(c, title);
    }

    /**
     * Returns the chapter's description
     *
     * @return String
     */
    public String getDescription(Context c) {
        return GameData.INSTANCE.updateNames(c, title);
    }

    /**
     * Returns the chapter's number
     *
     * @return int
     */
    public int getNumber() {
        return nbChapter(code);
    }

    /**
     * Returns the chapter's condition
     * @return int
     */
    public String getCondition() {
        return when;
    }

    /**
     * Returns the image's name
     *
     * @return String
     */
    public String getImage() {
        return image;
    }

    /**
     * Returns the chapter's conversation name
     *
     * @return String
     */
    public String getStartingConversationName(Context c) {
        return GameData.INSTANCE.updateNames(c, startingConversationName);
    }

    /**
     * Returns the chapter's conversation status
     *
     * @return String
     */
    public String getStartingConversationStatus(Context c) {
        return GameData.INSTANCE.updateNames(c, startingConversationStatus);
    }

    /**
     * Returns the chapter's alternative
     *
     * @return String
     */
    public String getAlternative() {
        return alphaAlternative(code);
    }

    /**
     * Does the chapter has a cinematic ?
     *
     * @return boolean
     */
    private boolean hasCinematic() {
        return hasCinematic;
    }

    public boolean isConversation() {
        return isConversation;
    }

    /**
     * Determines the n° of the chapter from the chapter code,
     * Example : return 6 from 6a
     *
     * @param code the chapter code
     * @return the number of the chapter
     */
    public static int nbChapter(final String code) {
        if (code == null || code.length() < 2)
            return 1;
        return Integer.parseInt(code.substring(0, code.length() - 1));
    }

    /**
     * Determines the alpha of the chapter's alternative from the chapter code,
     * Example : return a from 6a
     *
     * @param code the chapter code
     * @return the alpha of the chapter's alternative
     */
    public static String alphaAlternative(final String code) {
        if (code == null || code.length() < 2)
            return "a";
        return code.substring(code.length() - 1, code.length()).toLowerCase();
    }

    /**
     * Gets the chapter given the code
     *
     * @param code String
     * @see Chapter
     */
    public final void get(Context c, String code, TableOfSymbols symbols) {
        copy(ChapterDetailsHandler.getChapter(c, symbols, code));
    }

    /**
     * Copies the given chapter to the current one
     *
     * @param c Chapter
     */
    private void copy(Chapter c) {
        code = c.getCode();
        title = c.title;
        when = c.getCondition();
        description = c.description;
        hasCinematic = c.hasCinematic();
        startingConversationName = c.startingConversationName;
        startingConversationStatus = c.startingConversationStatus;
        image = c.getImage();
        isConversation = c.isConversation();
    }

    /**
     * Determines the chapter's filepath
     * The file that contains the content of the chapter.
     *
     * @return String
     */
    private String getChapterContentFilePath() {
        if (hasCinematic) {
            return "json/chapter_" + code + "_cinematic.json";
        }
        return "json/chapter_" + code + "_content.json";
    }

    @Override
    public int describeContents() {
        return 0;
    }
    public static final Creator<Chapter> CREATOR = new Creator<Chapter>() {
        @Override
        public Chapter createFromParcel(Parcel in) {
            return new Chapter(in);
        }

        @Override
        public Chapter[] newArray(int size) {
            return new Chapter[size];
        }
    };
}
