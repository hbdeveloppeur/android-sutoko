package fr.purpletear.friendzone4.game.activities.audiocinematic;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import fr.purpletear.friendzone4.factories.Std;
import purpletear.fr.purpleteartools.GameLanguage;

class AudioDescriptionReader {

    Line currentLine;

    /**
     * Contains the lines.
     */
    private ArrayList<Line> array;

    AudioDescriptionReader() {
        currentLine = new Line(0, 0, "");
        this.array = new ArrayList<>();
    }

    void read(Context c, int chapter) {
        try {
            InputStream lines = c.getAssets().open("friendzone4_assets/audio_description/"+ GameLanguage.Companion.determineLangDirectory() +"/intro_chapter_" + chapter + ".json");
            BufferedReader br =
                    new BufferedReader(new InputStreamReader(lines, "UTF-8"));
            Gson gson = new Gson();
            array = gson.fromJson(br, new TypeToken<List<Line>>() {
            }.getType());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void requestLine(int currentTime, AudioCinematicListener listener) {
        boolean found = false;
        for(Line line : array) {
            if(line.start <= currentTime && currentTime <= line.end) {
                found = true;
                if(!currentLine.equals(line)) {
                    currentLine = line;
                    listener.onFoundLine(line.text);
                }
                break;
            }
        }
        if(!found && !currentLine.text.equals("")) {
            currentLine = new Line(0, 0, "");
            listener.onFoundLine("");
        }
    }

    void describe() {
        for (Line line : array) {
            Std.debug(line.toString());
        }
    }

    private class Line {
        int start;
        int end;
        String text;

        Line(int start, int end, String text) {
            this.start = start;
            this.end = end;
            this.text = text;
        }

        @NonNull
        @Override
        public String toString() {
            return "Line{" +
                    "start=" + start +
                    ", end=" + end +
                    ", text='" + text + '\'' +
                    '}';
        }
    }
}


