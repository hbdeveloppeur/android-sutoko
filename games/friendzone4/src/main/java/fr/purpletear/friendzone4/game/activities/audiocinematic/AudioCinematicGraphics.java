package fr.purpletear.friendzone4.game.activities.audiocinematic;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import fr.purpletear.friendzone4.R;
import fr.purpletear.friendzone4.purpleTearTools.Animation;
import fr.purpletear.friendzone4.purpleTearTools.Maths;

class AudioCinematicGraphics {

    AudioCinematicGraphics() {

    }

    /**
     * Sets an image into the given View's id
     *
     * @param a     Activity
     * @param glide RequestManager
     * @param load  int
     * @param into  int
     */
    void setImage(Activity a, RequestManager glide, int load, int into, boolean originalSize, boolean ARGB_888) {
        if(a.isFinishing()) {
            return;
        }
        if (originalSize && ARGB_888) {
            glide.load(load)
                    .apply(new RequestOptions().format(DecodeFormat.PREFER_ARGB_8888).override(Target.SIZE_ORIGINAL))
                    .into((ImageView) a.findViewById(into));
        } else if (originalSize) {
            glide.load(load)
                    .apply(new RequestOptions().override(Target.SIZE_ORIGINAL))
                    .into((ImageView) a.findViewById(into));
        } else if (ARGB_888) {
            glide.load(load)
                    .apply(new RequestOptions().override(Target.SIZE_ORIGINAL))
                    .into((ImageView) a.findViewById(into));
        } else {
            glide.load(load)
                    .into((ImageView) a.findViewById(into));
        }
    }

    /**
     * Changes the button's text given the AudiState parameter
     *
     * @param state AudioState
     */
    void changeButton(View v, Context c, AudioCinematic.AudioState state) {
        String str;

        switch (state) {
            case PLAYING:
                str = c.getString(R.string.audio_cinematic_button_stop);
                break;
            case STOPPED:
                str = c.getString(R.string.audio_cinematic_button_play);
                break;
            case FINISHED:
                str = c.getString(R.string.audio_cinematic_button_restart);
                break;
            default:
                throw new IllegalArgumentException("Unknow AudioState");
        }

        ((TextView) v).setText(str);
    }

    /**
     * Updates the button "next" visibility
     * @param v View
     * @param isVisible boolean
     */
    void setNextButtonVisibility(View v, boolean isVisible) {
        v.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
    }

    /**
     * Updates the progressbar
     *
     * @param bg       View
     * @param bar      View
     * @param time     int
     * @param duration int
     */
    void updateProgressBar(View bg, View bar, int time, int duration) {
        int percent = (int) Maths.percent(time, duration);
        if (!(bar.getLayoutParams() instanceof ViewGroup.MarginLayoutParams)) {
            throw new IllegalArgumentException();
        }
        ViewGroup.LayoutParams p = bar.getLayoutParams();
        p.width = bg.getWidth() * percent / 100;
        bar.setLayoutParams(p);
    }

    /**
     * Fades the filter view out
     * @param a Activity
     */
    void fadeOutFilter(Activity a) {
        Animation.setAnimation(
                a.findViewById(R.id.fz4_audio_cinematic_filter_black),
                Animation.Animations.ANIMATION_FADEOUT,
                a
        );
    }

    /**
     * Fades the filter view in
     * @param a Activity
     */
    void fadeInFilter(Activity a, Runnable onCompletion) {
        long duration = Animation.setAnimation(
                a.findViewById(R.id.fz4_audio_cinematic_filter_black),
                Animation.Animations.ANIMATION_FADEIN,
                a
        );

        new Handler().postDelayed(onCompletion, duration);
    }
}
