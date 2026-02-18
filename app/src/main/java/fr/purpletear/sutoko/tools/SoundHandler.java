/*
 * Copyright (C) PurpleTear, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Hocine Belbouab <hbdeveloppeur@gmail.com>, 1/10/19 11:38 AM
 *
 */

package fr.purpletear.sutoko.tools;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Handler;
import androidx.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class SoundHandler {
    private List<Sound> array = new ArrayList<>();

    private void push(String name, MediaPlayer mp){
        array.add(new Sound(name, mp));
    }

    public void clear(){
        for(Sound sound : array){
            try {
                if (sound.mp != null && sound.mp.isPlaying()) {
                    sound.mp.pause();
                    sound.mp.release();
                }
            } catch (Exception e) {
                Log.e("Console", "Cannot pause the player.");
            }
        }
        array.clear();
    }

    public boolean exist(String name) {
        for(Sound sound : array) {
            if(sound.name.equals(name)) {
                return true;
            }
        }
        return false;
    }

    public int pause(String name){
        for(Sound sound : array){
            if(!sound.name.equals(name)) continue;
            try {
                if (sound.mp != null && sound.mp.isPlaying()) {
                    int currentPosition = sound.mp.getCurrentPosition();
                    sound.mp.pause();
                    return currentPosition;
                }
            } catch (Exception e) {
                Log.e("Console", "Cannot pause the player.");
            }
        }
        return 0;
    }

    public void play(String name){
        play(name,  null);
    }

    public void play(String name, int at) {play(name, at, null);}

    public void play(String name, int at, @Nullable final Runnable r){
        for(Sound sound : array){
            if(sound.name.equals(name)){
                try {
                    sound.mp.seekTo(at);
                    sound.mp.start();
                    sound.mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            mp.release();
                            new Handler().post(r);
                        }
                    });
                } catch (Exception e) {
                    Log.e("Console", "Cannot start the player.");
                }
                return;
            }
        }
    }

    public void play(String name, @Nullable final Runnable r){
        play(name, 0, r);
    }



    private void removeIfExist(String name) {
        int i = 0;
        boolean found = false;
        for(Sound s : array) {
            if(s.name.equals(name)) {
                found = true;
                break;
            }
            i++;
        }

        if(found) {
            array.remove(i);
        }

    }

    public SoundHandler generateFromPath(String path, Activity activity, final boolean loop) {
        removeIfExist(path);
        try {
            AssetFileDescriptor message_audio = activity.getAssets().openFd(path);
            MediaPlayer player = new MediaPlayer();
            player.setLooping(loop);
            player.setDataSource(message_audio.getFileDescriptor(), message_audio.getStartOffset(), message_audio.getLength());
            player.prepare();
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if(!loop) {
                        mp.release();
                    }
                }
            });
            player.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    //Invoked when there has been an error during an asynchronous operation
                    switch (what) {
                        case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                            Log.e("MediaPlayer Error", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra);
                            break;
                        case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                            Log.e("MediaPlayer Error", "MEDIA ERROR SERVER DIED " + extra);
                            break;
                        case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                            Log.e("MediaPlayer Error", "MEDIA ERROR UNKNOWN " + extra);
                            break;
                    }
                    return true;
                }
            });
            push(path, player);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    public SoundHandler generate(String name, Activity activity, final boolean loop){
        return generateFromPath("sound/" + name + "mp3", activity, loop);
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
