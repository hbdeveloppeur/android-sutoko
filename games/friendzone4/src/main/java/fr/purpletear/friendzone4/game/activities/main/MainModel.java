package fr.purpletear.friendzone4.game.activities.main;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Parcelable;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.example.sharedelements.SutokoSharedElementsData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import fr.purpletear.friendzone4.R;
import fr.purpletear.friendzone4.factories.Data;
import fr.purpletear.friendzone4.factories.Std;
import fr.purpletear.friendzone4.game.adapters.GameContactAdapter;
import fr.purpletear.friendzone4.game.adapters.GameConversationAdapter;
import fr.purpletear.friendzone4.game.config.AverageTime;
import fr.purpletear.friendzone4.game.config.Chapter;
import fr.purpletear.friendzone4.game.config.Params;
import fr.purpletear.friendzone4.game.tables.TableOfCharacters;
import fr.purpletear.friendzone4.game.tables.TableOfLinks;
import fr.purpletear.friendzone4.game.tables.TableOfPhrases;
import fr.purpletear.friendzone4.purpleTearTools.Finger;
import fr.purpletear.friendzone4.purpleTearTools.MemoryHandler;
import fr.purpletear.friendzone4.purpleTearTools.SoundHandler;
import purpletear.fr.purpleteartools.GlobalData;
import purpletear.fr.purpleteartools.TableOfSymbols;

/**
 * Handles the data and controllers settings
 */
class MainModel {

    private GameConversationAdapter adapter;

    /**
     * Contains the first start state value
     */
    private boolean isFirstStart;

    /**
     * Contains the chapter details
     *
     * @see Chapter
     */
    private Chapter chapter;

    /**
     * Contains the Activity's Glide RequestManager
     *
     * @see RequestManager
     */
    private RequestManager rm;

    /**
     * Contains the currentPhrase in the discussion
     */
    private Phrase currentPhrase;

    /**
     * Represents the Phrases table structure
     *
     * @see TableOfPhrases
     */
    private TableOfPhrases tableOfPhrases;

    /**
     * Represents the Links table structure
     *
     * @see TableOfLinks
     */
    private TableOfLinks tableOfLinks;

    /**
     * Represents the data structure of the symbols
     *
     * @see TableOfSymbols
     */
    private TableOfSymbols tableOfSymbols;

    /**
     * Represents the sound handling structure.
     *
     * @see SoundHandler
     */
    private SoundHandler sh;

    /**
     * Boolean determines if the Activity has a backgroundMedia
     */
    private Boolean hasBackgroundMedia;

    /**
     * String determines the current video to reload.
     */
    private String videoToReload;

    /**
     * Handles the delay Handlers
     *
     * @see MemoryHandler
     */
    MemoryHandler mh;

    /**
     * Represents the game state
     */
    enum GameState {
        DESCRIPTION,
        PAUSED,
        USER_PAUSED,
        PLAYING,
        WAITING_FOR_USER
    }

    /**
     * Contains the current GameState value
     *
     * @see GameState
     */
    private GameState currentGameState;

    /**
     * Contains the table of characters structure
     *
     * @see TableOfCharacters
     */
    private TableOfCharacters tableOfCharacters;

    /**
     * Contains the backup data structure
     *
     * @see Params
     */
    private Params params;

    /**
     * Contains the timed choice seconds value
     */
    private int timedChoice;

    /**
     * Contains the TimedChoice progressBar ValueAnimator
     */
    private ValueAnimator timedChoiceProgressBarValueAnimator;

