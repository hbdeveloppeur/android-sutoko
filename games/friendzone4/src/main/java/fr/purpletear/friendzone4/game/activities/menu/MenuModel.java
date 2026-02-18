package fr.purpletear.friendzone4.game.activities.menu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Spanned;
import android.widget.Toast;

import com.bumptech.glide.RequestManager;
import com.example.sutokosharedelements.SutokoSharedElementsData;

import fr.purpletear.friendzone4.R;
import fr.purpletear.friendzone4.factories.Std;
import fr.purpletear.friendzone4.game.config.ChapterDetailsHandler;
import fr.purpletear.friendzone4.game.config.Params;
import fr.purpletear.friendzone4.purpleTearTools.SoundHandler;
import purpletear.fr.purpleteartools.GlobalData;
import purpletear.fr.purpleteartools.TableOfSymbols;

class MenuModel {

    /**
     * Represents the first start Activity's state
     */
    private boolean isFirstStart;

    /**
     * Returns the Glide RequestManager
     *
     * @see RequestManager
     */
    private RequestManager rm;

    /**
     * Manages the sound.
     */
    private SoundHandler sh;

    /**
     * Contains the menu sound name
     */
    private final static String soundName = "bg_menu";
    /**
     * CurrentSoundPosition
     */
    private int currentSoundPosition;

    /**
     *
     */
    private int currentDisplayingChapterNumber;

    /**
     * Contains the currentSoundState
     */
    private boolean soundState;

    /**
     * Contains the game back up
     */
    private Params params;

    /**
     * Contains the game symbols
     */
    private TableOfSymbols symbols;

    /**
     * Represents the menu chapters navigation
     */
    public enum Navigation {
        PREVIOUS,
        NEXT
    }

    MenuModel(Activity c, RequestManager rm) {
        params = new Params();
        symbols = c.getIntent().getParcelableExtra("symbols");


        if (symbols == null) {
            symbols = new TableOfSymbols(GlobalData.Game.FRIENDZONE4.getId());
        }
        symbols.setGameId(GlobalData.Game.FRIENDZONE4.getId());
        load(c);

        currentDisplayingChapterNumber = getCurrentChapterNumber();
        currentSoundPosition = 0;
        soundState = true;
        isFirstStart = true;
        sh = new SoundHandler();
        this.rm = rm;
    }

    /**
     * Loads info
     */
    void load(Activity c) {
        params.read(c);
        symbols.read(c);

        //noinspection ConstantConditions
        if (SutokoSharedElementsData.INSTANCE.getSHOULD_FORCE_CHAPTER()) {
            params.setChapterCode(SutokoSharedElementsData.INSTANCE.getFORCE_CHAPTER_CODE());
            params.save(c);
        }
    }

    /**
     * Returns the Glide request Manager
     *
     * @return RequestManager
     * @see RequestManager
     */
    RequestManager getRequestManager() {
        return rm;
    }

    /**
     * Starts or stops the sound
     *
     * @param a         Activity
     * @param isPlaying boolean
     */
    void soundState(Activity a, boolean isPlaying) {
        if (isPlaying && !soundState) {
            return;
        }
        if (isPlaying && !sh.exist(soundName)) {
            sh.generate(soundName, a, false);
        }
        if (isPlaying) {
            sh.play(soundName, currentSoundPosition);
        } else {
            currentSoundPosition = sh.getCurrentPosition(soundName);
            sh.pause(soundName);
        }
    }

    /**
     * Returns the info content
     *
     * @param a Activity
     * @return Spanned
     */
    Spanned getInfoContent(Activity a) {
        return Std.getTextFromHtmlFile("info", a);
    }

    /**
     * Returns the current chapter title
     *
     * @return String
     */
    String getCurrentChapterTitle(Context c) {
        return ChapterDetailsHandler.getChapter(c, symbols, getCurrentDisplayingChapterCode()).getTitle(c);
    }

    /**
     * Returns params
     *
     * @return Params
     */
    Params getParams() {
        return params;
    }

