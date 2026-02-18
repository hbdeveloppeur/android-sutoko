package fr.purpletear.friendzone4.game.activities.phonecall;

import android.app.Activity;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

import com.bumptech.glide.RequestManager;

import fr.purpletear.friendzone4.purpleTearTools.MemoryHandler;
import fr.purpletear.friendzone4.purpleTearTools.Runnable2;
import fr.purpletear.friendzone4.purpleTearTools.SoundHandler;

class PhoneCallModel {
    /**
     * Contains the firstStartActivityState.
     */
    private boolean isFirstStart;

    /**
     * Glide RequestManager
     */
    private RequestManager glide;

    /**
     * Contains the sound structure
     */
    private SoundHandler sh;

    /**
     * Contains the soundName
     */
    private final static String soundName = "ttsy";

    /**
     * Contains the currentSoundPosition
     */
    private int currentSoundPosition;

    /**
     * Determines if the sound is playing
     */
    boolean isSoundPlaying;

    /**
     * Contains the MemoryHandler
     *
     * @see MemoryHandler
     */
    MemoryHandler mh;

    PhoneCallModel(RequestManager glide) {
        isFirstStart = true;
        this.glide = glide;
        currentSoundPosition = 0;
        isSoundPlaying = false;
        mh = new MemoryHandler();
        sh = new SoundHandler();
    }

    /**
     * Determines the FirstStart Activity's state
     *
     * @return boolean
     */
    boolean isFirstStart() {
        boolean value = isFirstStart;
        isFirstStart = false;
        return value;
    }

    /**
     * Returns the Activity's RequestManager
     *
     * @return RequestManager
     * @see RequestManager
     */
    RequestManager getRequestManager() {
        return glide;
    }

    /**
     * Clears the model.
     */
    void clear() {
        mh.kill();
    }

    /**
     * Vibrates
     *
     * @param n number of vibration
     * @param o Vibrator
     */
    void vibrate(final int n, final Object o) {
        if (n == 0) {
            return;
        }
        final Vibrator v = (Vibrator) o;
        if (v != null && v.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= 26) {
                v.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                v.vibrate(150);
            }
        }

        Runnable2 runnable = new Runnable2("", 1000) {
            @Override
            public void run() {
                vibrate(n - 1, v);
            }
        };
        mh.push(runnable);
        mh.run(runnable);
    }


    /**
     * Plays the sound
     *
     * @param a            Activity
     */
    void generateSound(Activity a) {

        sh.generate(soundName, a, false);
    }

    /**
     * Plays the sound
     */
    void playSound(final Runnable onCompletion) {
        sh.play(soundName, currentSoundPosition, new Runnable() {
            @Override
            public void run() {
                isSoundPlaying = false;
                if(onCompletion != null) {
                    onCompletion.run();
                }
            }
        });
        isSoundPlaying = true;
    }

    /**
     * Determines if the sound should be launched again
     * @return boolean
     */
    boolean shouldResumeSound() {
        return  isSoundPlaying;
    }

    /**
     * Pauses the sound
     */
    void pauseSound() {
        sh.pause(soundName);
        currentSoundPosition = sh.getCurrentPosition(soundName);
    }
}