    /**
     * @param activity         Activity
     * @param am        AssetManager
     * @param glide     RequestManager
     * @param callbacks MainInterface
     */
    MainModel(Activity activity, AssetManager am, Parcelable parcelableParams, Parcelable parcelableSymbols, RequestManager glide, MainInterface callbacks) {
        timedChoice = 0;
        hasBackgroundMedia = false;
        params = (Params) parcelableParams;
        sh = new SoundHandler();
        isFirstStart = true;
        mh = new MemoryHandler();
        currentGameState = GameState.DESCRIPTION;
        tableOfPhrases = new TableOfPhrases();
        tableOfLinks = new TableOfLinks();
        tableOfCharacters = new TableOfCharacters();
        tableOfCharacters.load(params.getChapterCode());
        tableOfSymbols = (TableOfSymbols) parcelableSymbols;

        RequestManager r = Glide.with(activity);
        rm = glide;
        chapter = new Chapter();
        chapter.get(activity, params.getChapterCode(), tableOfSymbols);
        adapter = new GameConversationAdapter(activity, r, callbacks, tableOfCharacters, params.getChapterCode(), hasBackgroundMedia, isNoSeen());
        tableOfSymbols.removeFromASpecificChapterNumber(GlobalData.Game.FRIENDZONE4.getId(), chapter.getNumber());
        try {
            String version = tableOfSymbols.getStoryVersion(GlobalData.Game.FRIENDZONE4.getId());
            assert version != null;
            tableOfPhrases.read(activity, params.getChapterCode(), version);
            tableOfLinks.read(activity, params.getChapterCode(), version);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(tableOfLinks.getDest(SutokoSharedElementsData.INSTANCE.getSTARTING_PHRASE_ID()).size() > 0) {
                currentPhrase = tableOfPhrases.getPhrase(tableOfLinks.getDest(SutokoSharedElementsData.INSTANCE.getSTARTING_PHRASE_ID()).get(0));
            } else {
                currentPhrase = new Phrase(Phrase.Type.me);
            }

        }
    }

    /**
     * Contains the first start state value
     *
     * @return boolean
     */
    boolean isFirstStart() {
        boolean value = isFirstStart;
        isFirstStart = false;
        return value;
    }

    /**
     * Returns the RequestManager
     *
     * @return RequestManager
     * @see RequestManager
     */
    RequestManager getRequestManager() {
        return rm;
    }

    /**
     * Returns the associated resource identifier. Returns 0 if no such resource was found. (0 is not a valid resource ID.)
     *
     * @param c Context
     * @return int
     */
    String getProfilPictureResourceId(Context c) {
        if (chapter.getImage().equals("transparent")) {
            return "";
        }
        String id = Data.getImage(c, "fz4_" + chapter.getImage());
        if ("".equals(id)) {
            throw new Resources.NotFoundException("Resource " + chapter.getImage() + " not found.");
        }
        return id;
    }

    /**
     * Returns the formated version of the chapter's title
     * format : R.string.title_format
     *
     * @param c Context
     * @return String
     */
    String getFormatedChapterTitle(Context c) {
        return c.getString(R.string.title_format, chapter.getNumber(), chapter.getTitle(c));
    }

    /**
     * Returns the chapter's description
     *
     * @return String
     * @see Chapter
     */
    String getChapterDescription(Context c) {
        return chapter.getDescription(c);
    }

    /**
     * Returns the chapter conversation's name
     *
     * @return String
     */
    String getChapterConversationName(Context c) {
        return chapter.getStartingConversationName(c);
    }

    /**
     * Returns the chapter conversation's status
     *
     * @return String
     */
    String getChapterConversationStatus(Context c) {
        return chapter.getStartingConversationStatus(c);
    }

    /**
     * Returns the current GameState
     *
     * @return GameState
     * @see GameState
     */
    GameState getCurrentGameState() {
        return currentGameState;
    }

    /**
     * Returns the current chapter number
     *
     * @return int
     * @see Chapter
     */
    int getChapterNumber() {
        return chapter.getNumber();
    }

    /**
     * Sets the value of the current Game State
     *
     * @param gs GameState
     */
    void setCurrentGameState(GameState gs) {
        currentGameState = gs;
    }

    Phrase getCurrentPhrase() {
        return currentPhrase;
    }

    void moveForward() {
        currentPhrase = getTableOfLinks().getDestPhrases(getCurrentPhrase().getId(), getTableOfPhrases()).get(0);
    }

    /**
     * Returns the game conversation adapter
     *
     * @return GameConversationAdapter
     */
    GameConversationAdapter getAdapter() {
        return adapter;
    }