    /**
     * Erases data from the current displaying chapter number
     *
     * @param c Context
     */
    void eraseToNavigatingChapter(Activity c) {
        symbols.setChapterCode(getCurrentDisplayingChapterCode());
        params.setChapterCode(getCurrentDisplayingChapterCode());
        params.removePlayedChapterFrom(currentDisplayingChapterNumber);
        params.save(c);
        symbols.removeFromASpecificChapterNumber(GlobalData.Game.FRIENDZONE4.getId(), currentDisplayingChapterNumber);
        symbols.save(c);
    }

    /**
     * Returns symbols
     *
     * @return TableOfSymbols
     */
    TableOfSymbols getSymbols() {
        return symbols;
    }

    /**
     * Returns the current displaying chapter code
     *
     * @return String
     */
    private String getCurrentDisplayingChapterCode() {
        return params.playedChapters().getChapterCode(currentDisplayingChapterNumber);
    }

    /**
     * Returns the current chapter number
     *
     * @return int
     */
    private int getCurrentChapterNumber() {
        return params.getChapterNumber();
    }

    /**
     * Returns the current displaying chapter number
     *
     * @return int
     */
    int getCurrentDisplayingChapterNumber() {
        return currentDisplayingChapterNumber;
    }

    /**
     * Resets the currentDisplayingChapterNumber
     */
    void resetCurrentDisplayingChapterNumber() {
        currentDisplayingChapterNumber = params.getChapterNumber();
    }

    /**
     * Opens the outlet web page
     *
     * @param a Activity
     */
    void openOutlet(Activity a) {
        String url = a.getString(R.string.store_link);
        Intent i = new Intent(Intent.ACTION_VIEW);
        if (i.resolveActivity(a.getPackageManager()) == null) {
            Toast.makeText(a.getApplicationContext(), "Aucun navigateur web par défaut trouvé", Toast.LENGTH_LONG).show();
            return;
        }
        i.setData(Uri.parse(url));
        a.startActivity(i);
    }

    /**
     * Sets the current soundState
     *
     * @param isSoundEnabled boolean
     */
    void setSoundState(boolean isSoundEnabled) {
        soundState = isSoundEnabled;
    }

    /**
     * Returns the soundState
     *
     * @return boolean
     */
    boolean getSoundState() {
        return soundState;
    }

    /**
     * Returns the ChapterNumber
     *
     * @return int
     */
    int getPreviousChapterNumber() {
        return currentDisplayingChapterNumber - 1;
    }

    /**
     * Returns the next ChapterNumber
     *
     * @return int
     */
    int getNextChapterNumber() {
        return currentDisplayingChapterNumber + 1;
    }

    /**
     * Determines if the current chapter is the end
     *
     * @return boolean
     */
    private boolean isEnd() {
        return params.getChapterNumber() == 11 || params.getChapterCode().toLowerCase().equals("10c");
    }

    /**
     * Determines if it should display the navigation button
     *
     * @param n Navigation
     * @return boolean
     */
    boolean shouldDisplayButton(Navigation n) {
        switch (n) {
            case PREVIOUS:
                return currentDisplayingChapterNumber > 1 && isEnd();
            case NEXT:
                Std.debug(getCurrentChapterNumber());
                Std.debug(params.getChapterCode());
                return currentDisplayingChapterNumber < getCurrentChapterNumber() && isEnd();
        }
        throw new IllegalStateException();
    }

    /**
     * Updates the currendisplayingchapternumber
     *
     * @param n Navigation
     */
    void navigate(Navigation n) {
        switch (n) {
            case PREVIOUS: {
                currentDisplayingChapterNumber--;
                break;
            }
            case NEXT: {
                currentDisplayingChapterNumber++;
                break;
            }
            default:
                throw new IllegalStateException();
        }
    }

    /**
     * Determines the first start Activity's state
     *
     * @return boolean
     */
    boolean isFirstStart() {
        boolean v = isFirstStart;
        isFirstStart = false;
        return v;
    }

    /**
     * Resets.
     *
     * @param c Context
     */
    void reset(Activity c) {
        params.reset(c);
        symbols.reset(c, GlobalData.Game.FRIENDZONE4.getId());
        currentDisplayingChapterNumber = 1;
    }

    /**
     * Determines if the user is navigating
     *
     * @return boolean
     */
    boolean userIsNavigating() {
        return currentDisplayingChapterNumber < params.getChapterNumber();
    }
}
