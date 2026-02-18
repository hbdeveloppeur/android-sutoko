package fr.purpletear.friendzone2.activities.poetry;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import fr.purpletear.friendzone2.R;
import purpletear.fr.purpleteartools.Finger;
import purpletear.fr.purpleteartools.TableOfSymbols;

public class Poetry extends AppCompatActivity {
    private PoetryGraphics graphics;
    private PoetryModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poetry);
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
        TableOfSymbols symbols = getIntent().getParcelableExtra("symbols");
        model = new PoetryModel(symbols, Glide.with(Poetry.this));
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
        Finger.Companion.defineOnTouch(
                findViewById(R.id.poetry_button_next),
                this,
                new Runnable() {
                    @Override
                    public void run() {
                        setResult(RESULT_OK);
                        finish();
                    }
                }
        );

    }
}
