/*
 * Copyright (C) PurpleTear, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Hocine Belbouab <hbdeveloppeur@gmail.com>, 1/10/19 11:38 AM
 *
 */

package com.purpletear.smsgame.activities.smsgame.objects;

import android.os.Parcel;
import android.os.Parcelable;

public class Var implements Parcelable {
    public static final Creator<Var> CREATOR = new Creator<Var>() {
        @Override
        public Var createFromParcel(Parcel in) {
            return new Var(in);
        }

        @Override
        public Var[] newArray(int size) {
            return new Var[size];
        }
    };
    private String name;
    private String value;
    private String chapterCode;
    private int storyId;

    public Var(String name, String value, String chapterCode, int storyId) {
        this.name = name;
        this.value = value;
        this.chapterCode = chapterCode;
        this.storyId = storyId;
    }

    protected Var(Parcel in) {
        this.name = in.readString();
        this.value = in.readString();
        this.chapterCode = in.readString();
        this.storyId = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(value);
        dest.writeString(chapterCode);
        dest.writeInt(storyId);

    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof Var))
            return false;
        final Var v = (Var) obj;
        return v.getName().equals(getName()) && v.getValue().equals(getValue());
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public int getStoryId() {
        return storyId;
    }

    @Override
    public String toString() {
        return "Var :(" + name + ";" + value + ";" + storyId + "; " + chapterCode + "})";
    }
}