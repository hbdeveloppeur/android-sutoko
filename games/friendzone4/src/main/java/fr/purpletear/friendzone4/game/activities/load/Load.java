package fr.purpletear.friendzone4.game.activities.load;

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

    private boolean stop = false;
    private int hasSeenChapterNumber = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fz4_activity_load);

        load();
    }

    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("hasSeenChapterNumber", this.hasSeenChapterNumber);
        outState.putParcelable("params", model.getParams());
        outState.putParcelable("symbols", model.getSymbols());
        outState.putBoolean("hasSeenAudioCinematic", model.hasSeenAudioCinematic);
        outState.putBoolean("hasSeenLoading", model.hasSeenLoading);
        outState.putBoolean("hasSeenTextCinematic", model.hasSeenTextCinematic);
        outState.putBoolean("hasSeenPoetry", model.hasSeenPoetry);
        outState.putBoolean("stop", stop);
    }

    @Override
    protected void onRestoreInstanceState(@NotNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.hasSeenChapterNumber = savedInstanceState.getInt("hasSeenChapterNumber");
        model.hasSeenAudioCinematic = savedInstanceState.getBoolean("hasSeenAudioCinematic");
        model.hasSeenLoading = savedInstanceState.getBoolean("hasSeenLoading");
        model.hasSeenTextCinematic = savedInstanceState.getBoolean("hasSeenTextCinematic");
        model.hasSeenPoetry = savedInstanceState.getBoolean("hasSeenPoetry");
        stop = savedInstanceState.getBoolean("stop");
        model.setParams((Params) savedInstanceState.getParcelable("params"));
        model.setSymbols((TableOfSymbols) savedInstanceState.getParcelable("symbols"));
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (stop) {
            stop = false;
            return;
        }
        this.hasSeenChapterNumber = model.getSymbols().getChapterNumber();
        stop = true;
        navigate();
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
            stop = true;
            finish();
            return;
        }
        if (requestCode == NavigationHandler.Navigation.TEXTCINEMATIC.ordinal()) {
            model.hasSeenTextCinematic = true;
            if (data != null && data.hasExtra("params")) {
                model.setParams((Params) data.getParcelableExtra("params"));
            }
            if (stop) {
                navigate();
            }
        } else if (requestCode == NavigationHandler.Navigation.AUDIOCINEMATIC.ordinal()) {
            model.hasSeenAudioCinematic = true;
            if (stop) {
                navigate();
            }
        } else if (requestCode == NavigationHandler.Navigation.LOADING.ordinal()) {
            model.hasSeenLoading = true;
            if (stop) {
                navigate();
            }
        } else if (requestCode == NavigationHandler.Navigation.GAME.ordinal()) {
            model.invalidate();
            if (data == null) {
                return;
            }
            model.setParams((Params) data.getParcelableExtra("params"));
            model.setSymbols((TableOfSymbols) data.getParcelableExtra("symbols"));
            if (stop) {
                navigate();
            }
        } else if (requestCode == NavigationHandler.Navigation.POETRY.ordinal()) {
            model.hasSeenPoetry = true;
            if (stop) {
                navigate();
            }
        }
    }

    /**
     * Loads the Activity's vars
     */
    private void load() {
        TableOfSymbols symbols = getIntent().getParcelableExtra("symbols");
        Params params = new Params();
        params.read(this);
        params.setChapterCode(symbols.getChapterCode());
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
        startActivityForResult(i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION), model.getNavigationHandler().getRequestCode());
        overridePendingTransition(0, 0);
    }

}
