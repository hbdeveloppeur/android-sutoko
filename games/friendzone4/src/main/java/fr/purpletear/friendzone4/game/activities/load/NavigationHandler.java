package fr.purpletear.friendzone4.game.activities.load;

import android.content.Context;
import android.content.Intent;

import fr.purpletear.friendzone4.game.activities.audiocinematic.AudioCinematic;
import fr.purpletear.friendzone4.game.activities.end.End;
import fr.purpletear.friendzone4.game.activities.loading.Loading;
import fr.purpletear.friendzone4.game.activities.main.Main;
import fr.purpletear.friendzone4.game.activities.poetry.Poetry;
import fr.purpletear.friendzone4.game.activities.textcinematic.TextCinematic;


/**
 * The NavigationHandler handles the destination from the currentActivity.
 */
class NavigationHandler {

    /**
     * Determines the destination
     */
    private Navigation destination;

    public enum Navigation {
        MENU,
        GAME,
        AUDIOCINEMATIC,
        LOADING,
        TEXTCINEMATIC,
        VIDEOAD,
        INTERAD,
        POETRY,
        END
    }

    NavigationHandler() {
        destination = Navigation.GAME;
    }

    /**
     * Reloads the destionation
     *
     * @param n Navigation enum
     */
    public void to(Navigation n) {
        destination = n;
    }

    boolean toMenu() {
        return destination == Navigation.MENU;
    }

    /**
     * Returns the destination Intent
     *
     * @param c Context
     * @return Intent
     */
    Intent getIntent(Context c) {
        switch (destination) {
            case LOADING:
                return new Intent(c, Loading.class);
            case TEXTCINEMATIC:
                return new Intent(c, TextCinematic.class);
            case AUDIOCINEMATIC:
                return new Intent(c, AudioCinematic.class);
            case MENU:
                return null;
            case GAME:
                return new Intent(c, Main.class);
            case POETRY:
                return new Intent(c, Poetry.class);
            case END:
                return new Intent(c, End.class);
            default:
                throw new IllegalArgumentException();
        }
    }

    /**
     * @return the request code number using ordinals
     */
    int getRequestCode() {
        return destination.ordinal();
    }
}
