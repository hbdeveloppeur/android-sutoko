package purpletear.fr.purpleteartools;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;

public class SinglePlayer {
    private Sound sound;
    private boolean isPlaying = false;
    private String rootDirectory;

    public void kill() {
        isPlaying = false;
        if(sound == null)
            return;
        pause();

    }

    public SinglePlayer(String rootDirectory) {
        this.rootDirectory = rootDirectory;
    }

    private void pause(){
        try {
            if (sound.mp != null && sound.mp.isPlaying()) {
                sound.mp.pause();
                isPlaying = false;
            }

        } catch (Exception e) {
            Log.e("Console", "Cannot pause the player.");
        }
    }

    public String playWithName(String name, Activity activity, Runnable runnable){
        if(getCurrentSoundName().equals(name) && !isPlaying()) {
            play(null);
            return name;
        } else if(getCurrentSoundName().equals(name) && isPlaying()) {
            pause();
            return "";
        } else if(isPlaying()) {
            pause();
            generate(name, activity, false).play(runnable);
            return name;
        } else {
            generate(name, activity, false).play(runnable);
            return name;
        }
    }

    public String playWithNameFromFullPath(String name, Activity activity, Runnable runnable){
        if(getCurrentSoundName().equals(name) && !isPlaying()) {
            play(null);
            return name;
        } else if(getCurrentSoundName().equals(name) && isPlaying()) {
            pause();
            return "";
        } else if(isPlaying()) {
            pause();
            generateFromFullPath(name, activity, false).play(runnable);
            return name;
        } else {
            generateFromFullPath(name, activity, false).play(runnable);
            return name;
        }
    }

    public void play(@Nullable final Runnable r){
        try {
            sound.mp.start();
            isPlaying = true;
            sound.mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    isPlaying = false;
                    sound.name = "";
                    mp.release();
                    new Handler().post(r);

                }
            });
        } catch (Exception e) {
            Log.e("Console", "Cannot start the player.");
        }
    }

    public String getCurrentSoundName(){
        if(sound == null)
            return "";
        return sound.name;
    }

    public SinglePlayer generate(String name, Activity activity, boolean loop){
        try {
            AssetFileDescriptor message_audio = activity.getAssets().openFd(rootDirectory + "/sound/" + name + ".mp3");
            MediaPlayer player = new MediaPlayer();
            player.setLooping(loop);
            player.setDataSource(message_audio.getFileDescriptor(), message_audio.getStartOffset(), message_audio.getLength());
            player.prepare();
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.release();
                }
            });
            sound = new Sound(name, player);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    public SinglePlayer generateFromFullPath(String name, Activity activity, boolean loop){
        try {
            MediaPlayer player = new MediaPlayer();
            player.setDataSource(new File(name).getPath());
            player.setLooping(loop);
            player.prepare();
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.release();
                }
            });
            sound = new Sound(name, player);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    class Sound {
        private String name;
        private MediaPlayer mp;

        Sound(String name, MediaPlayer mp){
            this.name = name;
            this.mp = mp;
        }
    }
}
