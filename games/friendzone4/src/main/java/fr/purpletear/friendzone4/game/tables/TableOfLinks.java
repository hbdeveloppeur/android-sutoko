package fr.purpletear.friendzone4.game.tables;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.sharedelements.SmsGameTreeStructure;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fr.purpletear.friendzone4.GameData;
import fr.purpletear.friendzone4.game.activities.main.Phrase;
import fr.purpletear.friendzone4.purpleTearTools.Std;
import purpletear.fr.purpleteartools.GameLanguage;
import purpletear.fr.purpleteartools.GlobalData;

public class TableOfLinks {

    /**
     * Contains the links between phrases
     */
    private ArrayList<Link> links = new ArrayList<>();

    /**
     * Adds a link to the list.
     *
     * @param srcId  int source's id
     * @param destId int dest's id
     * @see Link
     */
    public void append(int srcId, int destId) {
        links.add(new Link(srcId, destId));
    }

    /**
     * Returns the number of links.
     *
     * @return number of links
     */
    public int size() {
        return links.size();
    }

    /**
     * Returns an arrayList of the dest phrases id
     *
     * @param srcId source's id
     * @return ArrayList
     */
    public ArrayList<Integer> getDest(int srcId) {
        ArrayList<Integer> ids = new ArrayList<>();
        for (Link link : links) {
            if (link.getSrcId() == srcId) {
                ids.add(link.getDestId());
            }
        }
        return ids;
    }

    /**
     * Returns an arrayList of the dest phrases id
     *
     * @param srcId source's id
     * @return ArrayList
     */
    public ArrayList<Phrase> getDestPhrases(int srcId, TableOfPhrases tableOfPhrases) {
        ArrayList<Phrase> phrases = new ArrayList<>();
        for (Link link : links) {
            if (link.getSrcId() == srcId) {
                int id = link.getDestId();
                phrases.add(tableOfPhrases.getPhrase(id));
            }
        }
        return phrases;
    }

    /**
     * Determines if the source phrase has an answer (a next)
     *
     * @param srcId source's id
     * @return boolean
     */
    public boolean hasAnswer(int srcId) {
        for (Link link : links) {
            if (link.getSrcId() == srcId) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if the next Phrase is a choice of the user
     *
     * @param srcId          source's id
     * @param tableOfPhrases Table of phrases
     * @return boolean
     */
    public boolean answerIsUserChoice(int srcId, TableOfPhrases tableOfPhrases) {
        for (Link link : links) {
            if (link.getSrcId() == srcId
                    && tableOfPhrases.getPhrase(link.getDestId()).getId_author() == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Reads the chapter's param file
     *
     * @param c Context
     */
    public void read(Context c, String chapterCode, String storyVersion) {
        File file = SmsGameTreeStructure.Companion.getStoryLinksFile(
                c, String.valueOf(GlobalData.Game.FRIENDZONE4.getId()),
                chapterCode, GameLanguage.Companion.determineLangDirectory());

        if (!file.exists()) {
            throw new IllegalStateException("Couldn't find file "+file.getAbsolutePath()+" for story number ${GlobalData.Game.FRIENDZONE4.id}");
        }

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (br != null) {
            links = new Gson().fromJson(br, new TypeToken<List<Link>>() {}.getType());
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Describes the table
     */
    public void describe() {
        Std.debug("***** DESCRIBE TABLE OF LINKS *****");
        for (Link link : links) {
            Std.debug(link.toString());
        }
        Std.debug("***********************************");
    }


    /**
     * Returns the name of the file containing the links.
     *
     * @return String
     */
    private static String getChapterLinksFileName(String chapterCode) {
        return chapterCode + "_links.json";
    }

    /**
     * A Link represent the connection between two phrases
     */
    private class Link {
        /**
         * The id of the incoming phrase
         */
        private int src;

        /**
         * The id of the next phrase
         */
        private int dest;

        Link(int srcId, int destId) {
            this.src = srcId;
            this.dest = destId;
        }

        /**
         * The source id
         *
         * @return int
         */
        int getSrcId() {
            return src;
        }

        /**
         * The dest id
         *
         * @return int
         */
        int getDestId() {
            return dest;
        }

        @NonNull
        @Override
        public String toString() {
            return "Link{" +
                    "src=" + src +
                    ", dest=" + dest +
                    '}';
        }
    }
}


