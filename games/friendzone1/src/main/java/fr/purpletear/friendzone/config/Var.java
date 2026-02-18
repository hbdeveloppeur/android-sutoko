/*
 * Copyright (C) PurpleTear, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Hocine Belbouab <hbdeveloppeur@gmail.com>, 1/10/19 11:38 AM
 *
 */

package fr.purpletear.friendzone.config;

import android.os.Parcel;
import android.os.Parcelable;

public class Var implements Parcelable {
    private String name;
    private String value;
    private int chapterNumber;

    public Var(String name, String value, int chapterNumber) {
        this.name = name;
        this.value = value;
        this.chapterNumber = chapterNumber;
    }



    protected Var(Parcel in) {
        this.name = in.readString();
        this.value = in.readString();
        this.chapterNumber = in.readInt();
    }

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(value);
        dest.writeInt(chapterNumber);

    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this)
            return true;
        if(!(obj instanceof Var))
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

    public int getChapterNumber() {
        return chapterNumber;
    }

    @Override
    public String toString() {
        return "Var :("+name+";"+value+";"+String.valueOf(chapterNumber)+")";
    }
}