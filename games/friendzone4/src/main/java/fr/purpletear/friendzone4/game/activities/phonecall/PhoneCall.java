package fr.purpletear.friendzone4.game.activities.phonecall;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import fr.purpletear.friendzone4.GameData;
import fr.purpletear.friendzone4.R;
import fr.purpletear.friendzone4.purpleTearTools.Finger;

public class PhoneCall extends AppCompatActivity {
    /**
     * Contains the graphics settings, methods and structure.
     *
     * @see PhoneCallGraphics
     */
    private PhoneCallGraphics graphics;

    /**
     * Contains the model settings, methods and structure.
     *
     * @see PhoneCallModel
     */
    private PhoneCallModel model;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fz4_activity_phone_call);
        load();
        listeners();
    }

    @Override
    protected void onDestroy() {
        model.clear();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        model.pauseSound();
        super.onPause();
    }

    /**
     * Inits the Activity's vars
     */
    private void load() {
        graphics = new PhoneCallGraphics();
        model = new PhoneCallModel(Glide.with(PhoneCall.this));
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && model.isFirstStart()) {
            graphics.setContactName(PhoneCall.this, GameData.INSTANCE.updateNames(this, "Lucie Belle"));
            graphics.setImages(PhoneCall.this, model.getRequestManager());
            model.vibrate(2, getSystemService(Context.VIBRATOR_SERVICE));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (model.shouldResumeSound()) {
            playSound();
        }
    }

    /**
     * Sets the Activity's listeners
     */
    private void listeners() {
        Finger.defineOnTouch(
                findViewById(R.id.fz4_phone_call_button_ok),
                PhoneCall.this,
                new Runnable() {
                    @Override
                    public void run() {
                        graphics.hideButtons(PhoneCall.this);
                        graphics.recurseCounter(PhoneCall.this, model.mh);
                        model.generateSound(PhoneCall.this);
                        playSound();
                    }
                }
        );


        Finger.defineOnTouch(
                findViewById(R.id.fz4_phone_call_button_abort),
                PhoneCall.this,
                new Runnable() {
                    @Override
                    public void run() {
                        setResult(RESULT_OK);
                        finish();
                    }
                }
        );
    }

    /**
     * Plays the sound
     */
    private void playSound() {
        model.playSound(new Runnable() {
            @Override
            public void run() {
                graphics.stopCounter(PhoneCall.this, model.mh);
                setResult(RESULT_OK);
                finish();
            }
        });
    }
}
