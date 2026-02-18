package fr.purpletear.friendzone4.game.config;

import android.os.Parcel;
import android.os.Parcelable;

import fr.purpletear.friendzone4.factories.Std;


public class AverageTime implements Parcelable {
    private int nb;
    private int currentAverageTime;

    public enum Type {
        MS,
        SEC
    }

    public AverageTime() {
        this.nb = 0;
        this.currentAverageTime = 0;
    }

    public void update(int time) {
        nb++;
        currentAverageTime = (currentAverageTime * (nb -1) + time) / nb;
        Std.debug("#", currentAverageTime);
    }

    int getAverage(Type type) {
        switch (type) {
            case MS:
                return currentAverageTime;
            case SEC:
                return currentAverageTime / 1000;
        }
        throw new IllegalStateException();
    }

    protected AverageTime(Parcel in) {
        nb = in.readInt();
        currentAverageTime = in.readInt();
    }

    public static final Creator<AverageTime> CREATOR = new Creator<AverageTime>() {
        @Override
        public AverageTime createFromParcel(Parcel in) {
            return new AverageTime(in);
        }

        @Override
        public AverageTime[] newArray(int size) {
            return new AverageTime[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(nb);
        dest.writeInt(currentAverageTime);
    }
}