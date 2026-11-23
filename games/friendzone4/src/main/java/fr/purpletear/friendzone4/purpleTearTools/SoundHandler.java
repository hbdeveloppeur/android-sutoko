package fr.purpletear.friendzone4.purpleTearTools;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.Nullable;
import com.example.sharedelements.OnlineAssetsManager;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import purpletear.fr.purpleteartools.GlobalData;

public class SoundHandler {
    private final Map<String, MediaPlayer> players = new LinkedHashMap<>();

    public void clear() {
        for (MediaPlayer player : players.values()) player.release();
        players.clear();
    }

    public boolean exist(String name) { return players.containsKey(name); }

    public void pause(String name) {
        MediaPlayer player = players.get(name);
        if (player != null && player.isPlaying()) player.pause();
    }

    public void play(String name) { play(name, 0, null); }
    public void play(String name, int at) { play(name, at, null); }
    public void play(String name, @Nullable Runnable completion) { play(name, 0, completion); }

    public void play(String name, int at, @Nullable Runnable completion) {
        MediaPlayer player = players.get(name);
        if (player == null) return;
        player.setOnCompletionListener(finished -> {
            if (players.get(name) == finished) {
                players.remove(name);
                finished.release();
                if (completion != null) new Handler(Looper.getMainLooper()).post(completion);
            }
        });
        player.seekTo(Math.max(0, at));
        player.start();
    }

    public int getCurrentPosition(String name) {
        MediaPlayer player = players.get(name);
        return player == null ? 0 : player.getCurrentPosition();
    }

    public SoundHandler generate(String name, Activity activity, boolean loop) {
        MediaPlayer previous = players.remove(name);
        if (previous != null) previous.release();
        MediaPlayer player = new MediaPlayer();
        try {
            player.setDataSource(OnlineAssetsManager.INSTANCE.getSoundFilePath(activity,
                String.valueOf(GlobalData.Game.FRIENDZONE4.getId()), "fz4_" + name));
            player.setLooping(loop);
            player.prepare();
            players.put(name, player);
        } catch (IOException | IllegalArgumentException e) {
            player.release();
            Log.e("Friendzoned4Audio", "Cannot load sound " + name, e);
        }
        return this;
    }
}
