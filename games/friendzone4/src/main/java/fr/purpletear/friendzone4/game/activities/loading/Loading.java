package fr.purpletear.friendzone4.game.activities.loading;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import fr.purpletear.friendzone4.R;
import fr.purpletear.friendzone4.purpleTearTools.MemoryHandler;

public class Loading extends AppCompatActivity {

    /**
     * Handles the graphic settings
     *
     * @see LoadingGraphics
     */
    private LoadingGraphics graphics;

    /**
     * Handles the model and controllers
     *
     * @see LoadingModel
     */
    private LoadingModel model;

    private MemoryHandler mh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fz4_activity_loading);
        load();
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    /**
     * Inits the Activity's vars
     */
    private void load() {
        mh = new MemoryHandler();
        graphics = new LoadingGraphics();
        model = new LoadingModel();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus && model.isFirstStart()) {
            graphics.startLoadAnim(this);
        }

        if(hasFocus) {
            model.goToNextActivity(mh, new Runnable() {
                @Override
                public void run() {
                    graphics.fadeOutLogo(Loading.this, new Runnable() {
                        @Override
                        public void run() {
                            setResult(RESULT_OK);
                            finish();
                        }
                    });
                }
            });
        } else {
            mh.kill();
        }
    }
}
