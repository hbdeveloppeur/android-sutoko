package fr.purpletear.friendzone4.game.activities.audiocinematic;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import fr.purpletear.friendzone4.GameData;
import fr.purpletear.friendzone4.R;
import fr.purpletear.friendzone4.factories.Std;
import fr.purpletear.friendzone4.purpleTearTools.Finger;

public class AudioCinematic extends AppCompatActivity {
    /**
     * Handles the graphic settings
     *
     * @see AudioCinematicGraphics
     */
    private AudioCinematicGraphics graphics;

    /**
     * Handles the model settings
     *
     * @see AudioCinematicModel
     */
    private AudioCinematicModel model;

    /**
     * Contains the current AudioState value.
     */
    private AudioState currentAudio = AudioState.STOPPED;

    /**
     * The Thread that updates the progressbar
     *
     * @see Thread
     */
    private Thread progressBarUpdate;

    enum AudioState {
        PLAYING,
        STOPPED,
        PAUSED,
        FINISHED
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Std.hideBars(getWindow(), false, true);
        setContentView(R.layout.fz4_activity_audio_cinematic);
        load();
    }

    /**
     * Inits the Activity's vars
     */
    private void load() {
        graphics = new AudioCinematicGraphics();
        model = new AudioCinematicModel(this, 6);
        listeners();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && model.isFirstStart()) {
            graphics.fadeOutFilter(this);
            images();
        } else if (!hasFocus) {
            pause();
        } else {
            resume();
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    /**
     * Sets images.
     *
     * @see AudioCinematicGraphics
     */
    private void images() {
        RequestManager glide = Glide.with(this);
        graphics.setImage(
                this,
                glide,
                R.drawable.fz4_ic_audio_tape,
                R.id.fz4_audio_cinematic_image_audio,
                true,
                false
        );

        graphics.setImage(
                this,
                glide,
                R.drawable.fz4_ic_play_tape,
                R.id.fz4_audio_cinematic_button_play_icon,
                false,
                false
        );
    }

    /**
     * Sets the listeners.
     */
    private void listeners() {
        Finger.defineOnTouch(
                findViewById(R.id.fz4_audio_cinematic_button_play),
                this,
                new Runnable() {
                    @Override
                    public void run() {
                        switch (currentAudio) {
                            case PLAYING:
                                stop();
                                updateIconImage(AudioState.STOPPED);
                                break;
                            case FINISHED:
                            case STOPPED:
                                play();
                                updateIconImage(AudioState.PLAYING);
                                break;
                        }
                    }
                }
        );

        Finger.defineOnTouch(
                findViewById(R.id.fz4_audio_cinematic_button_next),
                this,
                new Runnable() {
                    @Override
                    public void run() {
                        graphics.fadeInFilter(AudioCinematic.this, new Runnable() {
                            @Override
                            public void run() {
                                setResult(RESULT_OK);
                                finish();
                            }
                        });
                    }
                }
        );
    }

    /**
     * Plays the audio.
     */
    private void play() {
        if (currentAudio == AudioState.PLAYING) {
            throw new IllegalStateException("Calling play when the audio is already playing");
        }
        currentAudio = AudioState.PLAYING;
        graphics.changeButton(
                findViewById(R.id.fz4_audio_cinematic_button_play),
                this,
                AudioState.PLAYING
        );
        model.play(this, new AudioCinematicListener() {
            @Override
            public void onCompletion() {
                killThread();
                currentAudio = AudioState.FINISHED;
                graphics.setNextButtonVisibility(
                        findViewById(R.id.fz4_audio_cinematic_button_next),
                        true
                );
                graphics.changeButton(
                        findViewById(R.id.fz4_audio_cinematic_button_play),
                        AudioCinematic.this,
                        AudioState.FINISHED
                );
                updateIconImage(AudioState.STOPPED);
            }

            @Override
            public void onFoundLine(String text) {
            }
        });
        progress();
    }

    private void resume() {
        if (currentAudio != AudioState.PAUSED) {
            return;
        }
        currentAudio = AudioState.PLAYING;
        model.resume();
        progress();
    }

    /**
     * Stops the audio.
     */
    private void stop() {
        if (currentAudio == AudioState.STOPPED) {
            throw new IllegalStateException("Calling stop when the audio is already stopped");
        }
        killThread();
        currentAudio = AudioState.STOPPED;
        model.stop();
        graphics.changeButton(
                findViewById(R.id.fz4_audio_cinematic_button_play),
                AudioCinematic.this,
                AudioState.STOPPED
        );
    }

    /**
     * Pauses the audio file
     */
    private void pause() {
        if (currentAudio != AudioState.PLAYING) {
            return;
        }
        currentAudio = AudioState.PAUSED;
        model.pause();
    }

    /**
     * Updates the progressbar
     */
    private void progress() {
        progressBarUpdate = new Thread(new Runnable() {
            private long start = System.currentTimeMillis();

            @Override
            public void run() {
                //noinspection InfiniteLoopStatement
                for (; ; ) {
                    try {
                        Thread.sleep(280);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }

                    model.updateLine(
                            System.currentTimeMillis() - start,
                            new AudioCinematicListener() {
                                @Override
                                public void onCompletion() {

                                }

                                @Override
                                public void onFoundLine(final String text) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            updateAudioTextDescription(text);
                                        }
                                    });
                                }
                            }
                    );
                    if (currentAudio != AudioState.PLAYING) {
                        Std.debug("Stopping");
                        return;
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                graphics.updateProgressBar(
                                        findViewById(R.id.fz4_audio_cinematic_progress_background),
                                        findViewById(R.id.fz4_audio_cinematic_progress_bar),
                                        model.getSp().getTime(),
                                        model.getSp().getDuration()
                                );
                            } catch (Exception e) {
                                Log.e("ppdebug", e.toString());
                            }
                        }
                    });
                }
            }
        });
        progressBarUpdate.start();
    }

    /**
     * Kills the progressBarUpdate Thread
     *
     * @see Thread
     */
    private void killThread() {
        Std.debug("Killing the Thread");
        progressBarUpdate.interrupt();
        progressBarUpdate = null;
    }

    /**
     * Updates the icon on the main button
     *
     * @param state AudioState
     */
    private void updateIconImage(AudioState state) {
        switch (state) {
            case PLAYING:
                graphics.setImage(
                        this,
                        Glide.with(this),
                        R.drawable.fz4_ic_stop_tape,
                        R.id.fz4_audio_cinematic_button_play_icon,
                        false,
                        false
                );
                break;
            case STOPPED:
                graphics.setImage(
                        this,
                        Glide.with(this),
                        R.drawable.fz4_ic_play_tape,
                        R.id.fz4_audio_cinematic_button_play_icon,
                        false,
                        false
                );
                break;
        }
    }

    private void updateAudioTextDescription(String text) {
        ((TextView) findViewById(R.id.fz4_audio_cinematic_text_audio_description)).setText(GameData.INSTANCE.updateNames(this, text));
    }
}
