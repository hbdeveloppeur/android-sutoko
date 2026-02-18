/*
 * Copyright (C) PurpleTear, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Hocine Belbouab <hbdeveloppeur@gmail.com>, 1/10/19 11:38 AM
 *
 */

package purpletear.fr.purpleteartools;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.annotation.Keep;

@Keep
public class Animation {

    /**
     * Overload setAnimation adding default duration.
     */
    public static long setAnimation(View viewToAnimate, Animations mode, Context context) {
        return setAnimation(viewToAnimate, mode, context, 0);
    }

    /**
     * Permits to create an animation with a specific duration
     */
    public static long setAnimation(final View viewToAnimate, Animations mode, Context context, int duration) {
        android.view.animation.Animation animation;
        switch (mode) {
            case ANIMATION_FADEIN -> {
                animation = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        viewToAnimate.setVisibility(View.VISIBLE);
                    }
                }, (duration == 0) ? animation.getDuration() : duration);
            }
            case ANIMATION_FADEOUT -> {
                animation = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        viewToAnimate.setVisibility(View.INVISIBLE);
                    }
                }, (duration == 0) ? animation.getDuration() : duration);
            }
            case ANIMATION_SLIDE_IN_FROM_RIGHT -> {
                animation = AnimationUtils.loadAnimation(context, R.anim.slide_in_right);
                Handler handler = new Handler();
                handler.postDelayed(() -> viewToAnimate.setVisibility(View.VISIBLE), (duration == 0) ? animation.getDuration() : duration);
            }
            case ANIMATION_SLIDE_IN_FROM_BOTTOM -> {
                animation = AnimationUtils.loadAnimation(context, R.anim.slide_in_bottom);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        viewToAnimate.setVisibility(View.VISIBLE);
                    }
                }, (duration == 0) ? animation.getDuration() : duration);
            }
            case ANIMATION_SLIDE_IN_FROM_LEFT ->
                    animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
            case ANIMATION_SLIDE_OUT_TO_RIGHT ->
                    animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_out_right);
            case ANIMATION_SLIDE_OUT_TO_LEFT ->
                    animation = AnimationUtils.loadAnimation(context, R.anim.slide_out_to_left);
            case ANIMATION_SLIDE_OUT_TO_BOTTOM -> {
                animation = AnimationUtils.loadAnimation(context, R.anim.slide_out_bottom);
                Handler handler = new Handler();
                handler.postDelayed(() -> viewToAnimate.setVisibility(View.INVISIBLE), (duration == 0) ? animation.getDuration() : duration);
            }
            case ANIMATION_SLIDE_OUT_AND_STAY ->
                    animation = AnimationUtils.loadAnimation(context, R.anim.slide_out_right_and_stay);
            case ANIMATION_ZOOM_IN -> {
                viewToAnimate.setVisibility(View.VISIBLE);
                animation = AnimationUtils.loadAnimation(context, R.anim.zoom_in);
            }
            default -> animation = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
        }
        if (duration > 0) {
            animation.setDuration(duration);
        }
        viewToAnimate.startAnimation(animation);
        return animation.getDuration();
    }

    /**
     * Animates a image into an ImageView
     *
     * @param activity    Activity
     * @param imageViewId int
     * @param animationId int
     */
    public static void animate(Activity activity, int imageViewId, int animationId) {
        ImageView i = activity.findViewById(imageViewId);
        i.setBackgroundResource(animationId);
        final AnimationDrawable a = (AnimationDrawable) i.getBackground();
        a.setCallback(i);
        a.setVisible(true, true);
        a.start();
    }

    @Keep
    public enum Animations {
        ANIMATION_FADEIN,
        ANIMATION_FADEOUT,
        // Slides in from left
        ANIMATION_SLIDE_IN_FROM_LEFT,
        // Zooms in
        ANIMATION_ZOOM_IN,
        // Slides in from bottom
        ANIMATION_SLIDE_IN_FROM_BOTTOM,
        // Slides out to bottom
        ANIMATION_SLIDE_OUT_TO_BOTTOM,
        // Slides out to right
        ANIMATION_SLIDE_OUT_TO_RIGHT,
        // Slides in from right
        ANIMATION_SLIDE_IN_FROM_RIGHT,
        ANIMATION_SLIDE_OUT_TO_LEFT,
        ANIMATION_SLIDE_OUT_AND_STAY
    }
}
