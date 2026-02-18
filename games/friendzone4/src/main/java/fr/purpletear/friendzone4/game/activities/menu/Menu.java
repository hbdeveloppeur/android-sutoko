package fr.purpletear.friendzone4.game.activities.menu;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import fr.purpletear.friendzone4.R;
import fr.purpletear.friendzone4.factories.Std;
import fr.purpletear.friendzone4.game.activities.load.Load;
import fr.purpletear.friendzone4.purpleTearTools.Finger;

public class Menu extends AppCompatActivity {

    /**
     * Page types.
     * Main - The first page with the buttons (controlAndPlay, reset)
     * INFO - The second page info
     * CHAPTERS - The screen with the chapters.
     */
    enum Page {
        MAIN,
        INFO,
        CHAPTERS
    }

    /**
     * Contains the value of the currentPage type.
     * Used in order to prevent to display a page when it is already done.
     */
    private Page currentPage;

    /**
     * Manages the model structure
     */
    private MenuModel model;

    /**
     * Manages the graphics structure
     */
    private MenuGraphics graphics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Std.hideBars(getWindow(), true, true);
        setContentView(R.layout.fz4_activity_menu_fix);
        load();
        listeners();

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && model.isFirstStart()) {
            graphics();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        graphics.videoState(Menu.this, true);
        model.soundState(Menu.this, true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        graphics.videoState(Menu.this, false);
        model.soundState(Menu.this, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        model.load(Menu.this);
        model.resetCurrentDisplayingChapterNumber();
        updatingContent();
    }

    @Override
    public void onBackPressed() {
        if(currentPage != Page.MAIN) {
            currentPage = graphics.display(Menu.this, Page.MAIN);
            return;
        }
        super.onBackPressed();
    }


    /**
     * Loads the Activity's vars
     */
    private void load() {
        currentPage = Page.MAIN;
        model = new MenuModel(Menu.this, Glide.with(Menu.this));
        model.load(Menu.this);
        graphics = new MenuGraphics();
    }

    /**
     * Manages graphics operations
     */
    private void graphics() {
        long duration = graphics.startLogoAppearance(this);
        graphics.startComponentAppearance(Menu.this, duration);
        graphics.setImages(Menu.this, model.getRequestManager());
        graphics.setInfoContent(Menu.this, model.getInfoContent(Menu.this));
    }

    /**
     * Components that need to be updated
     */
    private void updatingContent() {
        updateNavigation();
    }

    /**
     * Updates the navigation UI
     */
    private void updateNavigation() {
        graphics.setMenuTitle(Menu.this, model.getCurrentDisplayingChapterNumber(), model.getCurrentChapterTitle(this));
        graphics.updateNavigationButtonsText(Menu.this, model.getPreviousChapterNumber(), model.getNextChapterNumber());
        graphics.updateNavigationButtonsAbility(Menu.this, MenuModel.Navigation.PREVIOUS, model.shouldDisplayButton(MenuModel.Navigation.PREVIOUS));
        graphics.updateNavigationButtonsAbility(Menu.this, MenuModel.Navigation.NEXT, model.shouldDisplayButton(MenuModel.Navigation.NEXT));
    }

    /**
     * Sets the Activity's listeners
     */
    private void listeners() {
        Finger.defineOnTouch(
                findViewById(R.id.fz4_menu_button_play),
                Menu.this,
                new Runnable() {
                    @Override
                    public void run() {
                        currentPage = graphics.display(Menu.this, Page.CHAPTERS);
                    }
                }
        );

        Finger.defineOnTouch(
                findViewById(R.id.fz4_menu_button_boutique),
                Menu.this,
                new Runnable() {
                    @Override
                    public void run() {
                        model.openOutlet(Menu.this);
                    }
                }
        );


        Finger.defineOnTouch(
                findViewById(R.id.fz4_menu_main_btn_sound),
                this,
                new Runnable() {
                    @Override
                    public void run() {
                        model.setSoundState(!model.getSoundState());
                        model.soundState(Menu.this, model.getSoundState());
                        graphics.updateSoundStateImage(Menu.this, model.getRequestManager(), model.getSoundState());
                    }
                }
        );

        Finger.defineOnTouch(
                findViewById(R.id.fz4_menu_chapters_previous),
                this,
                new Runnable() {
                    @Override
                    public void run() {
                        model.navigate(MenuModel.Navigation.PREVIOUS);
                        updateNavigation();
                    }
                }
        );

        Finger.defineOnTouch(
                findViewById(R.id.fz4_menu_chapters_next),
                this,
                new Runnable() {
                    @Override
                    public void run() {
                        model.navigate(MenuModel.Navigation.NEXT);
                        updateNavigation();
                    }
                }
        );

        Finger.defineOnTouch(
                findViewById(R.id.fz4_menu_page_chapitres_reset),
                this,
                new Runnable() {
                    @Override
                    public void run() {

                        Std.confirm(
                                Menu.this,
                                R.string.menu_confirm_reset,
                                R.string.ok,
                                R.string.abort,
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        reset();
                                    }
                                }, null);
                    }
                }
        );



        Finger.defineOnTouch(
                findViewById(R.id.fz4_menu_page_chapitres_play),
                this,
                new Runnable() {
                    @Override
                    public void run() {
                        controlAndPlay();
                    }
                }
        );
    }

    /**
     * Resets the game.
     */
    private void reset() {
        model.reset(Menu.this);
        updateNavigation();
        Toast.makeText(getApplicationContext(), getString(R.string.menu_confirm_reset_done), Toast.LENGTH_SHORT).show();
    }

    /**
     * Controls.
     */
    private void controlAndPlay() {
        if(model.userIsNavigating()) {
            confirmChangeChapter();
            return;
        }

        play();
    }


    /**
     * Warns the user that the data will be erased from the chapter MenuModel.currentChapterNumber
     */
    private void confirmChangeChapter() {
        Std.confirm(
                getString(R.string.menu_confirm_play_from, model.getCurrentDisplayingChapterNumber()),
                getString(R.string.ok),
                getString(R.string.abort),
                new Runnable() {
                    @Override
                    public void run() {
                        model.eraseToNavigatingChapter(Menu.this);
                        play();
                    }
                },
                null,
                this
        );
    }


    /**
     * Plays the game.
     */
    private void play() {
        Intent i = new Intent(Menu.this, Load.class);
        i.putExtra("params", (Parcelable) model.getParams());
        i.putExtra("symbols", (Parcelable) model.getSymbols());
        i.putExtra("smart_ads", (Parcelable) getIntent().getParcelableExtra("smart_ads"));
        i.putExtra("granted", getIntent().getBooleanExtra("granted", false));
        startActivity(i);
    }

}
