package fr.purpletear.friendzone4.game.activities.audiocinematic;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import purpletear.fr.purpleteartools.CinematicProgressView;
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

    private final Handler progressHandler = new Handler(Looper.getMainLooper());
    private CinematicProgressView progress;
    private final Runnable updateProgress = new Runnable() {
        @Override
        public void run() {
            if (currentAudio != AudioState.PLAYING || !model.getSp().isPlaying()) return;
            int position = model.getSp().getTime();
            int duration = model.getSp().getDuration();
            if (duration > 0) progress.setProgress(position / (float) duration);
            model.updateLine(position, new AudioCinematicListener() {
                @Override public void onCompletion() { }
                @Override public void onFoundLine(String text) { updateAudioTextDescription(text); }
            });
            progressHandler.postDelayed(this, 50);
        }
    };

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
        progress = findViewById(R.id.cinematic_progress);
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
        progress.setProgress(0f);
        graphics.changeButton(
                findViewById(R.id.fz4_audio_cinematic_button_play),
                this,
                AudioState.PLAYING
        );
        model.play(this, new AudioCinematicListener() {
            @Override
            public void onCompletion() {
                stopProgressUpdates();
                progress.setProgress(1f);
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
        stopProgressUpdates();
        currentAudio = AudioState.STOPPED;
        progress.setProgress(0f);
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
        stopProgressUpdates();
        currentAudio = AudioState.PAUSED;
        model.pause();
    }

    private void progress() {
        stopProgressUpdates();
        progressHandler.post(updateProgress);
    }

    private void stopProgressUpdates() {
        progressHandler.removeCallbacks(updateProgress);
    }

    @Override
    protected void onPause() {
        pause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        stopProgressUpdates();
        if (model != null) model.getSp().release();
        super.onDestroy();
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
