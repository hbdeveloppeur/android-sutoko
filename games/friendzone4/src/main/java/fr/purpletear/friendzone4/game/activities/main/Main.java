package fr.purpletear.friendzone4.game.activities.main;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sharedelements.SutokoSharedElementsData;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import fr.purpletear.friendzone4.BuildConfig;
import fr.purpletear.friendzone4.R;
import fr.purpletear.friendzone4.custom.Timer;
import fr.purpletear.friendzone4.factories.Data;
import fr.purpletear.friendzone4.factories.Std;
import fr.purpletear.friendzone4.game.activities.hacking.HackingGame;
import fr.purpletear.friendzone4.game.activities.phonecall.PhoneCall;
import fr.purpletear.friendzone4.game.tables.Var;
import fr.purpletear.friendzone4.purpleTearTools.Animation;
import fr.purpletear.friendzone4.purpleTearTools.Finger;
import fr.purpletear.friendzone4.purpleTearTools.Runnable2;
import fr.purpletear.friendzone4.purpleTearTools.Video;
import purpletear.fr.purpleteartools.GlobalData;
import purpletear.fr.purpleteartools.SimpleSound;
import purpletear.fr.purpleteartools.TableOfSymbols;

public class Main extends AppCompatActivity implements MainInterface {

    /**
     * Handles the data settings and controllers
     *
     * @see MainModel
     */
    private MainModel model;

    /**
     * Handles the graphic settings
     *
     * @see MainGraphics
     */
    private MainGraphics graphics;


    /**
     * Prevents the game to  move forward in the discussion
     */
    private boolean preventDefault;

