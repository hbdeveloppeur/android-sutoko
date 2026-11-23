package friendzone3.purpletear.fr.friendzon3;

import com.purpletear.ads.ChapterBreakGate;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sharedelements.SutokoSharedElementsData;

import java.util.ArrayList;
import java.util.List;

import friendzone3.purpletear.fr.friendzon3.handlers.NavigationHandler;
import purpletear.fr.purpleteartools.Std;
import purpletear.fr.purpleteartools.TableOfSymbols;

public class Load extends AppCompatActivity {
    public NavigationHandler navigator;
    public boolean hasSeenCinematic;
    public boolean hasSeenPoetry;
    public TableOfSymbols symbols;
    private ChapterBreakGate adGate;

    private boolean isPremiumGame = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.getIntent() != null) {
            isPremiumGame = this.getIntent().getBooleanExtra("isPremiumGame", false);
        }
        symbols = getIntent().getParcelableExtra("symbols");

        if (SutokoSharedElementsData.INSTANCE.getSHOULD_FORCE_CHAPTER()) {
            symbols.setChapterCode(SutokoSharedElementsData.INSTANCE.getFORCE_CHAPTER_CODE());
            symbols.save(this);
        }

        hasSeenCinematic = false;
        hasSeenPoetry = false;
        setContentView(R.layout.fz3_activity_load);
        navigator = new NavigationHandler();
        adGate = new ChapterBreakGate(this, savedInstanceState, 159,
                getIntent().getBooleanExtra("granted", false) || isPremiumGame || SutokoSharedElementsData.INSTANCE.getSHOULD_FORCE_CHAPTER(), this::navigate);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (resultCode == RESULT_CANCELED) {
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
        adGate.childReturned(data, symbols.getChapterNumber());
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        adGate.save(outState);
        outState.putParcelable("symbols", symbols);
        outState.putBoolean("hasSeenCinematic", hasSeenCinematic);
        outState.putBoolean("hasSeenPoetry", hasSeenPoetry);
        outState.putBoolean("isPremiumGame", this.isPremiumGame);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        hasSeenPoetry = savedInstanceState.getBoolean("hasSeenPoetry");
        hasSeenCinematic = savedInstanceState.getBoolean("hasSeenCinematic");
        symbols = savedInstanceState.getParcelable("symbols");
        isPremiumGame = savedInstanceState.getBoolean("isPremiumGame");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Std.debug("Load.onStart");
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

        Intent i = navigator.getIntent(Load.this);
        i.putExtra("symbols", (Parcelable) symbols);

        startActivityForResult(i, navigator.getDestination().ordinal());
    }

}
