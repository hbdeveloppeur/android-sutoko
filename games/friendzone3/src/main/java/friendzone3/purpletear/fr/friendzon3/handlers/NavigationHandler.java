package friendzone3.purpletear.fr.friendzon3.handlers;

import android.content.Context;
import android.content.Intent;

import friendzone3.purpletear.fr.friendzon3.End;
import friendzone3.purpletear.fr.friendzon3.MainActivity;
import friendzone3.purpletear.fr.friendzon3.PoeticActivity;
import friendzone3.purpletear.fr.friendzon3.textcinematic.TextCinematic;

public class NavigationHandler {
    private Navigation destination = Navigation.GAME;

    public enum Navigation {
        MENU,
        GAME,
        CINEMATIC,
        POETRY,
        END
    }

    public NavigationHandler() {
        destination = Navigation.GAME;
    }

    /**
     * Reloads the destionation
     * @param n Navigation enum
     */
    public void to(Navigation n) {
        destination = n;
    }


    public Navigation getDestination() {
        return destination;
    }


    public boolean toMenu() {
        return destination == Navigation.MENU;
    }

    public Intent getIntent(Context c) {
        switch (destination) {
            case CINEMATIC:
                return new Intent(c, TextCinematic.class);
            case MENU:
                return null;
            case GAME:
                return new Intent(c, MainActivity.class);
            case POETRY:
                return new Intent(c, PoeticActivity.class);
            case END:
                return new Intent(c, End.class);
            default:
                throw new IllegalArgumentException();
        }
    }
}
