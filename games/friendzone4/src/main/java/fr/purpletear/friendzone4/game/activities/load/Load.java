package fr.purpletear.friendzone4.game.activities.load;

import com.purpletear.ads.ChapterBreakGate;
import com.example.sharedelements.SutokoSharedElementsData;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import org.jetbrains.annotations.NotNull;

import fr.purpletear.friendzone4.R;
import fr.purpletear.friendzone4.game.config.ChapterDetailsHandler;
import fr.purpletear.friendzone4.game.config.Params;
import purpletear.fr.purpleteartools.TableOfSymbols;


public class Load extends AppCompatActivity {
    /**
     * Handles the model settings
     *
     * @see LoadModel
     */
    private LoadModel model;

    private ChapterBreakGate adGate;
    private int hasSeenChapterNumber = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fz4_activity_load);

        load();
        adGate = new ChapterBreakGate(this, savedInstanceState, 163,
                getIntent().getBooleanExtra("granted", false) || SutokoSharedElementsData.INSTANCE.getSHOULD_FORCE_CHAPTER(), this::navigate);
    }

    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        adGate.save(outState);
        outState.putInt("hasSeenChapterNumber", this.hasSeenChapterNumber);
        outState.putParcelable("params", model.getParams());
        outState.putParcelable("symbols", model.getSymbols());
        outState.putBoolean("hasSeenAudioCinematic", model.hasSeenAudioCinematic);
        outState.putBoolean("hasSeenLoading", model.hasSeenLoading);
        outState.putBoolean("hasSeenTextCinematic", model.hasSeenTextCinematic);
        outState.putBoolean("hasSeenPoetry", model.hasSeenPoetry);
    }

    @Override
    protected void onRestoreInstanceState(@NotNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.hasSeenChapterNumber = savedInstanceState.getInt("hasSeenChapterNumber");
        model.hasSeenAudioCinematic = savedInstanceState.getBoolean("hasSeenAudioCinematic");
        model.hasSeenLoading = savedInstanceState.getBoolean("hasSeenLoading");
        model.hasSeenTextCinematic = savedInstanceState.getBoolean("hasSeenTextCinematic");
        model.hasSeenPoetry = savedInstanceState.getBoolean("hasSeenPoetry");
        model.setParams((Params) savedInstanceState.getParcelable("params"));
        model.setSymbols((TableOfSymbols) savedInstanceState.getParcelable("symbols"));
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_CANCELED) {
            if (data != null && data.hasExtra("params")) {
                setResult(RESULT_OK, new Intent()
                        .putExtra("params", (Parcelable) data.getParcelableExtra("params"))
                        .putExtra("symbols", (Parcelable) data.getParcelableExtra("symbols"))
                );
            } else {
                setResult(RESULT_OK, new Intent()
                        .putExtra("params", (Parcelable) model.getParams())
                        .putExtra("symbols", (Parcelable) model.getSymbols())
                );
            }
            finish();
            return;
        }
        if (requestCode == NavigationHandler.Navigation.TEXTCINEMATIC.ordinal()) {
            model.hasSeenTextCinematic = true;
            if (data != null && data.hasExtra("params")) {
                model.setParams((Params) data.getParcelableExtra("params"));
            }

        } else if (requestCode == NavigationHandler.Navigation.AUDIOCINEMATIC.ordinal()) {
            model.hasSeenAudioCinematic = true;

        } else if (requestCode == NavigationHandler.Navigation.LOADING.ordinal()) {
            model.hasSeenLoading = true;

        } else if (requestCode == NavigationHandler.Navigation.GAME.ordinal()) {
            model.invalidate();
            if (data == null) {
                adGate.childReturned(null, model.getSymbols().getChapterNumber());
                return;
            }
            model.setParams((Params) data.getParcelableExtra("params"));
            model.setSymbols((TableOfSymbols) data.getParcelableExtra("symbols"));

        } else if (requestCode == NavigationHandler.Navigation.POETRY.ordinal()) {
            model.hasSeenPoetry = true;

        }
        adGate.childReturned(data, model.getSymbols().getChapterNumber());
    }

    /**
     * Loads the Activity's vars
     */
    private void load() {
        TableOfSymbols symbols = getIntent().getParcelableExtra("symbols");
        Params params = new Params();
        params.read(this);
        if (symbols.read(this)) {
            params.setChapterCode(symbols.getChapterCode());
        }
        model = new LoadModel(
                params,
                symbols);
    }

    private void navigate() {
        model.getNavigationHandler().to(model.require());

        Intent i = model.getNavigationHandler().getIntent(this);

        i.putExtra("params", (Parcelable) model.getParams());
        i.putExtra("symbols", (Parcelable) model.getSymbols());
        i.putExtra("chapter", ChapterDetailsHandler.getChapter(this, model.getSymbols(), model.getParams().getChapterCode()));
        startActivityForResult(i, model.getNavigationHandler().getRequestCode());
    }

}
