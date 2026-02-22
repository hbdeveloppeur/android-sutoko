package fr.purpletear.friendzone4.factories;

import android.content.Context;
import android.content.res.AssetManager;

import com.example.sutokosharedelements.OnlineAssetsManager;

import java.io.IOException;
import java.io.InputStream;

import purpletear.fr.purpleteartools.GlobalData;

public class Data {

    /**
     * Returns the content of an asset file given its path
     *
     * @param am   AssetManager
     * @param path String
     * @return String content
     * @throws IOException -
     */
    public static String getAssetContent(AssetManager am, String path) throws IOException {
        InputStream is = am.open(path);
        int size = is.available();
        byte[] buffer = new byte[size];
        int read = is.read(buffer);
        if (0 == read) {
            throw new IllegalStateException("Error: " + path + " seems empty or null");
        }
        is.close();
        return new String(buffer);
    }

    public static String getImage(Context context, String name) {
        return OnlineAssetsManager.INSTANCE.getImageFilePath(context, String.valueOf(GlobalData.Game.FRIENDZONE4.getId()), name);
    }

    public static String getSound(Context context, String name) {
        return OnlineAssetsManager.INSTANCE.getSoundFilePath(context, String.valueOf(GlobalData.Game.FRIENDZONE4.getId()), name);
    }
}