    Timer timer = new Timer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fz4_activity_main);
        load();
        listeners();
    }

    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("recyclerview", model.getRecyclerViewLayoutManager(Main.this).onSaveInstanceState());
        outState.putParcelable("currentPhrase", model.getCurrentPhrase());
        outState.putParcelable("tableOfSymbols", model.getTableOfSymbols());
        outState.putSerializable("currentGameState", model.getCurrentGameState());
        outState.putParcelable("graphics", graphics);
        outState.putParcelableArrayList("phrases", model.getAdapter().getAll());
        outState.putBoolean("beforeDescription", beforeDescription);
    }

    @Override
    protected void onRestoreInstanceState(@NotNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        model.getRecyclerViewLayoutManager(this).onRestoreInstanceState(savedInstanceState.getParcelable("recyclerview"));
        model.setCurrentPhrase((Phrase) savedInstanceState.getParcelable("currentPhrase"));
        model.setTableOfSymbols((TableOfSymbols) savedInstanceState.getParcelable("tableOfSymbols"));
        model.setCurrentGameState((MainModel.GameState) savedInstanceState.getSerializable("currentGameState"));
        graphics = savedInstanceState.getParcelable("graphics");
        beforeDescription = savedInstanceState.getBoolean("beforeDescription");
        model.getAdapter().setArray(savedInstanceState.<Phrase>getParcelableArrayList("phrases"));
    }

    private boolean beforeDescription = true;

    @Override
    protected void onStart() {
        super.onStart();
        if (beforeDescription) {
            beforeDescription();
            beforeDescription = false;
        }
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && model.isFirstStart()) {
            graphics();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        resume();
        if (graphics.isBlackScreen) {
            View header_black_filter = findViewById(R.id.fz4_mainactivity_header_black_filter);
            View black_background = findViewById(R.id.fz4_mainactivity_glitch_black_filter);
            graphics.setVisibilityBlackScreen(this, header_black_filter, black_background, false);
        }
    }

    @Override
    public void onBackPressed() {
        if (model.getCurrentGameState() == MainModel.GameState.DESCRIPTION) {
            Main.super.onBackPressed();
            return;
        }
        Std.confirm(
                Main.this,
                R.string.main_confirm_leave,
                R.string.ok,
                R.string.abort,
                new Runnable() {
                    @Override
                    public void run() {
                        Main.super.onBackPressed();
                    }
                },
                null
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == model.getHackGameRequestCode()) {
            if (resultCode == RESULT_CANCELED) {
                setResult(RESULT_CANCELED);
                finish();
            } else {
                model.moveForward();
            }
        }

        if (requestCode == model.getPhoneCallingRequestCode()) {
            if (resultCode == RESULT_CANCELED) {
                setResult(RESULT_CANCELED);
                finish();
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        pause(false);
    }

    @Override
    protected void onDestroy() {
        model.clear();
        super.onDestroy();
    }

    /**
     * Inits the Activity's vars
     *
     * @see MainGraphics
     * @see MainModel
     */
    private void load() {
        preventDefault = false;
        model = new MainModel(
                this,
                getAssets(),
                getIntent().getParcelableExtra("params"),
                getIntent().getParcelableExtra("symbols"),
                Glide.with(this),
                this);
        graphics = new MainGraphics(
                model.getChapterConversationName(this),
                model.getChapterConversationStatus(this),
                "",
                "DEFINED",
                true,
                false,
                false,
                false,
                false,
                true,
                model.getProfilPictureResourceId(this),
                "fz4_not_found",
                ""

        );
        graphics.setRecyclerView(this, model.getAdapter(), getWindowManager().getDefaultDisplay());
        graphics.setContactRecyclerView(this, model.getContactAdapter(this, this), getWindowManager().getDefaultDisplay());
        graphics.focusTrash(this);
    }

    /**
     * Inits the graphic components.
     *
     * @see MainGraphics
     */
    private void graphics() {
        if (model.getParams().getChapterNumber() == 10) {
            graphics.setLostImage(Main.this, model.getRequestManager());
        }

        graphics.setFasterButtonVisibility(this, model.getRequestManager(), false, model.shouldShowFasterBtn());
        graphics.setImages(this, model.getRequestManager(), !model.isNoSeen());
        graphics.setTitle(this, model.getFormatedChapterTitle(this));
        graphics.setDescription(this, model.getChapterDescription(this));
        graphics.setConversationName(this);
        graphics.setConversationStatus(this);
        graphics.setConversationImage(this, model.getRequestManager());
        graphics.fadeOutBlackFilter(this);
        graphics.setButtonVisibility(this);
        graphics.switchBackgroundImage(Main.this, model.getRequestManager());
        graphics.setHeaderAlpha(Main.this,
                model.isNoSeen()
        );
        graphics.setButtonAlpha(Main.this, model.isNoSeen());

    }

    /**
     * Delays the description if needed
     *
     * @see MainModel
     * @see MainGraphics
     */
    private void resume() {
        if (!graphics.getVideoToReload().equals("")) {
            switchVideo(graphics.getVideoToReload());
        }
        if (model.getCurrentGameState() != MainModel.GameState.DESCRIPTION
                && model.getCurrentGameState() != MainModel.GameState.USER_PAUSED) {
            graphics.hideDescription(Main.this);

            if (!preventDefault) {
                discuss();
            }
        }
    }

    /**
     * Sets the Activity's listeners.
     */
    private void listeners() {


        Finger.defineOnTouch(
                findViewById(R.id.fz4_main_button_continue),
                this,
                new Runnable() {
                    @Override
                    public void run() {
                        graphics.fadeDescription(Main.this);
                        model.setCurrentGameState(MainModel.GameState.PLAYING);
                        // console();
                        discuss();
                    }
                }
        );

        Finger.defineOnTouch(
                findViewById(R.id.fz4_mainactivity_button_faster_build),
                this,
                new Runnable() {
                    @Override
                    public void run() {
                        model.mh.kill();
                        if (model.getCurrentPhrase().isHesitate()) {
                            model.getAdapter().removeIfLastIs(Phrase.Type.typing);
                            discussForward(model.getCurrentPhrase(), true);
                        } else if (model.getAdapter().lastIs(Phrase.Type.typing)) {
                            model.getAdapter().editLastIfIs(Phrase.Type.typing, Phrase.Type.dest);
                        } else {
                            discussForward(model.getCurrentPhrase(), true);
                        }
                    }
                }
        );


        Finger.defineOnTouch(
                findViewById(R.id.fz4_mainactivity_button_pause),
                this,
                new Runnable() {
                    @Override
                    public void run() {
                        switch (model.getCurrentGameState()) {
                            case PLAYING:
                                pause(true);
                                break;
                            case USER_PAUSED:
                                model.setCurrentGameState(MainModel.GameState.PLAYING);
                                model.getAdapter().removeIfLastIs(Phrase.Type.paused);
                                model.getAdapter().removeIfLastIs(Phrase.Type.nextChapter);
                                discuss(model.getCurrentPhrase());
                                break;
                        }
                        graphics.updateGameStateButton(Main.this, model.getRequestManager(), model.getCurrentGameState());
                    }
                }
        );
        Finger.defineOnTouch(
                findViewById(R.id.fz4_mainactivity_button),
                this,
                new Runnable() {
                    @Override
                    public void run() {
                        openChoiceBox();
                        timer.start();
                        graphics.setChoiceBoxVisibility(Main.this, true);
                        if (model.isTimedChoice()) {
                            graphics.setTimedChoiceVisibility(Main.this, true);

                            ValueAnimator v = graphics.enableTimedChoiceAnimation(
                                    Main.this,
                                    model.getTimedChoiceTime());
                            model.setTimeChoiceProgressBarValueAnimator(v);
                            graphics.countAnimation(
                                    Main.this,
                                    model.mh,
                                    model.getTimedChoiceTime(),
                                    Main.this,
                                    model.getRandomChoice()

                            );
                        }
                    }
                }
        );

        Finger.defineOnTouch(
                findViewById(R.id.fz4_mainactivity_choicebox_area),
                Main.this,
                new Runnable() {
                    @Override
                    public void run() {
                        timer.end();
                        graphics.setChoiceAreaVisibility(Main.this, false
                        );
                    }
                }
        );
    }

    private void discuss() {
        discuss(model.getCurrentPhrase(), false);
    }

    private void discuss(Phrase p) {
        discuss(p, false);
    }

    /**
     * If in anyway, the current item is already inside the reyclcer view, it shouldn't be inserted again
     *
     * @param p Phrase
     * @return boolean: True if the method has changed the currentPhrase
     */
    private boolean logicalized(Phrase p) {
        if (model.getAdapter().getItemCount() == 0) {
            return false;
        }
        if (Phrase.determineTypeEnum(model.getAdapter().getLastItem().getType()) == Phrase.Type.nextChapter) {
            model.getAdapter().removeIfLastIs(Phrase.Type.nextChapter);
            discuss(model.getCurrentPhrase());
            return true;
        }
        if (p.equals(model.getAdapter().getLastItem())) {
            discussForward(p);
            return true;
        }
        return false;
    }

    /**
     * Manage a sentence and run the required actions
     *
     * @param p Phrase
     * @see Phrase
     */
    private void discuss(final Phrase p, final boolean isFast) {
        if (logicalized(p)) {
            // let it empty
            return;
        }
        model.formatName(this, p);
        p.formatVars(this, model.getTableOfSymbols());
        graphics.setButtonVisibility(Main.this, false);

        //noinspection AccessStaticViaInstance,ConstantConditions
        if (SutokoSharedElementsData.IS_FAST_CHAPTER) {
            p.setSeen(0);
            p.setWait(0);
        }

        if (DiscussionHandler.execute("Fin du jeu", p.isEnd())) {
            model.getParams().setChapterCode(p.getNextChapter().toLowerCase());
            model.getTableOfSymbols().setChapterCode(p.getNextChapter().toLowerCase());
            model.getParams().save(Main.this);
            model.getTableOfSymbols().save(Main.this);
            setResult(RESULT_OK, new Intent()
                    .putExtra("params", (Parcelable) model.getParams())
                    .putExtra("symbols", (Parcelable) model.getTableOfSymbols())
            );
            finish();
            return;
        }

        if (DiscussionHandler.execute("Lancement d'un nouveau chapitre", p.isNextChapter())) {
            final String chapter = p.getNextChapter();
            model.getAdapter().insertPhrase(p, Phrase.Type.nextChapter);
            Runnable2 runnable = new Runnable2("Nouveau chapitre", Phrase.nextChapterDelay()) {
                @Override
                public void run() {
                    long duration = Animation.setAnimation(
                            findViewById(R.id.fz4_main_main_filter_black),
                            Animation.Animations.ANIMATION_FADEIN,
                            Main.this
                    );
                    Runnable2 runnable = new Runnable2("fadeIn black filter", duration) {
                        @Override
                        public void run() {
                            model.getParams().setChapterCode(chapter);
                            model.getTableOfSymbols().setChapterCode(chapter);
                            model.getParams().save(Main.this);
                            model.getTableOfSymbols().save(Main.this);
                            setResult(RESULT_OK, new Intent()
                                    .putExtra("params", (Parcelable) model.getParams())
                                    .putExtra("symbols", (Parcelable) model.getTableOfSymbols())
                            );
                            finish();
                        }
                    };
                    model.mh.push(runnable);
                    model.mh.run(runnable);
                }
            };
            model.mh.push(runnable);
            model.mh.run(runnable);
            return;
        }

       /* if (DiscussionHandler.execute("Un personnage s'est déconnecté", p.isOffline())) {
            Character c = model.getTableOfCharacters().getCharacter(p.getId_author());
            p.setSentence(this, getString(R.string.is_disconnected, c.getName()));
            model.getAdapter().insertPhrase(p, Phrase.Type.info);
            discussForward(p);
            graphics.status(Main.this, false);
            return;
        }

        if (DiscussionHandler.execute("Un personnage a banni le joueur", p.isBan())) {
            p.setSentence(this, getString(R.string.mainactivity_banned));
            model.getAdapter().insertPhrase(p, Phrase.Type.info);
            graphics.setStatusTextAndStyle(Main.this, getString(R.string.got_banned), R.color.colorSoftRed);
            discussForward(p);
            return;
        } */

        if (DiscussionHandler.execute("Un personnage hésite", p.isHesitate())) {

            Runnable2 runnable = new Runnable2("Un personnage hésite", p.getSeen()) {
                @Override
                public void run() {
                    Runnable2 runnable = new Runnable2("Un personnage hésite", p.getWait()) {
                        @Override
                        public void run() {
                            model.getAdapter().removeIfLastIs(Phrase.Type.typing);
                            discussForward(p);
                        }
                    };
                    if (p.getSeen() > 0) {
                        model.getAdapter().insertPhrase(p, Phrase.Type.typing);
                        model.playSound(Main.this, "typing", false);
                    }

                    model.mh.push(runnable);
                    model.mh.run(runnable);
                }
            };

            model.mh.push(runnable);
            model.mh.run(runnable);
            return;
        }

        if (DiscussionHandler.execute("Aller à la phrase d'identifiant X", p.isJumpToId())) {
            discuss(model.getTableOfPhrases().getPhrase(p.getJumpToId()));
            return;
        }

        if (DiscussionHandler.execute("Action : Une personne vous a accepté dans sa liste d'amis",
                p.is(Phrase.Type.action) && p.isFriendAcceptNotification())) {
            /*Character c = model.getTableOfCharacters().getCharacter(p.getId_author());
             notification(getString(R.string.notification_friend_name, c.getFirst_name(), c.getLast_name()),
                    getString(R.string.accepted_you_to_its_friendslist),
                    R.drawable.add_friend, c.getIdImage());
            animeNotification(new Runnable() {
                @Override
                public void run() {
                    discussForward(p);
                }
            }, false);*/
            return;
        }

        if (DiscussionHandler.execute("Action : Une personne vous a ajouté à sa liste d'amis",
                p.is(Phrase.Type.action) && p.isFriendNotification())) {
            /*Character c = tableOfCharacters.getCharacter(p.getId_author());
            notification(getString(R.string.notification_friend_name, c.getFirst_name(), c.getLast_name()),
                    getString(R.string.added_you_to_its_friendslist),
                    R.drawable.add_friend, c.getIdImage());
            animeNotification(new Runnable() {
                @Override
                public void run() {
                    discussForward(p);
                }
            }, false);*/
            return;
        }

        if (DiscussionHandler.execute("Verification d'une condition", p.is(Phrase.Type.condition))) {
            String values[] = p.getAnswerCondition();
            String condition[] = values[0].replace("[", "").replace("]", "").replace(" ", "").split("==");
            int mThen = Integer.parseInt(values[1]);
            int mElse = Integer.parseInt(values[2]);

            Var v = new Var(condition[0], condition[1], model.getChapterNumber());
            Phrase next;
            if (model.getTableOfSymbols().condition(GlobalData.Game.FRIENDZONE4.getId(), v.getName(), v.getValue())) {
                next = model.getTableOfPhrases().getPhrase(mThen);
            } else {
                next = model.getTableOfPhrases().getPhrase(mElse);
            }
            model.setCurrentPhrase(next);

            discuss(next);
            return;
        }

        if (DiscussionHandler.execute("Insertion d'une variable en mémoire", p.is(Phrase.Type.memory))) {
            Var v = p.getVarFromCondition(model.getChapterNumber());

            model.getTableOfSymbols().addOrSet(GlobalData.Game.FRIENDZONE4.getId(), v.getName(), v.getValue(), model.getChapterNumber());

            discussForward(p);
            return;
        }

        if (DiscussionHandler.execute("Un personnage envoie une image", p.isContentImage())) {

            Runnable2 runnable = new Runnable2("Un personnage envoie une image", p.getSeen()) {
                @Override
                public void run() {
                    model.getAdapter().insertPhrase(p, Phrase.Type.image, !isFast);
                    discussForward(p);
                }
            };
            model.mh.push(runnable);
            model.mh.run(runnable);


            return;
        }

        if (DiscussionHandler.execute("Le joueur débloque un trophée", p.isTrophy())) {

            Runnable2 runnable = new Runnable2("Un personnage envoie une image", p.getSeen()) {
                @Override
                public void run() {
                    if (!model.collectedTrophies.containsByTrophyId(p.getTrophyId())) {
                        model.collectedTrophies.add(Main.this, p.getTrophyId(), GlobalData.Game.FRIENDZONE4.getId(), BuildConfig.VERSION_CODE);
                        model.collectedTrophies.save(Main.this);
                        try {
                            SimpleSound sh = new SimpleSound();
                            sh.prepareAndPlay(Main.this, com.example.sharedelements.R.raw.deduction, false, 0);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        model.getAdapter().insertPhrase(p, Phrase.Type.trophy, !isFast);
                    }
                    discussForward(p);
                }
            };
            model.mh.push(runnable);
            model.mh.run(runnable);
            return;
        }


        if (DiscussionHandler.execute("Affichage d'une date", p.is(Phrase.Type.date))) {
            Runnable2 runnable = new Runnable2("Affichage d'une date", p.getSeen()) {
                @Override
                public void run() {
                    model.getAdapter().insertPhrase(p, Phrase.Type.date, !isFast);
                    discussForward(p);
                }
            };

            model.mh.push(runnable);
            model.mh.run(runnable);

            return;
        }

        if (DiscussionHandler.execute("Affichage d'une information", p.is(Phrase.Type.info))) {
            Runnable2 runnable = new Runnable2("Affichage d'une information", p.getSeen()) {
                @Override
                public void run() {
                    model.getAdapter().insertPhrase(p, Phrase.Type.info, !isFast);
                    discussForward(p);
                }
            };

            model.mh.push(runnable);
            model.mh.run(runnable);
            return;
        }

        if (DiscussionHandler.execute("Demande de lancement de code", p.isCode())) {
            Runnable2 runnable = new Runnable2("Affichage d'une information", p.getSeen()) {
                @Override
                public void run() {
                    managerCode(p, p.getCode());
                }
            };

            model.mh.push(runnable);
            model.mh.run(runnable);
            return;
        }

        if (DiscussionHandler.execute("Demande de notification overlay", p.isOverlayNotification())) {
            Runnable2 runnable = new Runnable2("Demande de notification overlay + ", p.getSeen()) {
                @Override
                public void run() {
                    graphics.setOverlayColor(Main.this, p.getOverlayNotifColorId());
                    graphics.setOverlaySentence(Main.this, p.getOverlayNotifSentence());
                    graphics.setOverlayTextColor(Main.this, p.getOverlayNotifTextColorId());
                    discussForward(p);
                }
            };

            model.mh.push(runnable);
            model.mh.run(runnable);
            return;
        }

        if (DiscussionHandler.execute("Demande d'affichage d'un gif", p.isGif())) {
            Runnable2 runnable = new Runnable2("Demande de notification overlay + ", p.getSeen()) {
                @Override
                public void run() {
                    model.getAdapter().insertPhrase(p, Phrase.Type.gif, !isFast);
                    discussForward(p);
                }
            };

            model.mh.push(runnable);
            model.mh.run(runnable);
            return;
        }

        if (DiscussionHandler.execute("Lancement d'un son", p.isSound())) {
            Runnable2 runnable = new Runnable2("Demande de notification overlay + ", p.getSeen()) {
                @Override
                public void run() {
                    model.playSound(Main.this, p.getSoundName(), false);
                    discussForward(p);
                }
            };

            model.mh.push(runnable);
            model.mh.run(runnable);
            return;
        }

        if (DiscussionHandler.execute("Affichage d'une image en fond", p.isBackgroundImage())) {
            Runnable2 runnable = new Runnable2("Demande de changement d'image de fond ", p.getSeen()) {
                @Override
                public void run() {
                    graphics.switchBackgroundImage(Main.this, model.getRequestManager(), Data.getImage(Main.this, "fz4_" + p.getBackgroundImageName()));
                    discussForward(p);
                }
            };

            model.mh.push(runnable);
            model.mh.run(runnable);
            return;
        }

        if (DiscussionHandler.execute("Choix à durée limitée", p.isTimedChoice())) {
            int value = p.getTimedChoiceTime();
            model.setTimedChoice(value);
            graphics.setTimedChoiceVisibility(Main.this, true);
            model.updateTimedChoiceListener(Main.this, true, null);
            discussForward(p);
            return;
        }

        if (DiscussionHandler.execute("Thunderstorm effect", p.isThunder())) {
            model.playSound(Main.this, "thunder", false);
            discussForward(p);
            return;
        }

        if (DiscussionHandler.execute("Changement du stauts de conversation", p.isConversationStatusChange())) {

            Runnable2 runnable = new Runnable2("Changement du stauts de conversation", p.getSeen()) {
                @Override
                public void run() {
                    graphics.setConversationStatus(Main.this, p.getConversationStatusChange());
                    discussForward(p);
                }
            };

            model.mh.push(runnable);
            model.mh.run(runnable);
            return;
        }

        if (DiscussionHandler.execute("Insertion d'un message de type Type.me", p.isMe())) {
            p.setSentence(this, p.getMe());
            model.getAdapter().insertPhrase(p, Phrase.Type.me);
            discussForward(p);
            return;
        }

        DiscussionHandler.execute("Eva parle", new Runnable() {
            @Override
            public void run() {

                if (p.getSeen() == 0 && p.getWait() == 0) {
                    model.getAdapter().insertPhrase(p, Phrase.Type.dest);
                    discussForward(p);
                    return;
                }

                Runnable2 runnable = new Runnable2("Eva parle", p.getSeen()) {
                    @Override
                    public void run() {
                        if (p.getWait() > 0 && !model.isNoSeen()) {
                            model.getAdapter().insertPhrase(p, Phrase.Type.typing, !isFast);
                            model.playSound(Main.this, "typing", false);
                        }

                        Runnable2 runnable = new Runnable2("Typing", p.getWait()) {
                            @Override
                            public void run() {
                                p.setSentence(Main.this, Emojis.translate(p.getSentence()));
                                if (p.getWait() > 0 && !model.isNoSeen()) {
                                    model.getAdapter().editLast(p, Phrase.Type.dest);
                                } else {
                                    model.getAdapter().insertPhrase(p, Phrase.Type.dest, !isFast);
                                }
                                if (!model.isNoSeen()) {
                                    model.playSound(Main.this, "message", false);
                                }
                                discussForward(p);
                            }
                        };
                        model.mh.push(runnable);
                        model.mh.run(runnable);
                    }
                };

                model.mh.push(runnable);
                model.mh.run(runnable);
            }
        });
    }


    private void discussForward(Phrase p) {
        discussForward(p, false);
    }

    /**
     * Continues in the discussion if the given phrase has an answer.
     *
     * @param p Phrase
     */
    private void discussForward(Phrase p, boolean isFast) {
        if (model.getTableOfLinks().answerIsUserChoice(p.getId(), model.getTableOfPhrases())) {
            model.setCurrentGameState(MainModel.GameState.WAITING_FOR_USER);
            graphics.setButtonVisibility(Main.this, true);
            return;
        }

        if (model.getTableOfLinks().hasAnswer(p.getId())) {
            int nextId = model.getTableOfLinks().getDest(p.getId()).get(0);
            Phrase next = model.getTableOfPhrases().getPhrase(nextId);
            if (isFast) {
                next.setSeen(0);
                next.setWait(0);
            }
            model.setCurrentPhrase(
                    next
            );
            discuss(model.getCurrentPhrase(), isFast);
        }
    }

    @Override
    public void onClickChoice(Phrase p) {

        model.updateTimedChoiceListener(
                Main.this,
                false,
                new Runnable() {
                    @Override
                    public void run() {
                        graphics.setChoiceAreaVisibility(Main.this, false);
                    }
                }
        );
        model.mh.removeAll("countAnimation");
        model.clearTimedChoice();
        model.disableTimedChoice();
        timer.end();
        model.updateAverageTime((int) timer.result());
        graphics.setTimedChoiceVisibility(Main.this, false);
        graphics.setButtonVisibility(Main.this, false);
        graphics.setChoiceAreaVisibility(Main.this, false);
        model.setCurrentGameState(MainModel.GameState.PLAYING);

        Phrase tmp = p;
        int seen = 0;
        do {
            tmp.setSentence(this, Emojis.translate(tmp.withoutInfo()));
            model.getAdapter().insertPhrase(tmp, Phrase.Type.me);
            ArrayList<Integer> links = model.getTableOfLinks().getDest(tmp.getId());
            if (links.size() == 0) {
                model.mh.kill();
                discuss();
                return;
            }
            int nextId = links.get(0);
            if (tmp.getId_author() == 0) {
                seen = tmp.getSeen();
            }
            tmp = model.getTableOfPhrases().getPhrase(nextId);
            tmp.setSentence(this, Emojis.translate(tmp.getSentence()));
        } while (tmp.getId_author() == 0);

        final Phrase ftmp = tmp;
        model.setCurrentPhrase(ftmp);

        if (!model.isNoSeen()) {
            //noinspection ConstantConditions
            Runnable2 runnable = new Runnable2("meSeen", (SutokoSharedElementsData.IS_FAST_CHAPTER ? 0 : seen)) {
                @Override
                public void run() {
                    model.setLastSeenIf(Phrase.Type.me);
                    discuss(ftmp);
                }
            };

            model.mh.push(runnable);
            model.mh.run(runnable);
        } else {
            discuss(ftmp);
        }

    }

    @Override
    public void onInsertPhrase(int position, boolean isSmoothScroll) {
        graphics.scrollToPosition(Main.this, (RecyclerView) findViewById(R.id.fz4_mainActivity_recyclerview_fix), position, isSmoothScroll);
    }

    @Override
    public void onClickContact(String code, Contact.Type type) {
        managerCode(null, code);
    }

    @Override
    public void onClickItem(String type, String name) {

    }

    @Override
    public void onMissedChoice(Phrase p) {
        if (!model.isTimedChoice()) {
            return;
        }
        model.getAdapter().insertPhrase(p, Phrase.Type.me);
        graphics.setButtonVisibility(Main.this, false);
        graphics.setChoiceAreaVisibility(Main.this, false);
        graphics.setTimedChoiceVisibility(
                Main.this,
                false
        );
        model.disableTimedChoice();
        model.updateTimedChoiceListener(
                Main.this,
                false,
                new Runnable() {
                    @Override
                    public void run() {
                        graphics.setChoiceAreaVisibility(Main.this, false);
                    }
                }
        );
        discussForward(p);
    }

    @Override
    public void onTouchJoystick() {
        graphics.setRecyclerViewScrollingState(Main.this, false);
    }

    @Override
    public void onReleaseJoystick() {
        graphics.setRecyclerViewScrollingState(Main.this, true);
    }

    private int hits = 0;

    @Override
    public void onJoystickInfoHit(long start, String code) {
        hits++;
        int limit = 15;
        Std.vibrate(getSystemService(Context.VIBRATOR_SERVICE));

        if (hits == limit) {
            long time = (System.currentTimeMillis() - start);
            if ((code.equals("9c3") || code.equals("9c4")) && time >= 4500) {
                model.getTableOfSymbols().addOrSet(GlobalData.Game.FRIENDZONE4.getId(), "bryanHurtMe", "true");
            }
            model.getAdapter().removeIfLastIs(Phrase.Type.minigame);
            discussForward(model.getCurrentPhrase());
            preventDefault = false;
        }
        if (hits >= limit) {
            hits = 0;
        }

    }

    /**
     * Opens the choice box
     *
     * @see ChoicesController
     */
    private void openChoiceBox() {
        ChoicesController.clear(graphics.getChoiceParentView(Main.this));
        graphics.setChoiceAreaVisibility(Main.this, true);
        ChoicesController.choose(Main.this,
                graphics.getChoiceParentView(Main.this),
                model.getChoices(),
                this,
                model.getTableOfSymbols(),
                model.getTableOfSymbols().getGlobalFirstName());
    }

    /**
     * Manages and handles the action codes.
     * An action code is a phrase with a sentence like -> [CODE_(a-zA-Z0-9)+]
     * The extracted code is treaten in a switch
     * For instance, [CODE_1a1] is a code for the chapter 1a code number 1
     * and the extracted code is 1a1
     *
     * @param code String
     * @see Phrase
     */
    private void managerCode(@Nullable final Phrase p, String code) {
        switch (code) {
            case "1a1": {
                model.playSound(Main.this, "bug", false);
                graphics.glitchit(Main.this, model.mh, new Runnable() {
                    @Override
                    public void run() {
                        discussForward(model.getCurrentPhrase());
                    }
                });
                break;
            }

            case "2a2": {
                graphics.setConversationName(Main.this, "Zoé Topaze");
                graphics.setConversationImage(Main.this, Data.getImage(this, model.getTableOfCharacters().getCharacter(28).getImageId()), Glide.with(Main.this));
                discussForward(model.getCurrentPhrase());
                break;
            }

            case "3a2": {
                if (p == null) {
                    throw new NullPointerException();
                }
                graphics.setOverlayImage(Main.this, model.getRequestManager(), Data.getImage(this, "fz4_candle"));
                graphics.setOverlayColor(Main.this, p.getOverlayNotifColorId());
                graphics.setOverlaySentence(Main.this, model.getOverlayNotificationFirstSentence(Main.this));
                Runnable2 runnable = new Runnable2("a", p.getSeen()) {
                    @Override
                    public void run() {
                        int duration = (int) graphics.animateOverlayNotification(Main.this);
                        Runnable2 runnable = new Runnable2("b", duration) {
                            @Override
                            public void run() {
                                discussForward(p);
                            }
                        };
                        model.mh.push(runnable);
                        model.mh.run(runnable);
                    }
                };
                model.mh.push(runnable);
                model.mh.run(runnable);
                break;
            }

            case "3a3": {
                Finger.defineOnTouch(
                        findViewById(R.id.fz4_mainactivity_overlay),
                        Main.this,
                        new Runnable() {
                            @Override
                            public void run() {
                                final long duration = Animation.setAnimation(
                                        findViewById(R.id.fz4_main_main_filter_black),
                                        Animation.Animations.ANIMATION_FADEIN,
                                        Main.this
                                );
                                Runnable2 runnable = new Runnable2("CODE_3a3", duration) {
                                    @Override
                                    public void run() {
                                        model.clearRecyclerView();
                                        graphics.setOverlayNotificationVisibility(Main.this, false);
                                        graphics.setConversationName(Main.this, getString(R.string.horror_night));
                                        graphics.setConversationImage(Main.this, Data.getImage(Main.this, "fz4_candle"), model.getRequestManager());
                                        switchVideo("horror");
                                        Runnable2 runnable = new Runnable2("CODE_3a3/2", duration) {
                                            @Override
                                            public void run() {
                                                discussForward(p);
                                                Animation.setAnimation(
                                                        findViewById(R.id.fz4_main_main_filter_black),
                                                        Animation.Animations.ANIMATION_FADEOUT,
                                                        Main.this
                                                );
                                            }
                                        };
                                        model.mh.push(runnable);
                                        model.mh.run(runnable);
                                    }
                                };
                                model.mh.push(runnable);
                                model.mh.run(runnable);
                            }
                        }
                );
                break;
            }

            case "3a4": {
                if (model.shouldShowFasterBtn()) {
                    discussForward(p);
                    return;
                }
                graphics.setFasterButtonVisibility(Main.this, model.getRequestManager(), true, true);
                discussForward(p);
                break;
            }

            case "3a5": {
                graphics.setConversationStatus(Main.this, "Zoé, " + model.getTableOfSymbols().getFirstName());
                discussForward(p);
                break;
            }

            case "3a6": {
                graphics.setConversationStatus(Main.this, model.getTableOfSymbols().getFirstName());
                discussForward(p);
                break;
            }


            case "3a7": {
                graphics.setContactOptionIcon(Main.this, model.getRequestManager());
                graphics.setContactName(Main.this, model.getTableOfSymbols().getFirstName());
                graphics.animateContact(Main.this, true, null);
                break;
            }

            case "3a8": {
                graphics.setConversationStatus(Main.this, "Eva, " + model.getTableOfSymbols().getFirstName());
                discussForward(p);
                break;
            }

            case "3a10": {
                if (model.shouldShowFasterBtn()) {
                    discussForward(p);
                    return;
                }
                graphics.setFasterButtonVisibility(Main.this, model.getRequestManager(), true, false);
                discussForward(p);
                break;
            }

            case "3a11": {
                model.getTableOfSymbols().addOrSet(GlobalData.Game.FRIENDZONE4.getId(), "barWithZoe", "true");
                graphics.animateContact(Main.this, false, null);
                discussForward(model.getCurrentPhrase());
                break;
            }

            case "3a12": {
                model.getTableOfSymbols().addOrSet(GlobalData.Game.FRIENDZONE4.getId(), "barWithZoe", "false");
                graphics.animateContact(Main.this, false, null);
                discussForward(model.getCurrentPhrase());
                break;
            }

            case "6a1": {
                switchVideo("background_lake");
                discussForward(model.getCurrentPhrase());
                break;
            }

            case "6a2": {
                graphics.setConversationImage(Main.this, Data.getImage(this, "fz4_lucie2"), model.getRequestManager());
                discussForward(model.getCurrentPhrase());
                break;
            }

            case "8a1": {
                model.moveForward();
                startActivityForResult(new Intent(Main.this, HackingGame.class), model.getHackGameRequestCode());
                break;
            }

            case "8a2": {
                model.moveForward();
                startActivityForResult(new Intent(Main.this, HackingGame.class), model.getHackGameRequestCode());
                break;
            }

            case "8b1": {
                graphics.setFilterAlpha(Main.this, .65f, 10000);
                discussForward(model.getCurrentPhrase());
                break;
            }

            case "9a1": {
                switchVideo("background_lake_night");
                discussForward(model.getCurrentPhrase());
                break;
            }

            case "9a2": {
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                if (v != null && v.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= 26) {
                        v.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        v.vibrate(150);
                    }
                }
                discussForward(model.getCurrentPhrase());
                break;
            }


            case "9a3": {
                Intent i = new Intent(Main.this, HackingGame.class);
                i.putExtra("isEnd", true);
                startActivityForResult(i, model.getHackGameRequestCode());
                break;
            }

            case "9a4": {
                model.moveForward();
                startActivityForResult(new Intent(Main.this, PhoneCall.class), model.getPhoneCallingRequestCode());
                break;
            }

            case "9a99": {
                graphics.blackFilter(Main.this, 2000, true);
                Runnable2 runnable = new Runnable2("9a99", 2000) {
                    @Override
                    public void run() {
                        discussForward(model.getCurrentPhrase());
                    }
                };
                model.mh.push(runnable);
                model.mh.run(runnable);
                break;
            }

            case "9c0": {
                switchVideo("thunder");
                discussForward(model.getCurrentPhrase());
                break;
            }

            case "9c1": {
                model.getAdapter().removeIfLastIs(Phrase.Type.date);
                discussForward(model.getCurrentPhrase());
                break;
            }

            case "9c2": {
                preventDefault = true;
                Phrase p1 = new Phrase(Phrase.Type.minigame);
                p1.setSentence(this, getString(R.string.struggle));
                model.getAdapter().insertPhrase(p1, Phrase.Type.minigame);
                break;
            }

            case "9c3": {
                preventDefault = true;
                Phrase p1 = new Phrase(Phrase.Type.minigame);
                p1.setSentence(this, getString(R.string.fz4_action_push_him_back));
                model.getAdapter().insertPhrase(p1, Phrase.Type.minigame);
                break;
            }

            case "9c4": {
                preventDefault = true;
                Phrase p1 = new Phrase(Phrase.Type.minigame);
                p1.setSentence(Main.this, getString(R.string.fz4_action_dodge));
                model.getAdapter().insertPhrase(p1, Phrase.Type.minigame);
                break;
            }

            case "9c5": {
                preventDefault = true;
                Phrase p1 = new Phrase(Phrase.Type.minigame);
                p1.setSentence(this, getString(R.string.fz4_action_fend_off));
                model.getAdapter().insertPhrase(p1, Phrase.Type.minigame);
                break;
            }

            case "9c5a": {
                graphics.bloodFilter(Main.this, 10000, true);
                discussForward(model.getCurrentPhrase());
                break;
            }

            case "9c6": {
                preventDefault = true;
                Phrase p1 = new Phrase(Phrase.Type.minigame);
                p1.setSentence(this, getString(R.string.fz4_action_counter_attack));
                model.getAdapter().insertPhrase(p1, Phrase.Type.minigame);
                break;
            }
            case "9c7": {
                graphics.bloodFilterPlus(Main.this, 10000);
                discussForward(model.getCurrentPhrase());
                break;
            }
            case "9c8": {
                preventDefault = true;
                Phrase p1 = new Phrase(Phrase.Type.minigame);
                p1.setSentence(this, getString(R.string.fz4_action_face_your_fears));
                model.getAdapter().insertPhrase(p1, Phrase.Type.minigame);
                graphics.bloodFilter(Main.this, 10000, false);
                break;
            }
            case "9c9": {
                preventDefault = true;
                Phrase p1 = new Phrase(Phrase.Type.minigame);
                p1.setSentence(this, getString(R.string.fz4_action_dont_fait));
                model.getAdapter().insertPhrase(p1, Phrase.Type.minigame);
                graphics.bloodFilter(Main.this, 10000, false);

                break;
            }
            case "9c10": {
                discussForward(model.getCurrentPhrase());
                break;
            }

            case "9c11": {
                Phrase p1 = new Phrase(Phrase.Type.me);
                p1.setSentence(this, "Erika!");
                model.getAdapter().insertPhrase(p1, Phrase.Type.me);
                discussForward(model.getCurrentPhrase());
                break;
            }

            case "9c12": {
                Phrase p1 = Phrase.fast(5, Phrase.determineTypeCode(Phrase.Type.dest), getString(R.string.chloe_sentence_nobody_touches_erika), p.getSeen(), 0);
                model.getAdapter().insertPhrase(p1, Phrase.Type.dest);
                discussForward(model.getCurrentPhrase());
                break;
            }


            case "9cc": {
                graphics.blackFilter(this, 2000, true);
                Runnable2 runnable = new Runnable2("End", 3000) {
                    @Override
                    public void run() {
                        String chapterCode = "10b";
                        if (model.getTableOfSymbols().condition(GlobalData.Game.FRIENDZONE4.getId(), "barWithZoe", "true")) {
                            chapterCode = "10a";
                        }
                        model.getParams().setChapterCode(chapterCode);
                        model.getTableOfSymbols().setChapterCode(chapterCode);
                        model.getParams().save(Main.this);
                        model.getTableOfSymbols().save(Main.this);
                        setResult(RESULT_OK, new Intent()
                                .putExtra("params", (Parcelable) model.getParams())
                                .putExtra("symbols", (Parcelable) model.getTableOfSymbols())
                        );
                        finish();
                    }
                };
                model.mh.push(runnable);
                model.mh.run(runnable);
                break;
            }


            case "10": {
                graphics.friendzoned(Main.this);
                discussForward(model.getCurrentPhrase());
                break;
            }

            case "10c1": {
                switchVideo("horror");
                discussForward(model.getCurrentPhrase());
                break;
            }

            case "10c9": {
                graphics.blackFilter(this, 2000, true);
                Runnable2 runnable = new Runnable2("10c9", 2000) {
                    @Override
                    public void run() {
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                };
                model.mh.push(runnable);
                model.mh.run(runnable);
                break;
            }

        }
    }

    private void beforeDescription() {
        switch (model.getParams().getChapterCode().toLowerCase()) {
            case "3a":
                model.insertPhrase(-1, Phrase.Type.date, "Il y a 1 mois");
                model.insertPhrase(0,
                        getString(R.string.before_description_sentence_1), getString(R.string.before_description_sentence_2),
                        getString(R.string.before_description_sentence_3),
                        getString(R.string.before_description_sentence_4),
                        getString(R.string.before_description_sentence_5));
                if (model.getTableOfSymbols().getFirstName().equals("Pigloo")
                        || model.getTableOfSymbols().getFirstName().equals("Philibert")
                        || model.getTableOfSymbols().getFirstName().equals("Fador")
                        || model.getTableOfSymbols().getFirstName().equals("Hocine")
                        || model.getTableOfSymbols().condition(GlobalData.Game.FRIENDZONE4.getId(), "endFz3", "eva")) {
                    model.insertPhrase(-1, Phrase.Type.date, getString(R.string.before_description_sentence_6));
                    model.insertPhrase(0, getString(R.string.before_description_sentence_7));
                }
                model.setLastSeenIf(Phrase.Type.me);
                break;
        }
    }

    /**
     * Switch the background to the video mode
     *
     * @param name the name of the resource without the extension.
     */
    private void switchVideo(String name) {
        if (name.equals("background_lake")) {
            graphics.setFilterAlpha(Main.this, .3f);
        }
        graphics.setVideoToReload(name);
        final RelativeLayout r = findViewById(R.id.fz4_mainactivity_background);
        final VideoView v = findViewById(R.id.fz4_mainactivty_background_video);
        final ImageView i = findViewById(R.id.fz4_mainactivty_background_image);
        model.setHasBackgroundMedia(true);
        model.getAdapter().setHasBackgroundMedia(true);
        model.setVideoToReload(name);
        Video.put(
                v,
                Uri.parse("android.resource://" + getPackageName() + "/" + Video.determine(name, this)),
                true,
                new Runnable() {
                    @Override
                    public void run() {
                        model.setVideoToReload("");
                        graphics.setVideoToReload("");
                        model.getAdapter().setHasBackgroundMedia(false);
                    }
                });

        Animation.setAnimation(r, Animation.Animations.ANIMATION_FADEIN, this);
        r.setVisibility(View.VISIBLE);
        Animation.setAnimation(v, Animation.Animations.ANIMATION_FADEIN, this);
        v.setVisibility(View.VISIBLE);
        Animation.setAnimation(i, Animation.Animations.ANIMATION_FADEOUT, this);
        i.setVisibility(View.INVISIBLE);
    }

    /**
     * @param isUserPaused boolean
     */
    private void pause(boolean isUserPaused) {
        if (model.getCurrentGameState() == MainModel.GameState.USER_PAUSED) {
            return;
        }
        model.mh.kill();
        model.getAdapter().removeIfLastIs(Phrase.Type.typing);
        if (isUserPaused) {
            model.getAdapter().insertPhrase(new Phrase(Phrase.Type.paused), Phrase.Type.paused);
        }
        model.setCurrentGameState(isUserPaused ? MainModel.GameState.USER_PAUSED : MainModel.GameState.PAUSED);
    }
}
