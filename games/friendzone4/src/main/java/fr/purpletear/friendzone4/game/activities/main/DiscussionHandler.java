package fr.purpletear.friendzone4.game.activities.main;

import android.util.Log;

import fr.purpletear.friendzone4.BuildConfig;


class DiscussionHandler {

    static boolean execute(String name, boolean when){
        if(BuildConfig.DEBUG && when) {
            Log.i("Discussion", name);
        }
        return when;
    }

    static void execute(String name, Runnable runnable) {
        if(BuildConfig.DEBUG) {
            Log.i("Discussion", name);
        }
        runnable.run();
    }
}
