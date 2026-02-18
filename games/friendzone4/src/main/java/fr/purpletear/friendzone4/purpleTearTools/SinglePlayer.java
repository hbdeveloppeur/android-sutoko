package fr.purpletear.friendzone4.purpleTearTools;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.IOException;

import fr.purpletear.friendzone4.factories.Data;

public class SinglePlayer {
    private Sound sound;
    private boolean isPlaying = false;

    public void kill() {
        isPlaying = false;
        if(sound == null)
            return;
        pause();

    }

    public void pause(){
        try {
            if (sound.mp != null && sound.mp.isPlaying()) {
                sound.mp.pause();
                isPlaying = false;
            }

        } catch (Exception e) {
            Log.e("Console", "Cannot pause the player.");
        }
    }

    /**
     * Stops the SinglePlayer
     */
    public void stop() {
        if(!isPlaying()) {
            sound.name = "";
            return;
        }
        sound.mp.stop();
        sound.mp.release();
        isPlaying = false;
        sound.name = "";
    }

    public void playWithName(String name, Activity activity, Runnable runnable){
        if(getCurrentSoundName().equals(name) && !isPlaying()) {
            play(null);
        } else if(getCurrentSoundName().equals(name) && isPlaying()) {
            pause();
        } else if(isPlaying()) {
            pause();
            generate(name, activity, false).play(runnable);
        } else {
            Std.debug("play sound " + name);
            generate(name, activity, false).play(runnable);
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

            MediaPlayer player = new MediaPlayer();
            player.setLooping(loop);
            player.setDataSource(Data.getSound(activity, "fz4_" + name ));
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

    public int getTime() {
        if(sound == null || sound.mp == null) {
            return 0;
        }

        return sound.mp.getCurrentPosition();
    }

    public int getDuration() {
        if(sound == null || sound.mp == null) {
            return 0;
        }

        return sound.mp.getDuration();
    }

    public void resume() {
        isPlaying = true;
        sound.mp.start();
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
