package fr.purpletear.friendzone4.custom;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;

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

    public void playWithName(File file, Activity activity, Runnable runnable){
        if(getCurrentSoundName().equals(file.getName()) && !isPlaying()) {
            play(activity, null);
        } else if(getCurrentSoundName().equals(file.getName()) && isPlaying()) {
            pause();
        } else if(isPlaying()) {
            pause();
            generate(file, false).play(activity, runnable);
        } else {
            generate(file, false).play(activity, runnable);
        }
    }

    public void  play(final Context c, @Nullable final Runnable r){
        try {
            sound.mp.start();
            isPlaying = true;
            sound.mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    isPlaying = false;
                    sound.name = "";
                    mp.release();
                    Log.e("purpleteardebug", "#i");
                    new Handler(c.getMainLooper()).post(r);

                }
            });
        } catch (Exception e) {
            Log.e("Console", "Cannot start the player.");
        }
    }

    private String getCurrentSoundName(){
        if(sound == null)
            return "";
        return sound.name;
    }

    private SinglePlayer generate(File file, boolean loop){
        try {
            MediaPlayer player = new MediaPlayer();
            player.setLooping(loop);
            player.setDataSource(file.getAbsolutePath());
            player.prepare();
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.release();
                }
            });
            sound = new Sound(file.getName(), player);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    private boolean isPlaying() {
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
