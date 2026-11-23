package fr.purpletear.friendzone4.game.activities.textcinematic;

import com.purpletear.sutoko.analytics.data.statistics.LegacyChapterReports;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import fr.purpletear.friendzone4.R;
import fr.purpletear.friendzone4.factories.Std;
import fr.purpletear.friendzone4.game.config.AverageTime;
import fr.purpletear.friendzone4.game.config.Params;
import fr.purpletear.friendzone4.purpleTearTools.SoundHandler;
import purpletear.fr.purpleteartools.TableOfSymbols;
import purpletear.fr.purpleteartools.CinematicProgressView;

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
    private CinematicProgressView progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Std.hideBars(getWindow(), true, true);
        setContentView(R.layout.fz4_activity_text_cinematic);
        progress = findViewById(R.id.cinematic_progress);
        try {
            load();
            if (!com.example.sharedelements.SutokoSharedElementsData.INSTANCE.getSHOULD_FORCE_CHAPTER()) {
                LegacyChapterReports.entered(this,163,purpletear.fr.purpleteartools.GameLanguage.Companion.determineLangDirectory(),symbols.getChapterCode());
                LegacyChapterReports.observe(this,163,purpletear.fr.purpleteartools.GameLanguage.Companion.determineLangDirectory(),symbols.getChapterCode());
            }
        } catch (IOException e) {
            e.printStackTrace();
            setResult(RESULT_CANCELED);
            finish();
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
        super.onResume();
        if (model == null) return;
        model.scheduler.resume();
        progress.resume();
        if (model.isFirstStart()) {
            progress.start(model.cinematicDurationMillis(this));
            model.startDiscuss(this);
        }
    }

    @Override
    protected void onPause() {
        if (model != null) model.scheduler.pause();
        progress.pause();
        if (sh != null) sh.pause("bg_end");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (model != null) {
            model.scheduler.clear();
            model.pauseSound();
        }
        progress.stop();
        if (sh != null) sh.clear();
        super.onDestroy();
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