    GameContactAdapter getContactAdapter(Context c, MainInterface callBack) {
        ArrayList<Contact> array = new ArrayList<>();
        array.add(new Contact(c,"3a11", "fz4_zoe", c.getString(R.string.fz4_contact_list_action_1), c.getString(R.string.fz4_contact_list_action_2), Contact.Type.ACTION, false));
        array.add(new Contact(c,"3a12", "fz4_eva", c.getString(R.string.fz4_contact_list_action_3), c.getString(R.string.fz4_contact_list_action_4), Contact.Type.ACTION, false));
        array.add(new Contact(c,"fz4_lucie", c.getString(R.string.fz4_contact_list_action_5), c.getString(R.string.fz4_contact_list_action_6), Contact.Type.SIMPLE, true));
        array.add(new Contact(c,"fz4_jhon", "Jhon", c.getString(R.string.fz4_contact_list_action_7), Contact.Type.SIMPLE, true));
        array.add(new Contact(c,"fz4_marine", "Marine", c.getString(R.string.fz4_contact_list_action_8), Contact.Type.SIMPLE, true));
        array.add(new Contact(c,"fz4_toshi", "Toshi", c.getString(R.string.fz4_contact_list_action_9), Contact.Type.SIMPLE, true));
        array.add(new Contact(c,"fz4_mike", "Mike", c.getString(R.string.fz4_contact_list_action_10), Contact.Type.SIMPLE, true));
        array.add(new Contact(c,"fz4_heika", "Heika", "恐れ", Contact.Type.SIMPLE, true));
        return new GameContactAdapter(c, rm, array, callBack);
    }

    /**
     * Returns the table of links
     *
     * @return TableOfLinks
     */
    TableOfLinks getTableOfLinks() {
        return tableOfLinks;
    }

    /**
     * Returns the table of Phrases
     *
     * @return TableOfPhrases
     */
    TableOfPhrases getTableOfPhrases() {
        return tableOfPhrases;
    }

    /**
     * Returns the table of characters
     *
     * @return TableOfCharacters
     * @see TableOfCharacters
     */
    TableOfCharacters getTableOfCharacters() {
        return tableOfCharacters;
    }

    /**
     * Returns the table of symbols
     *
     * @return TableOfSymbols
     * @see TableOfSymbols
     */
    TableOfSymbols getTableOfSymbols() {
        return tableOfSymbols;
    }

    /**
     * Sets the current Phrase
     *
     * @param phrase Phrase
     */
    void setCurrentPhrase(Phrase phrase) {
        currentPhrase = phrase;
    }

    /**
     * Updates the table of symbols
     *
     * @param tableOfSymbols TableOfSymbols
     */
    void setTableOfSymbols(TableOfSymbols tableOfSymbols) {
        this.tableOfSymbols = tableOfSymbols;
    }

    /**
     * Clears the required resources
     */
    void clear() {
        mh.kill();
        clearTimedChoice();
    }

    void clearTimedChoice() {
        if (timedChoiceProgressBarValueAnimator == null) {
            return;
        }
        timedChoiceProgressBarValueAnimator.cancel();
    }

    /**
     * Plays a sound given its name
     * The file has to be in the assets folder
     *
     * @param a    Activity
     * @param name String
     * @param loop boolean
     * @see SoundHandler
     */
    void playSound(Activity a, String name, boolean loop) {
        sh.generate(name, a, loop)
                .play(name);
    }

    /**
     * Returns the backup data structure
     *
     * @return Params
     */
    final Params getParams() {
        return params;
    }

    ArrayList<Phrase> getChoices() {
        return getTableOfLinks().getDestPhrases(getCurrentPhrase().getId(), getTableOfPhrases());
    }

    Phrase getRandomChoice() {
        ArrayList<Phrase> phrases = getChoices();
        int min = 0;
        int max = phrases.size() - 1;
        int res = new Random().nextInt((max - min) + 1) + min;
        return phrases.get(res);
    }

    /**
     * Formats the name in the sentence
     *
     * @param p Phrase
     */
    void formatName(Context context, Phrase p) {
        p.setSentence(context, p.getSentence()
                .replace("[Name]", tableOfSymbols.getGlobalFirstName())
                .replace("[name]", tableOfSymbols.getGlobalFirstName())
                .replace("[prenom]", tableOfSymbols.getGlobalFirstName()));
    }

