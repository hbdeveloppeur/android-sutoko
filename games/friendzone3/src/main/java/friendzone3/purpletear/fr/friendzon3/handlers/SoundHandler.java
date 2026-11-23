package friendzone3.purpletear.fr.friendzon3.handlers;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.Nullable;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import friendzone3.purpletear.fr.friendzon3.Data;

public class SoundHandler {
    private final Map<String, MediaPlayer> players = new LinkedHashMap<>();

    public void clear() {
        for (MediaPlayer player : players.values()) player.release();
        players.clear();
    }

    public boolean exist(String name) {
        return players.containsKey(name);
    }

    public void pause(String name) {
        MediaPlayer player = players.get(name);
        if (player != null && player.isPlaying()) player.pause();
    }

    public void play(String name) { play(name, null); }

    public void play(String name, @Nullable final Runnable completion) {
        MediaPlayer player = players.get(name);
        if (player == null) return;
        player.setOnCompletionListener(finished -> {
            // Regeneration/clear may already have replaced this player.
            if (players.get(name) == finished) {
                players.remove(name);
                finished.release();
                if (completion != null) new Handler(Looper.getMainLooper()).post(completion);
            }
        });
        player.start();
    }

    public SoundHandler generate(String name, Activity activity, boolean loop) {
        MediaPlayer previous = players.remove(name);
        if (previous != null) previous.release();
        MediaPlayer player = new MediaPlayer();
        try {
            Data.INSTANCE.setSoundSource(activity, player, name);
            player.setLooping(loop);
            player.prepare();
            players.put(name, player);
        } catch (IOException | IllegalArgumentException e) {
            player.release();
            Log.e("Friendzoned3Audio", "Cannot load sound " + name, e);
        }
        return this;
    }
}
