package fr.purpletear.friendzone4.custom;

import android.util.Log;

import fr.purpletear.friendzone4.BuildConfig;


public class DiscussionHandler {

    public static boolean execute(String name, boolean when){
        if(BuildConfig.DEBUG && when) {
            Log.i("Discussion", name);
        }
        return when;
    }

    public static void execute(String name, Runnable runnable) {
        if(BuildConfig.DEBUG) {
            Log.i("Discussion", name);
        }
        runnable.run();
    }
}
