package fr.purpletear.friendzone4.purpleTearTools;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.animation.AnimationUtils;

import fr.purpletear.friendzone4.R;

public class Animation {

    public enum Animations {
        ANIMATION_FADEIN,
        ANIMATION_SLIDE,
        ANIMATION_FADEOUT,
        ANIMATION_SLIDE_OUT,
        ANIMATION_SLIDE_IN_FROM_LEFT,
        ANIMATION_SLIDE_OUT_AND_STAY
    }
    /**
     * Overload setAnimation adding default duration.
     */
    public static long setAnimation(View viewToAnimate, Animations mode, Context context) {
        return setAnimation(viewToAnimate, mode, context, 0);
    }

    /**
     * Permits to create an animation with a specific duration
     *
     */
    public static long setAnimation(final View viewToAnimate, Animations mode, Context context, int duration) {
        android.view.animation.Animation animation;
        switch (mode) {
            case ANIMATION_FADEIN: {
                animation = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        viewToAnimate.setVisibility(View.VISIBLE);
                    }
                }, (duration == 0) ? animation.getDuration() : duration);
                break;
            }
            case ANIMATION_FADEOUT: {
                animation = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        viewToAnimate.setVisibility(View.INVISIBLE);
                    }
                }, (duration == 0) ? animation.getDuration() : duration);
                break;
            }
            case ANIMATION_SLIDE:
                animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
                break;
            case ANIMATION_SLIDE_OUT:
                animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_out_right);
                break;
            case ANIMATION_SLIDE_OUT_AND_STAY:
                animation = AnimationUtils.loadAnimation(context, R.anim.slide_out_right_and_stay);
                break;
            case ANIMATION_SLIDE_IN_FROM_LEFT:
                animation = AnimationUtils.loadAnimation(context, R.anim.slide_in_right);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        viewToAnimate.setVisibility(View.VISIBLE);
                    }
                }, (duration == 0) ? animation.getDuration() : duration);
                break;
            default:
                animation = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
                break;
        }
        if(duration > 0){
            animation.setDuration(duration);
        }
        viewToAnimate.startAnimation(animation);
        return animation.getDuration();
    }

    /**
     * Determines the name of the animation given his code
     * @param code the code of the animation
     * @return the name of the animation
     */
    public static String getNameFromAnimationCode(int code) {
        switch (code){
            case 1 :
                return "animation_glitch";
            default:
                throw new IllegalArgumentException("Unknown code" + " " + code);
        }
    }
}
