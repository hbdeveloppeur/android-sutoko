package friendzone3.purpletear.fr.friendzon3;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.ProgressBar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sharedelements.SutokoSharedElementsData;
import com.purpletear.smartads.SmartAdsInterface;
import com.purpletear.smartads.adConsent.AdmobConsent;

import java.util.ArrayList;
import java.util.List;

import friendzone3.purpletear.fr.friendzon3.handlers.NavigationHandler;
import purpletear.fr.purpleteartools.Std;
import purpletear.fr.purpleteartools.TableOfSymbols;

public class Load extends AppCompatActivity implements SmartAdsInterface {
    public NavigationHandler navigator;
    public boolean hasSeenCinematic;
    public boolean hasSeenPoetry;
    public TableOfSymbols symbols;
    private boolean stop = false;
    private boolean isGranted = false;

    private boolean isPremiumGame = false;

    private ActivityResultLauncher<Intent> adActivityResultLauncher = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.getIntent() != null) {
            isPremiumGame = this.getIntent().getBooleanExtra("isPremiumGame", false);
        }
        adActivityResultLauncher = AdmobConsent.Companion.registerActivityResultLauncher(this, this);
        symbols = getIntent().getParcelableExtra("symbols");

        if (SutokoSharedElementsData.INSTANCE.getSHOULD_FORCE_CHAPTER()) {
            symbols.setChapterCode(SutokoSharedElementsData.INSTANCE.getFORCE_CHAPTER_CODE());
            symbols.save(this);
        }

        isGranted = getIntent().getBooleanExtra("granted", false);
        hasSeenCinematic = false;
        hasSeenPoetry = false;
        setContentView(R.layout.fz3_activity_load);
        changeColorSpinner();
        navigator = new NavigationHandler();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (resultCode == RESULT_CANCELED) {
            stop = true;
            finish();
            return;
        }


        if (requestCode == NavigationHandler.Navigation.CINEMATIC.ordinal()) {
            hasSeenCinematic = true;
        } else if (requestCode == NavigationHandler.Navigation.POETRY.ordinal()) {
            hasSeenPoetry = true;
        } else if (requestCode == NavigationHandler.Navigation.GAME.ordinal()) {
            if (data != null && data.hasExtra("symbols")) {
                symbols = data.getParcelableExtra("symbols");
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("symbols", symbols);
        outState.putBoolean("hasSeenCinematic", hasSeenCinematic);
        outState.putBoolean("hasSeenPoetry", hasSeenPoetry);
        outState.putBoolean("isGranted", isGranted);
        outState.putBoolean("stop", stop);
        outState.putBoolean("isPremiumGame", this.isPremiumGame);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        hasSeenPoetry = savedInstanceState.getBoolean("hasSeenPoetry");
        hasSeenCinematic = savedInstanceState.getBoolean("hasSeenCinematic");
        isGranted = savedInstanceState.getBoolean("isGranted");
        symbols = savedInstanceState.getParcelable("symbols");
        stop = savedInstanceState.getBoolean("stop");
        isPremiumGame = savedInstanceState.getBoolean("isPremium");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Std.debug("Load.onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (stop) {
            stop = false;
            return;
        }
        load();
    }

    /**
     * Loads and handles the navigation.
     */
    private void load() {
        if (navigator.toMenu()) {
            finish();
            return;
        }

        ArrayList<Integer> ads = new ArrayList<>();
        ads.add(2);
        ads.add(4);
        ads.add(6);
        ads.add(9);

        if (ads.contains(symbols.getChapterNumber()) && !isGranted && !isPremiumGame && AdmobConsent.Companion.canShowAd()) {
            AdmobConsent.Companion.start(this, this.adActivityResultLauncher);
        } else {
            this.navigate();
        }
    }

    /**
     * Changes the color of the central progress spinner
     */
    private void changeColorSpinner() {

        ((ProgressBar) findViewById(R.id.load_progressbar))
                .getIndeterminateDrawable()
                .setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN);
    }


    private boolean needsToWatchCinematic() {
        List<String> cinematics = new ArrayList<>();
        cinematics.add("1a");
        cinematics.add("9a");
        cinematics.add("9b");
        cinematics.add("9c");
        cinematics.add("9d");
        //noinspection ConstantConditions
        return SutokoSharedElementsData.IS_TEXTCINEMATIC_ENABLED && !hasSeenCinematic
                && navigator.getDestination() != NavigationHandler.Navigation.MENU
                && cinematics.contains(symbols.getChapterCode());
    }

    private boolean isEnd() {
        List<String> ends = new ArrayList<>();
        ends.add("9a");
        ends.add("9b");
        ends.add("9c");
        ends.add("9d");
        return ends.contains(symbols.getChapterCode());
    }

    private boolean needsToWatchPoetry() {
        List<String> poetries = new ArrayList<>();

        poetries.add("5a");
        poetries.add("6a");
        poetries.add("7b");
        //noinspection ConstantConditions
        return SutokoSharedElementsData.IS_POETRY_ENABLED && !hasSeenPoetry
                && navigator.getDestination() != NavigationHandler.Navigation.MENU
                && poetries.contains(symbols.getChapterCode());
    }

    @Override
    public void onAdAborted() {
        finish();
    }

    @Override
    public void onAdSuccessfullyWatched() {
        stop = true;
        navigate();
    }

    @Override
    public void onAdRemovedPaid() {
        navigate();
    }

    private void navigate() {

        if (needsToWatchCinematic()) {
            navigator.to(NavigationHandler.Navigation.CINEMATIC);
        } else if (needsToWatchPoetry()) {
            navigator.to(NavigationHandler.Navigation.POETRY);
        } else {
            if (isEnd()) {
                navigator.to(NavigationHandler.Navigation.CINEMATIC);
            } else {
                navigator.to(NavigationHandler.Navigation.GAME);
                hasSeenPoetry = false;
                hasSeenCinematic = false;
            }
        }

        Intent i = navigator.getIntent(Load.this).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        i.putExtra("symbols", (Parcelable) symbols);

        startActivityForResult(i, navigator.getDestination().ordinal());
    }

    @Override
    public void onErrorFound(@Nullable String code, @Nullable String message, @Nullable String adUnit) {

    }

}