    void setLastSeenIf(Phrase.Type type) {
        Phrase last = adapter.getLastItem();
        if (Phrase.determineTypeEnum(last.getType()) != type) {
            return;
        }
        adapter.setLastSeen();
    }

    /**
     * Inserts sentences
     *
     * @param authorId author id
     * @param sentence String ...
     */
    void insertPhrase(int authorId, Phrase.Type type, String... sentence) {
        for (String str : sentence) {
            adapter.insertPhrase(
                    Phrase.fast(authorId, Phrase.determineTypeCode(type), str, 0, 0),
                    type
            );
        }
    }

    /**
     * Inserts sentences Type Phrase.Type.me
     *
     * @param authorId int
     * @param sentence String
     */
    void insertPhrase(@SuppressWarnings("SameParameterValue") int authorId, String... sentence) {
        insertPhrase(authorId, Phrase.Type.me, sentence);
    }

    /**
     * Returns the overlay notification's first sentence.
     *
     * @param c Context
     * @return String
     */
    String getOverlayNotificationFirstSentence(Context c) {
        return c.getString(R.string.notification_first_sentence);
    }

    /**
     * Clears the reyclerview's adapter
     *
     * @see GameConversationAdapter
     */
    void clearRecyclerView() {
        adapter.clear();
    }

    /**
     * Sets the background media value
     *
     * @param hasBackgroundMedia boolean
     */
    void setHasBackgroundMedia(boolean hasBackgroundMedia) {
        this.hasBackgroundMedia = hasBackgroundMedia;
    }

    String getVideoToReload() {
        return videoToReload;
    }

    void setVideoToReload(String videoToReload) {
        this.videoToReload = videoToReload;
    }

    /**
     * Determines if the app should shoud the faster button
     *
     * @return boolean
     */
    boolean shouldShowFasterBtn() {
        return false;
    }

    /**
     * Returns the ImagePreview Activity requestCode
     *
     * @return int
     */
    int getImagePreviewRequestCode() {
        return 2544;
    }

    /**
     * Returns the RecyclerView LayoutManager
     *
     * @param a Activity
     * @return LayoutManager
     */
    CustomLinearLayoutManager getRecyclerViewLayoutManager(Activity a) {
        return (CustomLinearLayoutManager) ((RecyclerView) a.findViewById(R.id.fz4_mainActivity_recyclerview_fix)).getLayoutManager();
    }

    /**
     * Determines if it is a conversation or not
     *
     * @return boolean
     */
    boolean isNoSeen() {
        return !chapter.isConversation();
    }

    void updateAverageTime(int time) {
        Std.debug(time);
        params.updateAverageTime(time);
        Std.debug("Moyenne de rapidité de choix : ", params.getAverage(AverageTime.Type.SEC));
    }

    /**
     * Sets the timed choice value
     */
    void setTimedChoice(int time) {
        timedChoice = time;
    }

    /**
     * Disables the timed choice
     */
    void disableTimedChoice() {
        timedChoice = 0;
    }

    /**
     * Determines if the timed choice mode is activated
     *
     * @return boolean
     */
    boolean isTimedChoice() {
        return timedChoice > 0;
    }

    int getTimedChoiceTime() {
        return timedChoice;
    }

    void updateTimedChoiceListener(Activity a, boolean isTimedChoice, Runnable runnable) {
        View choiceArea = a.findViewById(R.id.fz4_mainactivity_choicebox_area);

        if (isTimedChoice) {
            Finger.defineOnTouch(choiceArea, a, null);
        } else {
            Finger.defineOnTouch(choiceArea, a, runnable);
        }
    }

    /**
     * Sets the timechoice progressbar value animator
     *
     * @param v ValueAnimator
     */
    void setTimeChoiceProgressBarValueAnimator(ValueAnimator v) {
        timedChoiceProgressBarValueAnimator = v;
    }

    /* REQUEST CODES */
    int getPhoneCallingRequestCode() {
        return 998;
    }

    int getHackGameRequestCode() {
        return 999;
    }
}
