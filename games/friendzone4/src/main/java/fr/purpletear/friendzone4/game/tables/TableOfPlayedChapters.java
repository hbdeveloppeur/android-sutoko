package fr.purpletear.friendzone4.game.tables;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import fr.purpletear.friendzone4.factories.Std;
import fr.purpletear.friendzone4.game.config.Chapter;

public class TableOfPlayedChapters implements Parcelable {
    private ArrayList<String> playedChapters;

    public TableOfPlayedChapters() {
        playedChapters = new ArrayList<>();
        playedChapters.add("1a");
    }

    public void push(String code) {
        int number = Chapter.nbChapter(code);
        for(String c : playedChapters) {
            int n = Chapter.nbChapter(c);
            if(n == number) {
                return;
            }
        }
        playedChapters.add(code);
    }

    private void reset() {
        playedChapters = new ArrayList<>();
    }

    public String getChapterCode(int number) {
        if(number == 1) {
            return "1a";
        }
        for(String c : playedChapters) {
            int n = Chapter.nbChapter(c);
            if(n == number) {
                return c;
            }
        }
        return "1a";
    }

    public void removeFrom(int number) {
        playedChapters =  new ArrayList<>(playedChapters.subList(0, number));
    }

    public void describe() {
        StringBuilder sb = new StringBuilder("{");
        for (String code : playedChapters) {
            sb.append(code);
            sb.append(", ");
        }
        sb.append("}");
        Std.debug(sb.toString());
    }

    protected TableOfPlayedChapters(Parcel in) {
        in.readList(this.playedChapters = new ArrayList<>(), String.class.getClassLoader());
    }

    public static final Creator<TableOfPlayedChapters> CREATOR = new Creator<TableOfPlayedChapters>() {
        @Override
        public TableOfPlayedChapters createFromParcel(Parcel in) {
            return new TableOfPlayedChapters(in);
        }

        @Override
        public TableOfPlayedChapters[] newArray(int size) {
            return new TableOfPlayedChapters[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(playedChapters);

    }
}
