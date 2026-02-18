package friendzone3.purpletear.fr.friendzon3.handlers;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Handler;
import androidx.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import friendzone3.purpletear.fr.friendzon3.Data;

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

    public void play(String name, @Nullable final Runnable r){
        for(Sound sound : array){
            if(sound.name.equals(name)){
                try {
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

    public SoundHandler generate(final String name, final Activity activity, final boolean loop){
        removeIfExist(name);
        try {
            AssetFileDescriptor message_audio = activity.getAssets().openFd(Data.assetsDirectoryName + "/sound/" + name + ".mp3");
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
