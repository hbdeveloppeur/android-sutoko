package fr.purpletear.friendzone4.game.activities.textcinematic;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import fr.purpletear.friendzone4.R;
import fr.purpletear.friendzone4.factories.Std;
import fr.purpletear.friendzone4.game.config.AverageTime;
import fr.purpletear.friendzone4.game.config.Params;
import fr.purpletear.friendzone4.purpleTearTools.SoundHandler;
import purpletear.fr.purpleteartools.TableOfSymbols;

public class TextCinematic extends AppCompatActivity {

    /**
     * Handles the graphic settings
     *
     * @see TextCinematicGraphics
     */
    private TextCinematicGraphics graphics;
    /**
     * Handles the data and controllers
     *
     * @see TextCinematicModel
     */
    private TextCinematicModel model;

    private Params params;

    private TableOfSymbols symbols;

    private SoundHandler sh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Std.hideBars(getWindow(), true, true);
        setContentView(R.layout.fz4_activity_text_cinematic);
        try {
            load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    /**
     * Inits the Activity's var
     */
    private void load() throws IOException {
        params = getIntent().getParcelableExtra("params");
        symbols = getIntent().getParcelableExtra("symbols");
        sh = new SoundHandler();
        model = new TextCinematicModel(this, filename(), params.getAverage(AverageTime.Type.SEC), symbols, params);
        graphics = new TextCinematicGraphics();
        model.prepareSound(this);
    }

    @Override
    protected void onResume() {
        if(model.isFirstStart()) {
            model.startDiscuss(this);
        } else {
            model.discuss(this);
        }
        super.onResume();
    }

    @Override
    protected void onStop() {
        model.mh.kill();
        sh.pause("bg_end");
        sh.clear();
        super.onStop();
    }

    private String filename() {

        if(params.getChapterCode().equals("10a")
                || params.getChapterCode().equals("10b")){
            sh.generate("bg_end", this, false).play("bg_end");
            return "11k";
        }

        if(params.getChapterCode().equals("10a")
                || params.getChapterCode().equals("10b")
                || params.getChapterCode().equals("10c")){
            return "11k";
        }

        if(params.getChapterCode().equals("5a")) {
            return "3k";
        }

        if(params.getChapterCode().equals("1a")) {
            return "0a";
        }
        return params.getChapterCode() + "_cinematic";
    }
}
