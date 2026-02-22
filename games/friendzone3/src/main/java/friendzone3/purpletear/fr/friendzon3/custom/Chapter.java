package friendzone3.purpletear.fr.friendzon3.custom;

import android.app.Activity;
import android.content.Context;

import com.example.sharedelements.OnlineAssetsManager;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import friendzone3.purpletear.fr.friendzon3.tables.TableOfChapters;
import purpletear.fr.purpleteartools.GlobalData;

public class Chapter implements Serializable {
    // The title of the chapter
    private String title;

    // The description of the chapter
    private String description;

    private String profilPicture;

    private String startingTitle;

    private String startingStatus;

    // The number of the chapter
    private int number;

    // Chapter code. Example : 6a
    private String code;

    /**
     * Determines the alpha code of the alternative of the chapter
     * exemple :
     * - Chapter 5 alternative a
     * - Chapter 5 alternative b
     */
    private String alternative;

    /**
     * All the parts of the chapter.
     * The use of that is simply reducing the complexity of the
     * Chapter generating
     */
    Discussion discussion;

    public Chapter(){
        discussion = new Discussion();
    }

    public Chapter(String code, String title, String description, String profilPicture, String startingTitle, String startingStatus) {
        this.code = code;
        this.title = title;
        this.description = description;
        this.number = nbChapter(code);
        this.alternative = alphaAlternative(code);
        this.profilPicture = profilPicture;
        this.startingTitle = startingTitle;
        this.startingStatus = startingStatus;
    }

    public boolean hasProfilPicture(Context context) {
        return !profilPicture.equals(OnlineAssetsManager.INSTANCE.getImageFilePath(context, String.valueOf(GlobalData.Game.FRIENDZONE3.getId()), ""))
                && !profilPicture.equals("");
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public String getProfilPicture() {
        return profilPicture;
    }

    public String getStartingTitle() {
        return startingTitle;
    }

    public String getStartingStatus() {
        return startingStatus;
    }

    public String getAlternative() {
        return alternative;
    }

    public int getNumber() {
        return number;
    }

    public String getCode(){
        return code;
    }

    private void setCode(final String code){
        this.code = code;
        this.alternative = Chapter.alphaAlternative(code);
        this.number = nbChapter(code);
    }

    /**
     * Reads a chapter and save it into the chapter object
     */
    public void read(final Activity activity) {
        try {
            discussion.symbols.read(activity);
            String version = discussion.symbols.getStoryVersion(GlobalData.Game.FRIENDZONE4.getId());
            assert version != null;
            discussion.links.read(activity, discussion.symbols.getChapterCode(), version);
            discussion.phrases.read(activity, discussion.symbols.getChapterCode(), version);
            discussion.characters.read(activity, discussion.symbols.getChapterCode());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Chapter chapter = TableOfChapters.INSTANCE.get(activity, discussion.symbols.getChapterCode());
        this.title = chapter.title;
        this.description = chapter.description;
    }

    /**
     * Returns the chapter first Phrase
     * @see Phrase
     * @return the root of the chapter (a Phrase)
     */
    public Phrase getRoot() {
        return discussion.getRoot();
    }

    public Discussion getDiscussion() {
        return discussion;
    }

    /**
     * Copies a chapter into the main one.
     */
    private boolean copy(final Chapter chapter){
        if(chapter == null)
            return false;
        title = chapter.title;
        description = chapter.description;

        if(chapter.getCode() != null)
            setCode(chapter.getCode());
        else {
            number = chapter.number;
            alternative = "a";
        }
        return true;
    }

    /**
     * Determines the n° of the chapter from the chapter code,
     * Example : return 6 from 6a
     * @param str the chapter code
     * @return the number of the chapter
     */
    public static int nbChapter(final String str){
        if(str.length() < 2)
            return 1;
        return Integer.parseInt(str.substring(0, str.length() - 1));
    }

    /**
     * Determines the alpha of the chapter's alternative from the chapter code,
     * Example : return a from 6a
     * @param code the chapter code
     * @return the alpha of the chapter's alternative
     */
    public static String alphaAlternative(final String code){
        if(code == null || code.length() < 2)
            return "a";
        return code.substring(code.length() - 1, code.length()).toLowerCase();
    }


    /**
     * Determines if the chapterCode is the one of the first chapter of the story.
     * @param chapterCode the code of the chapter.
     * @return true if it is.
     */
    public static boolean isFirst(String chapterCode){
        return Chapter.nbChapter(chapterCode) == 1;
    }

    /**
     * Determines if the chapterCode is the end of the story.
     * @param chapterCode the code of the chapter.
     * @return true if it is.
     */
    public static boolean isEnd(String chapterCode){
        ArrayList<String> endings = new ArrayList<String>() {{
           add("18a");
        }};
        return endings.contains(chapterCode);
    }

    @Override
    public String toString() {
        return "Chapter{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", numero=" + number +
                "}";
    }
}
