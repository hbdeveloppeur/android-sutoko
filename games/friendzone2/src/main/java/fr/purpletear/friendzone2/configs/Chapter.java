/*
 * Copyright (C) PurpleTear, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Hocine Belbouab <hbdeveloppeur@gmail.com>, 1/10/19 11:38 AM
 *
 */

package fr.purpletear.friendzone2.configs;


import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import fr.purpletear.friendzone2.tables.Character;
import fr.purpletear.friendzone2.R;


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


    private boolean isConversation;

    /**
     * Contains the conversationImageName
     */
    private String imageId;

    

    public Chapter() {
        code = "1a";
        title = "Aucun titre trouvé";
        description = "Aucune description trouvée";
        startingConversationName = "None";
        startingConversationStatus = "None";
        imageId = "";
    }

    public Chapter(String code, String title, String description, String startingConversationName, String startingConversationStatus, boolean isConversation, String imageId) {
        this.code = code;
        this.title = title;
        this.description = description;
        this.startingConversationName = startingConversationName;
        this.startingConversationStatus = startingConversationStatus;
        this.isConversation = isConversation;
        this.imageId = imageId;
    }

    @SuppressWarnings("WeakerAccess")
    protected Chapter(Parcel in) {
        code = in.readString();
        title = in.readString();
        description = in.readString();
        startingConversationName = in.readString();
        startingConversationStatus = in.readString();
        imageId = in.readString();
        isConversation = in.readByte() == 1;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(code);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(startingConversationName);
        dest.writeString(startingConversationStatus);
        dest.writeString(imageId);
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
        return Character.Companion.updateNames(c, title);
    }

    /**
     * Returns the chapter's description
     *
     * @return String
     */
    public String getDescription(Context c) {
        return Character.Companion.updateNames(c, description);
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
     * Returns the image's name
     *
     * @return String
     */
    public String getImage() {
        return imageId;
    }

    /**
     * Returns the chapter's conversation name
     *
     * @return String
     */
    public String getStartingConversationName(Context c) {
        return Character.Companion.updateNames(c, startingConversationName);
    }

    /**
     * Returns the chapter's conversation status
     *
     * @return String
     */
    public String getStartingConversationStatus(Context c) {
        return Character.Companion.updateNames(c, startingConversationStatus);
    }

    /**
     * Returns the chapter's alternative
     *
     * @return String
     */
    public String getAlternative() {
        return alphaAlternative(code);
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
