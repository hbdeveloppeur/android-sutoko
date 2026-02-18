package fr.purpletear.friendzone4.game.activities.splash;

import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.view.View;

import fr.purpletear.friendzone4.purpleTearTools.Animation;
import fr.purpletear.friendzone4.purpleTearTools.MemoryHandler;
import fr.purpletear.friendzone4.purpleTearTools.Runnable2;
import fr.purpletear.friendzone4.purpleTearTools.SoundHandler;

class SplashModel {
    /**
     * Handles the memory handlers
     *
     * @see MemoryHandler
     */
    private MemoryHandler mh;

    /**
     * Handles the sounds
     *
     * @see SoundHandler
     */
    private SoundHandler sh;

    SplashModel() {
        mh = new MemoryHandler();
        sh = new SoundHandler();
    }

    /**
     * Starts the animation
     *
     * @param a         Activity
     * @param animation View
     * @param listeners SplashInterface
     */
    void startAnimation(final Activity a, final View animation, final SplashInterface listeners) {
        Runnable2 runnable = new Runnable2("Animation appears", 1000) {
            @Override
            public void run() {
                Animation.setAnimation(
                        animation,
                        Animation.Animations.ANIMATION_FADEIN,
                        a,
                        1250);
                animate(a, animation, listeners);
            }
        };
        mh.push(runnable);
        mh.run(runnable);
    }

    /**
     * Animates the logo
     *
     * @param a         Activity
     * @param animation View
     * @param listeners SplashInterface
     */
    private void animate(final Activity a, final View animation, final SplashInterface listeners) {
        Runnable2 runnable = new Runnable2("Animates the logo", 2000) {
            @Override
            public void run() {

                // animateImage(a, animation);
                Runnable2 then = new Runnable2("Animation completed", 57 * 15) {
                    @Override
                    public void run() {
                        /* FADEOUT */
                        Animation.setAnimation(
                                animation,
                                Animation.Animations.ANIMATION_FADEOUT,
                                a,
                                1250);
                        onCompletion(listeners);
                    }
                };
                mh.push(then);
                mh.run(then);
            }
        };

        mh.push(runnable);
        mh.run(runnable);
    }

    /**
     * Code to run on animation completion
     *
     * @param listener SplashInterface
     */
    private void onCompletion(final SplashInterface listener) {
        Runnable2 runnable = new Runnable2("Animation completed + 1300 milliseconds", 1300) {
            @Override
            public void run() {
                listener.onCompletion();
            }
        };
        mh.push(runnable);
        mh.run(runnable);
    }

    /**
     * @param a the activity
     * @param i the imageView
     */
    private void animateImage(Activity a, View i) {
        int id = a.getResources().getIdentifier("fz4_anim_logo_purpletear", "drawable", a.getPackageName());
        if (id == 0) {
            throw new IllegalArgumentException("Animation" + " " + "anim_logo_purpletear" + " " + "Not found");
        }
        i.setBackgroundResource(id);
        final AnimationDrawable d = (AnimationDrawable) i.getBackground();
        d.setCallback(i);
        d.setVisible(true, true);
        d.start();

        sh.generate("glitch_01", a, false);
        sh.play("glitch_01");
    }

    /**
     * Kills the handlers and sounds
     */
    void kill() {
        mh.kill();
        sh.clear();
    }
}
