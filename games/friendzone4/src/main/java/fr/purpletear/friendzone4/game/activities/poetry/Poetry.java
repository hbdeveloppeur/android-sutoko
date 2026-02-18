package fr.purpletear.friendzone4.game.activities.poetry;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import fr.purpletear.friendzone4.R;
import fr.purpletear.friendzone4.game.config.Params;
import fr.purpletear.friendzone4.purpleTearTools.Finger;

public class Poetry extends AppCompatActivity {
    private PoetryGraphics graphics;
    private PoetryModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fz4_activity_poetry);
        load();
        listeners();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus && model.isFirstStart()) {
            graphics();
        }
    }

    /**
     * Loads the Activity's vars
     */
    private void load() {
        Params params = getIntent().getParcelableExtra("params");
        model = new PoetryModel(params.getChapterCode(), Glide.with(Poetry.this));
        graphics = new PoetryGraphics();
    }

    /**
     * Inits graphics
     */
    private void graphics() {
        graphics.setImage(
                Poetry.this,
                model.getRequestManager(),
                model.getBackgroundResourceId(this)
        );
        graphics.setText(Poetry.this, model.getText(this));
    }

    private void listeners() {
        Finger.defineOnTouch(
                findViewById(R.id.fz4_poetry_button_next),
                this,
                new Runnable() {
                    @Override
                    public void run() {
                        setResult(RESULT_OK);
                        finish();
                    }
                }
        );

        Finger.defineOnTouch(
                findViewById(R.id.fz4_poetry_background),
                this,
                new Runnable() {
                    @Override
                    public void run() {
                        graphics.setTextVisibility(Poetry.this, false);
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        graphics.setTextVisibility(Poetry.this, true);
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        graphics.setTextVisibility(Poetry.this, true);
                    }
                }
        );

    }
}
