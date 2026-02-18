package fr.purpletear.friendzone4.game.config;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;

import fr.purpletear.friendzone4.game.tables.TableOfPlayedChapters;

public class Params implements Serializable, Parcelable {

    /**
     * Contains the persistent states of the sound.
     * The user should be able to turn it into true or false as it want.
     */
    private Boolean stateSound;


    /**
     * Contains the user game's current chapter code
     */
    private String chapterCode;

    /**
     * Contains the average time of response for the user
     */
    private AverageTime averageTime;

    private TableOfPlayedChapters playedChapters;


    public Params() {
        this.chapterCode = "1a";
        this.playedChapters = new TableOfPlayedChapters();
        this.averageTime = new AverageTime();
    }

    public Params(String chapterCode) {
        this.chapterCode = chapterCode;
        this.stateSound = true;
        this.playedChapters = new TableOfPlayedChapters();
        this.averageTime = new AverageTime();
    }

    protected Params(Parcel in) {
        byte tmpStateSound = in.readByte();
        stateSound = tmpStateSound == 1;
        chapterCode = in.readString();
        averageTime = in.readParcelable(AverageTime.class.getClassLoader());
        playedChapters = in.readParcelable(TableOfPlayedChapters.class.getClassLoader());
    }

    public void removePlayedChapterFrom(int number) {
        this.playedChapters.removeFrom(number);
    }

    public static final Creator<Params> CREATOR = new Creator<Params>() {
        @Override
        public Params createFromParcel(Parcel in) {
            return new Params(in);
        }

        @Override
        public Params[] newArray(int size) {
            return new Params[size];
        }
    };

    /**
     * Saves the params.
     *
     * @param c
     */
    public void save(final Context c) {
        File dir = new File(c.getFilesDir(), getDir());
        File file = new File(dir, "params.json");
        try {
            if (!file.exists() && !dir.mkdirs() && !file.createNewFile()) {
                Log.e("Console", "Couldn't create file params.json");
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Gson gson = new Gson();
            FileOutputStream os = new FileOutputStream(file, false);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(os);
            outputStreamWriter.write(gson.toJson(this));
            outputStreamWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Read params.json and copies it to the this params
     *
     * @param c the context to use
     */
    public void read(Context c) {
        File dir = new File(c.getFilesDir(), getDir());
        File file = new File(dir, "params.json");
        if (!file.exists())
            return;

        Gson gson = new Gson();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (br == null) {
            Toast.makeText(c.getApplicationContext(), "Vous devez activer les permissions de stockage dans vos paramètres", Toast.LENGTH_LONG).show();
            return;
            // throw new NullPointerException("Could'nt read file. Please make sure that the required permissions are set.");
        }

        Params p = gson.fromJson(br, Params.class);
        this.copy(p);
    }

    public void updateAverageTime(int time) {
        this.averageTime.update(time);
    }

    public int getAverage(AverageTime.Type type) {
        return this.averageTime.getAverage(type);
    }

    /**
     * Returns the Params' dir
     *
     * @return String
     */
    private String getDir() {
        return "game" + File.separator;
    }

    /**
     * Copies a given Param into the current one
     *
     * @param params Params
     */
    private void copy(Params params) {
        stateSound = params.stateSound;
        chapterCode = params.chapterCode;
        averageTime = params.averageTime;
        playedChapters = params.playedChapters;
    }

    /**
     * Resets the game back up
     *
     * @param context Context
     */
    public void reset(Context context) {
        this.copy(new Params());
        save(context);
    }

    /**
     * Determines if the given user's name is valid or not
     *
     * @param name String
     * @return boolean
     */
    public static boolean isValidName(String name) {
        return name.equals(Params.getDefaultName()) || (name.matches("([a-zA-ZäöüßÄËÖÜẞÁÉÍÓÚáàèëùïéíóú\\- ]+)")
                && Params.formatName(name).length() >= 3) || formatName(name).length() == 0;
    }

    /**
     * Formats the user's name
     *
     * @param name String
     * @return String
     */
    private static String formatName(String name) {
        StringBuilder sb = new StringBuilder();
        boolean sawSpace = false;
        final int size = name.length();
        if (size < 3) {
            return name;
        }
        for (int i = 0; i < size; i++) {

            if (name.charAt(i) == ' ' && sawSpace) {
                continue;
            } else if (i == name.length() - 1 && name.charAt(i) == ' ') {
                break;
            } else sawSpace = name.charAt(i) == ' ';
            if (i == 0) {
                sb.append(Character.toUpperCase(name.charAt(i)));
            } else {
                sb.append(name.charAt(i));
            }

        }
        return sb.toString();
    }

    /**
     * Updates the chapter's code
     *
     * @param chapterCode String
     */
    public void setChapterCode(String chapterCode) {

        if(chapterCode.replace(" ", "").toLowerCase().equals("a0")) {
            playedChapters.push("10A");
            this.chapterCode = "10A";
            return ;
        }
        playedChapters.push(chapterCode);
        this.chapterCode = chapterCode;
    }

    public void empty() {
        playedChapters.removeFrom(1);
    }

    /**
     * Returns the chapter code
     *
     * @return String
     * @see Chapter
     */
    public String getChapterCode() {
        if(chapterCode.replace(" ", "").toLowerCase().equals("a0")) {
            return "10A";
        }
        return chapterCode.toLowerCase();
    }

    /**
     * Returns the chapter number
     * @return int
     */
    public int getChapterNumber() {
        if(chapterCode.replace(" ", "").toLowerCase().equals("a0")) {
            return Chapter.nbChapter("10A");
        }
        return Chapter.nbChapter(chapterCode);
    }

    public TableOfPlayedChapters playedChapters() {
        return  playedChapters;
    }

    /**
     * Returns the default user name
     *
     * @return String
     */
    private static String getDefaultName() {
        return "Nick";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (stateSound == null ? 0 : stateSound ? 1 : 2));
        dest.writeString(chapterCode);
        dest.writeParcelable(averageTime, flags);
        dest.writeParcelable(playedChapters, flags);
    }

    @Override
    public String toString() {
        return "Params{" +
                "stateSound=" + stateSound +
                ", chapterCode='" + chapterCode + '\'' +
                '}';
    }
}
