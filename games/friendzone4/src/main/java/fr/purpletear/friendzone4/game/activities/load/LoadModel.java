package fr.purpletear.friendzone4.game.activities.load;

import com.example.sutokosharedelements.SutokoSharedElementsData;

import java.util.ArrayList;
import java.util.List;

import fr.purpletear.friendzone4.GameData;
import fr.purpletear.friendzone4.game.config.Chapter;
import fr.purpletear.friendzone4.game.config.Params;
import purpletear.fr.purpleteartools.TableOfSymbols;

class LoadModel {
    /**
     * Contains the first start state of the Activity
     */
    private boolean isFirstStart = true;

    /**
     * Determines where to navigate to
     */
    private NavigationHandler nh;

    private TableOfSymbols symbols;

    boolean hasSeenAudioCinematic;
    boolean hasSeenTextCinematic;
    boolean hasSeenLoading;
    boolean hasSeenPoetry;
    boolean isGranted;

    /**
     * Handles the game data.
     *
     * @see Params
     */
    private Params params;

    LoadModel(Params params, TableOfSymbols symbols, boolean isGranted) {
        hasSeenAudioCinematic = false;
        hasSeenTextCinematic = false;
        hasSeenLoading = false;
        hasSeenPoetry = false;
        nh = new NavigationHandler();
        this.params = params;
        this.symbols = symbols;
        this.isGranted = isGranted;
    }

    void invalidate() {
        hasSeenAudioCinematic = false;
        hasSeenTextCinematic = false;
        hasSeenLoading = false;
        hasSeenPoetry = false;
    }

    /**
     * Determines the current firstStart state of the Activity
     *
     * @return boolean
     */
    boolean isFirstStart() {
        boolean value = isFirstStart;
        isFirstStart = false;
        return value;
    }

    /**
     * Sets the parameters
     * @param p Params
     */
    void setParams(Params p) {
        params = p;
    }

    /**
     * Returns the table of symbols
     * @return TableOfSymbols
     */
    public TableOfSymbols getSymbols() {
        return symbols;
    }

    /**
     * Updates the current table of symbols
     * @param symbols TableOfSymbols
     */
    public void setSymbols(TableOfSymbols symbols) {
        this.symbols = symbols;
    }

    /**
     * Returns the NavigationHandler
     *
     * @return NavigationHandler
     * @see NavigationHandler
     */
    NavigationHandler getNavigationHandler() {
        return nh;
    }

    NavigationHandler.Navigation require() {
        if(requiresTextIntro()) {
            return NavigationHandler.Navigation.TEXTCINEMATIC;
        } else if (requiresAudioIntro()) {
            return NavigationHandler.Navigation.AUDIOCINEMATIC;
        } else if(requiresLoading()) {
            return NavigationHandler.Navigation.LOADING;
        } else if(nh.toMenu()) {
            return  NavigationHandler.Navigation.MENU;
        } else if(requiresPoetry()) {
            return  NavigationHandler.Navigation.POETRY;
        } else if(requiresEnd()) {
            return  NavigationHandler.Navigation.END;
        }
        return NavigationHandler.Navigation.GAME;
    }


    /**
     * @return boolean
     */
    private boolean requiresEnd() {
        List<String> array = new ArrayList<>();
        array.add("11a");
        array.add("11b");
        return array.contains(params.getChapterCode());
    }

    /**
     * @return boolean
     */
    private boolean requiresPoetry() {
        List<String> array = new ArrayList<>();
        array.add("7a");
        array.add("7b");
        array.add("8a");
        array.add("8b");
        array.add("9a");
        array.add("9c");

        //noinspection ConstantConditions
        return  SutokoSharedElementsData.IS_POETRY_ENABLED &&  array.contains(params.getChapterCode()) && !hasSeenPoetry;
    }


    /**
     * @return boolean
     */
    private boolean requiresAudioIntro() {
        List<String> array = new ArrayList<>();
        array.add("6a");

        return array.contains(params.getChapterCode()) && !hasSeenAudioCinematic;
    }

    private boolean requiresTextIntro() {
        List<String> array = new ArrayList<>();
        array.add("1a");
        array.add("5a");
        array.add("10a");
        array.add("10b");
        array.add("10c");

        //noinspection ConstantConditions
        return SutokoSharedElementsData.IS_TEXTCINEMATIC_ENABLED && array.contains(params.getChapterCode()) && !hasSeenTextCinematic;
    }

    private boolean requiresLoading() {
        List<String> array = new ArrayList<>();

        return array.contains(params.getChapterCode()) && !hasSeenLoading;
    }

    /**
     * Returns the Params object
     *
     * @return Params
     */
    Params getParams() {
        return params;
    }

}
