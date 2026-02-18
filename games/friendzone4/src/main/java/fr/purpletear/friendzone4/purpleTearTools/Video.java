package fr.purpletear.friendzone4.purpleTearTools;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.widget.VideoView;

import androidx.annotation.Nullable;

public class Video {

    /**
     * Sets a videoView given a uri
     * @param v : The videoView
     * @param uri : The video descriptor
     * @param looping : Does the video needs to loop ?
     */
    public static void put(VideoView v, Uri uri, final boolean looping, @Nullable final Runnable completion) {
        if (uri == null) throw new AssertionError("Video.put.uri (Uri) cannot be null");
        if (v == null) throw new AssertionError("Video.put.v (VideoView) cannot be null");

        v.setVideoURI(uri);
        v.start();
        v.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(looping);
            }
        });
        v.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if(null != completion) completion.run();
            }
        });
    }

    /**
     * Returns the id of the raw resources given the name of the resource and the context.
     * @param name the name of the resource (without the extension)
     * @param context the calling activity context
     * @return id of the drawable.
     */
    public static int determine(String name, Context context) {
        int id = context.getResources().getIdentifier(name, "raw", context.getPackageName());
        if(id == 0) throw new IllegalArgumentException(name + " " + "not found.");
        return id;
    }

    /**
     * Sets a videoView given a uri, not looping.
     * @param v : The videoView
     * @param uri : The video descriptor
     */
    public static void put(VideoView v, Uri uri) {
        Video.put(v, uri, false, null);
    }

    /**
     * Sets a video given a uri, no completion,
     * @param v the vid
     * @param uri the video descriptor
     * @param looping does the video needs to loop or not ?
     */
    public static void put(VideoView v, Uri uri, boolean looping) {
        Video.put(v, uri, looping, null);
    }

    public static void start(VideoView v){
        if(v !=null && !v.isPlaying()) {
            v.start();
        }
    }

    /**
     * Pauses the video given a videoView
     * @param v the videow concerned
     */
    public static void pause(VideoView v){
        if(v !=null && v.isPlaying()) {
            v.pause();
        }
    }

    /**
     * Play a video
     * @param v the videowiew
     */
    public static void play(VideoView v) {
        v.start();
    }

}
