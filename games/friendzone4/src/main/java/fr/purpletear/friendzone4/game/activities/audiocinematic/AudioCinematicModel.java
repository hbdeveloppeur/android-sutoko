package fr.purpletear.friendzone4.game.activities.audiocinematic;

import android.app.Activity;
import android.content.Context;

import fr.purpletear.friendzone4.purpleTearTools.SinglePlayer;
import purpletear.fr.purpleteartools.GameLanguage;

class AudioCinematicModel {

    /**
     * Represents the audio player.
     *
     * @see SinglePlayer
     */
    private SinglePlayer sp;

    /**
     * Contains the first start state of the Activity
     */
    private boolean isFirstStart;

    private AudioDescriptionReader audioDescriptionReader;

    AudioCinematicModel(Context c, int chapterNumber) {
        isFirstStart = true;
        sp = new SinglePlayer();
        audioDescriptionReader = new AudioDescriptionReader();
        audioDescriptionReader.read(c, chapterNumber);
        audioDescriptionReader.describe();
    }

    void updateLine(long currentTime, AudioCinematicListener listener) {
        audioDescriptionReader.requestLine((int) currentTime, listener);
    }

    /**
     * Returns the first start state of the Activity
     *
     * @return boolean
     */
    public boolean isFirstStart() {
        boolean value = isFirstStart;
        isFirstStart = false;
        return value;
    }

    /**
     * Plays the SinglePlayer
     *
     * @param a        Activity
     * @param listener AudioCinematicListener
     * @see SinglePlayer
     */
    void play(Activity a, final AudioCinematicListener listener) {
        sp.generate("intro_6/intro_" + GameLanguage.Companion.determineLangDirectory(), a, false);
        sp.play(new Runnable() {
            @Override
            public void run() {
                listener.onCompletion();
            }
        });
    }

    public SinglePlayer getSp() {
        return sp;
    }

    /**
     * Stops the SinglePLayer
     *
     * @see SinglePlayer
     */
    void stop() {
        sp.stop();
    }

    void resume() {
        sp.resume();
    }

    /**
     * Pauses the SinglePlayer
     *
     * @see SinglePlayer
     */
    void pause() {
        sp.pause();
    }

}
