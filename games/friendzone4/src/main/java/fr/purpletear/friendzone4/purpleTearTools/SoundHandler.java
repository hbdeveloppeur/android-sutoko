package fr.purpletear.friendzone4.purpleTearTools;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.sutokosharedelements.OnlineAssetsManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import purpletear.fr.purpleteartools.GlobalData;


public class SoundHandler {
    private List<Sound> array = new ArrayList<>();

    private void push(String name, MediaPlayer mp){
        array.add(new Sound(name, mp));
    }

    public void clear(){
        for(Sound sound : array){
            sound.mp.release();
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

    public void pause(String name){
        for(Sound sound : array){
            if(!sound.name.equals(name)) continue;
            try {
                if (sound.mp != null && sound.mp.isPlaying())
                    sound.mp.pause();
            } catch (Exception e) {
                Log.e("Console", "Cannot pause the player.");
            }
        }
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

    public int getCurrentPosition(String name){
        for(Sound sound : array){
            if(sound.name.equals(name)){
                try {
                    return sound.mp.getCurrentPosition();
                } catch (Exception e) {
                    Log.e("Console", "Cannot start the player.");
                }
                return 0;
            }
        }
        return 0;
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

    public SoundHandler generate(String name, Activity activity, final boolean loop){
        removeIfExist(name);
        try {
            MediaPlayer player = new MediaPlayer();
            player.setLooping(loop);
            player.setDataSource(OnlineAssetsManager.INSTANCE.getSoundFilePath(activity, String.valueOf(GlobalData.Game.FRIENDZONE4.getId()), "fz4_" + name));
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
            push(name, player);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
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
