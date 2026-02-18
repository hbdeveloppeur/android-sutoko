package friendzone3.purpletear.fr.friendzon3;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.example.sutokosharedelements.OnlineAssetsManager;
import com.example.sutokosharedelements.SutokoSharedElementsData;

import java.util.ArrayList;
import java.util.List;

import friendzone3.purpletear.fr.friendzon3.adapter.ConversationAdapter;
import friendzone3.purpletear.fr.friendzon3.config.Language;
import friendzone3.purpletear.fr.friendzon3.config.Var;
import friendzone3.purpletear.fr.friendzon3.custom.Chapter;
import friendzone3.purpletear.fr.friendzon3.custom.Character;
import friendzone3.purpletear.fr.friendzon3.custom.GridSpacingItemDecoration;
import friendzone3.purpletear.fr.friendzon3.custom.Personnage;
import friendzone3.purpletear.fr.friendzon3.custom.PersonnageStyle;
import friendzone3.purpletear.fr.friendzon3.custom.Phrase;
import friendzone3.purpletear.fr.friendzon3.custom.PhraseCallBack;
import friendzone3.purpletear.fr.friendzon3.custom.Video;
import friendzone3.purpletear.fr.friendzon3.handlers.ChoicesController;
import friendzone3.purpletear.fr.friendzon3.handlers.DiscussionHandler;
import friendzone3.purpletear.fr.friendzon3.handlers.MemoryHandler;
import friendzone3.purpletear.fr.friendzon3.handlers.SoundHandler;
import friendzone3.purpletear.fr.friendzon3.tables.TableOfChapters;
import friendzone3.purpletear.fr.friendzon3.tables.TableOfPhrases;
import purpletear.fr.purpleteartools.Animation;
import purpletear.fr.purpleteartools.Finger;
import purpletear.fr.purpleteartools.GlobalData;
import purpletear.fr.purpleteartools.Measure;
import purpletear.fr.purpleteartools.SimpleSound;
import purpletear.fr.purpleteartools.SinglePlayer;
import purpletear.fr.purpleteartools.Std;

public class MainActivity extends AppCompatActivity implements PhraseCallBack {
    public static MemoryHandler mh;
    public static SinglePlayer sp;
    public static boolean hasBackgroundMedia = false;
    private Chapter chapter = new Chapter();
    private boolean firstStart = true;
    private Phrase currentPhrase = new Phrase();
    private ConversationAdapter adapter;
    private View button;
    private ConstraintLayout choiceArea;
    private LinearLayout choiceParent;
    private RecyclerView recyclerView;
    private SoundHandler sh = new SoundHandler();
    private State currentState = State.DESCRIPTION;
    private Support support = Support.NORMAL;
    private MainActivityGraphics graphics;
    private MainActivityModel model;
    private RequestManager requestManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Std.debug("MainActivity.onCreate");
        chapter.read(MainActivity.this);
        if (isEnd(chapter.getDiscussion().symbols.getChapterCode())) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }
        support = getSupport();
        setContentView(determineLayout());
        load();
        graphics();
        descriptionOnCreate();
    }

    private boolean isEnd(String code) {
        if (code == null) {
            return false;
        }
        List<String> ends = new ArrayList<>();
        ends.add("9a");
        ends.add("9b");
        ends.add("9c");
        ends.add("9d");
        return ends.contains(code.toLowerCase());
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            if (support == Support.NORMAL) {
                graphics.setImages(this, requestManager);
                graphics.setProfilPicture(this, graphics.getProfilPictureId(), support, requestManager);
                graphics.setNameConversation(this, graphics.getConversationTitle(), support);
                graphics.changeStatus(this, graphics.getConversationStatus(), R.color.white, graphics.isDisplayedOnlinePoint(), support);
                graphics.switchVideo(this, graphics.getVideoNameToReload());
                graphics.switchImage(this, graphics.getBackgroundImageName(), support, requestManager);
            }
            graphics.setButtonVisibility(this, graphics.isChoiceButtonVisible(), support);
            graphics.setDescriptionVisibility(this, graphics.isOpenedDescription(), support);
        }

        if (hasFocus && model.isFirstStart()) {
            descriptionOnStart(new Runnable() {
                @Override
                public void run() {
                    graphics.setOpenedDescription(false);
                    discuss(currentPhrase);
                    currentState = State.PLAY;
                }
            });
        } else if (hasFocus) {
            play();
        }

        if (hasFocus) {
            if (chapter.getDiscussion().symbols.getChapterCode().equals("8b") && sh.exist("bg_cinematic")) {
                sh.generate("bg_cinematic", this, true).play("bg_cinematic");
            }
            Std.debug("MainActivity.onResume");
            reloadVideoIfNeeded(getSupport());
            firstStart = false;
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        model.getRecyclerViewLayoutManager(this, support).onRestoreInstanceState(savedInstanceState.getParcelable("recyclerview"));
        graphics = savedInstanceState.getParcelable("graphics");
        chapter.getDiscussion().symbols = savedInstanceState.getParcelable("symbols");
        adapter.setArray(savedInstanceState.<Phrase>getParcelableArrayList("array"));
        currentPhrase = savedInstanceState.getParcelable("currentPhrase");
        firstStart = savedInstanceState.getBoolean("firstStart");
        hasBackgroundMedia = savedInstanceState.getBoolean("hasBackgroundMedia");
        model = savedInstanceState.getParcelable("model");
        currentState = (State) savedInstanceState.getSerializable("currentState");
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("symbols", chapter.getDiscussion().symbols);
        outState.putParcelable("recyclerview", model.getRecyclerViewLayoutManager(this, support).onSaveInstanceState());
        outState.putParcelable("currentPhrase", currentPhrase);
        outState.putParcelable("graphics", graphics);
        outState.putBoolean("firstStart", firstStart);
        outState.putBoolean("hasBackgroundMedia", hasBackgroundMedia);
        outState.putParcelableArrayList("array", adapter.array);
        outState.putParcelable("model", model);
        outState.putSerializable("currentState", currentState);
    }

    @Override
    protected void onDestroy() {
        Std.debug("MainActivity.onDestroy");
        if (sp != null) {
            sp.kill();
        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        Std.debug("MainActivity.onPause");
        if (chapter.getDiscussion().symbols.getChapterCode().equals("8b") && sh.exist("bg_cinematic")) {
            sh.pause("bg_cinematic");
        }
        super.onPause();
        pause();
    }

    private void load() {
        requestManager = Glide.with(this);
        adapter = new ConversationAdapter(MainActivity.this, MainActivity.this, support, chapter.getDiscussion().symbols.getChapterCode(), chapter.getDiscussion().symbols, requestManager);
        mh = new MemoryHandler();
        switch (support) {
            case NORMAL:
                hasBackgroundMedia = false;
                button = findViewById(R.id.mainactivity_button);
                choiceArea = findViewById(R.id.mainactivity_choicebox_area);
                choiceParent = findViewById(R.id.mainactivity_choicebox_parent);
                break;
            case SHELL:
                hasBackgroundMedia = false;
                button = findViewById(R.id.mainactivity_shell_button);
                choiceArea = findViewById(R.id.mainactivity_shell_choicebox_area);
                choiceParent = findViewById(R.id.mainactivity_shell_choicebox_parent);
                break;
            default:
                throw new IllegalArgumentException("Unknown support type.");
        }

        model = new MainActivityModel();
        model.getCollectedTrophies().read(this);

        graphics = new MainActivityGraphics(
                true,
                false,
                "",
                "",
                "",
                "",
                ""
        );

        currentPhrase = chapter.getRoot();
        sp = new SinglePlayer(Data.assetsDirectoryName);

        setRecyclerView();
        listeners();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Std.debug("MainActivity.onBackPressed");

        Std.INSTANCE.confirm(getString(R.string.mainactivity_confirm_quit), getString(R.string.confirm_ok),
                getString(R.string.confirm_discard), new Runnable() {
                    @Override
                    public void run() {

                        setResult(RESULT_CANCELED);
                        finish();

                    }
                }, null, MainActivity.this);
    }

    @Override
    protected void onStop() {
        Std.debug("MainActivity.onStop");
        super.onStop();
    }

    private void listeners() {
        Finger.Companion.defineOnTouch(button, new Runnable() {
            @Override
            public void run() {
                openChoiceBox(MainActivity.this);
            }
        });

        Finger.Companion.defineOnTouch(choiceArea, new Runnable() {
            @Override
            public void run() {
                closeChoiceBox();
            }
        });
    }

    /**
     * Handles the description in the onCreate event
     * - We don't display the description if the user already read it.
     */
    private void descriptionOnCreate() {
        TextView t, d;
        switch (support) {
            case NORMAL:
                t = findViewById(R.id.mainactivity_title);
                d = findViewById(R.id.mainactivity_description);
                break;
            case SHELL:
                t = findViewById(R.id.mainactivity_shell_title);
                d = findViewById(R.id.mainactivity_shell_description);
                break;
            default:
                throw new IllegalArgumentException("Not handled support type");
        }
        t.setText(Character.Companion.updateNames(
                this,
                getString(
                        R.string.mainactivity_chapter_title,
                        chapter.getDiscussion().symbols.getChapterNumber(),
                        chapter.getTitle())));

        d.setText(Character.Companion.updateNames(this, (chapter.getDescription())));
    }

    /**
     * Handles the description in the onStart event
     */
    private void descriptionOnStart(final Runnable completion) {
        int delay = Data.debugMode ? 2000 : 8500;

        Handler handler = new Handler(Looper.getMainLooper());
        Runnable runnable;

        handler.postDelayed(runnable = new Runnable() {
            @Override
            public void run() {
                View v;
                switch (support) {
                    case NORMAL:
                        v = findViewById(R.id.mainactivity_filter);
                        break;
                    case SHELL:
                        v = findViewById(R.id.mainactivity_shell_filter);
                        break;
                    default:
                        throw new IllegalArgumentException("Not handled support type");
                }
                long delayAnimation = Animation.setAnimation(
                        v,
                        Animation.Animations.ANIMATION_FADEOUT,
                        MainActivity.this);
                Handler handler = new Handler(Looper.getMainLooper());
                Runnable runnable;
                handler.postDelayed(runnable = new Runnable() {
                    @Override
                    public void run() {
                        completion.run();
                    }
                }, delayAnimation);
                mh.push(handler, runnable);
            }
        }, delay);

        mh.push(handler, runnable);
    }

    /**
     * Inits the graphics
     */
    private void graphics() {
        switch (support) {
            case NORMAL:
                Chapter info = TableOfChapters.INSTANCE.get(this, chapter.getDiscussion().symbols.getChapterCode());
                graphics.setNameConversation(this, (info.getStartingTitle()), support);
                if (info.hasProfilPicture(this)) {
                    graphics.setProfilPicture(this, info.getProfilPicture(), support, requestManager);
                }

                starter();
                break;
            case SHELL:

                break;
            default:
                throw new IllegalArgumentException("Not handled support type");
        }
    }

    private void setRecyclerView() {
        switch (support) {
            case NORMAL:
                recyclerView = findViewById(R.id.mainActivity_recyclerview);

                break;
            case SHELL:
                recyclerView = findViewById(R.id.mainactivity_shell_recyclerview);
                break;
            default:
                throw new IllegalArgumentException();
        }

        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        LinearLayoutManager lLayout = new LinearLayoutManager(MainActivity.this);
        switch (support) {
            case SHELL:
                lLayout.setStackFromEnd(false);
                recyclerView.addItemDecoration(
                        new GridSpacingItemDecoration(
                                1,
                                Math.round(Measure.percent(Measure.Type.HEIGHT,
                                        .1f,
                                        getWindowManager().getDefaultDisplay()
                                )), true, 0));
                break;
            case NORMAL:
                lLayout.setStackFromEnd(true);
                recyclerView.addItemDecoration(
                        new GridSpacingItemDecoration(
                                1,
                                Math.round(Measure.percent(Measure.Type.HEIGHT,
                                        1.5f,
                                        getWindowManager().getDefaultDisplay()
                                )), false, 0));
                break;
            default:
                throw new IllegalArgumentException("MainActivity.setRecyclerView : Not handled support type.");
        }
        recyclerView.setLayoutManager(lLayout);
    }

    private void manageIds(int id) {
        graphics.setButtonVisibility(this, id == 0, support);
        if (id == 0) {
            currentState = State.WAITING_FOR_PLAYER_CHOICE;
        }
    }

    /**
     * Pauses the game.
     */
    private void pause() {
        if (currentState != State.DESCRIPTION && currentState != State.WAITING_FOR_PLAYER_CHOICE) {
            currentState = State.PAUSE;
        }
        mh.kill();
        adapter.removeIfLastIs(Phrase.Type.typing);

        if (sp.isPlaying()) {
            sp.kill();
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * Plays the game.
     */
    private void play() {
        if (State.WAITING_FOR_PLAYER_CHOICE == currentState) {

            return;
        }
        if (State.DESCRIPTION != currentState) {
            currentState = State.PLAY;
            discuss(currentPhrase);
        } else {
            descriptionOnStart(new Runnable() {
                @Override
                public void run() {
                    discuss(currentPhrase);
                    currentState = State.PLAY;
                }
            });
        }
    }

    /**
     * Closes the choiceBox.
     */
    private void closeChoiceBox() {
        choiceArea.setVisibility(View.INVISIBLE);
    }

    /**
     * Opens the choice area.
     *
     * @param callBack the callback
     */
    private void openChoiceBox(PhraseCallBack callBack) {
        ChoicesController.clear(choiceParent);
        choiceArea.setVisibility(View.VISIBLE);
        ChoicesController.choose(this, choiceParent, currentPhrase.getAnswers(chapter.getDiscussion()), callBack, chapter.getDiscussion().symbols, support);
    }

    private boolean nextOrChoice(Phrase p) {
        if (p.getAnswers(chapter.getDiscussion()).size() > 1) {
            currentPhrase = p;
            graphics.setButtonVisibility(this, true, support);
            currentState = State.WAITING_FOR_PLAYER_CHOICE;
            return false;
        }

        graphics.setButtonVisibility(this, false, support);
        currentState = State.WAITING_FOR_PLAYER_CHOICE;
        if (p.getAnswer(chapter.getDiscussion()) != null) {
            discuss(currentPhrase = p.getAnswer(chapter.getDiscussion()));
        }
        return true;
    }

    /**
     * Starts the discussion
     *
     * @param p the root
     */
    private void discuss(final Phrase p) {
        if (p == null) {
            throw new NullPointerException("Found phrase null at chapter " + chapter.getDiscussion().symbols.getChapterCode());
        } else if (p.needsSkip()) {
            // discuss(currentPhrase = p.getAnswer(chapter.getDiscussion()));
            nextOrChoice(p);
            return;
        } else {
            Std.debug(p.toString());
        }

        currentState = State.PLAY;

        //noinspection ConstantConditions
        if (SutokoSharedElementsData.IS_FAST_CHAPTER) {
            p.setSeen(0);
            p.setWait(0);
        }

        //noinspection ConstantConditions
        if (needsLastSeen() && !SutokoSharedElementsData.IS_FAST_CHAPTER) {
            int delay = setLastSeen();

            Handler handler = new Handler(Looper.getMainLooper());
            Runnable runnable;

            handler.postDelayed(runnable = new Runnable() {
                @Override
                public void run() {
                    discuss(currentPhrase);
                }
            }, delay);
            mh.push(handler, runnable);
            return;
        }

        p.formatName("[prenom]", chapter.getDiscussion().symbols.getGlobalFirstName());
        p.formatName("[name]", chapter.getDiscussion().symbols.getGlobalFirstName());
        p.controlEmojis(support);

        if (DiscussionHandler.execute("Eva hésite", p.isDoubleType())) {
            Handler handler = new Handler(Looper.getMainLooper());
            Runnable runnable;

            handler.postDelayed(runnable = new Runnable() {
                @Override
                public void run() {
                    Handler handler = new Handler(Looper.getMainLooper());
                    Runnable runnable;

                    adapter.insertPhrase(p, Phrase.Type.typing);
                    sh
                            .generate("typing", MainActivity.this, false)
                            .play("typing");
                    handler.postDelayed(runnable = new Runnable() {
                        @Override
                        public void run() {
                            adapter.removeIfLastIs(Phrase.Type.typing);
                            nextOrChoice(p);
                        }
                    }, p.getWait());
                    mh.push(handler, runnable);
                }
            }, p.getSeen());

            mh.push(handler, runnable);
            return;
        }


        // DONE
        if (DiscussionHandler.execute("Execution d'un code", p.isCode())) {
            Handler handler = new Handler(Looper.getMainLooper());
            Runnable runnable;

            handler.postDelayed(runnable = new Runnable() {
                @Override
                public void run() {
                    manageCode(p.getCode(), p);
                }
            }, p.getWait());

            mh.push(handler, runnable);
            return;
        }

        if (DiscussionHandler.execute("Demande de mise hors ligne du correspondant", p.isOffline())) {
            final Personnage pers = Personnage.who(chapter.getDiscussion().symbols.getChapterCode(), p.getId_author(), chapter.getDiscussion().symbols);
            adapter.insertPhrase(
                    new Phrase() {{
                        setSentence(getString(R.string.went_offline, pers.getName()));
                    }},
                    Phrase.Type.info
            );
            final Handler handler = new Handler(Looper.getMainLooper());
            Runnable runnable;
            handler.postDelayed(runnable = new Runnable() {
                @Override
                public void run() {
                    graphics.changeStatus(MainActivity.this, getString(R.string.offline), R.color.white, false, support);
                    nextOrChoice(p);
                }
            }, p.getSeen());
            mh.push(handler, runnable);
            return;
        }

        if (DiscussionHandler.execute("Le joueur débloque un trophée", p.isTrophy())) {

            Handler handler = new Handler(Looper.getMainLooper());
            Runnable runnable;

            handler.postDelayed(runnable = new Runnable() {
                @Override
                public void run() {
                    if (!model.getCollectedTrophies().containsByTrophyId(p.getTrophyId())) {
                        model.getCollectedTrophies().add(MainActivity.this, p.getTrophyId(), GlobalData.Game.FRIENDZONE3.getId(), BuildConfig.VERSION_CODE);
                        model.getCollectedTrophies().save(MainActivity.this);
                        SimpleSound sh = new SimpleSound();
                        sh.prepareAndPlay(MainActivity.this, com.example.sutokosharedelements.R.raw.deduction, false, 0);
                        adapter.insertPhrase(p, Phrase.Type.trophy);
                    }
                    nextOrChoice(p);
                }
            }, 1280);

            mh.push(handler, runnable);
            return;
        }

        if (DiscussionHandler.execute("Insertion d'une nouvelle variable en mémoire", p.isCondition())) {
            Var v = p.getVarFromCondition(chapter.getDiscussion().symbols.getChapterNumber());

            chapter.getDiscussion().symbols.addOrSet(GlobalData.Game.FRIENDZONE3.getId(), v.getName(), v.getValue());

            nextOrChoice(p);
            return;
        }

        if (DiscussionHandler.execute("Demande de lancement d'un chapitre", p.isNextChapter())) {
            final String chapterCode = p.code.replace(" ", "").toLowerCase().replace("chapter_", "")
                    .replace("]", "");
            adapter.insertPhrase(new Phrase() {{
                setSeen(p.getSeen());
            }}, Phrase.Type.nextChapter);
            final Handler handler = new Handler(Looper.getMainLooper());
            Runnable runnable;
            //noinspection ConstantConditions
            handler.postDelayed(runnable = new Runnable() {
                @Override
                public void run() {
                    mh.kill();
                    chapter.getDiscussion().symbols.setChapterCode(chapterCode);
                    chapter.getDiscussion().symbols.save(MainActivity.this);
                    Intent intent = new Intent();
                    intent.putExtra("symbols", (Parcelable) chapter.getDiscussion().symbols);
                    setResult(RESULT_OK, intent);
                    // preventPause = true;
                    finish();
                }
            }, SutokoSharedElementsData.IS_FAST_CHAPTER ? 2000 : 7000);
            mh.push(handler, runnable);
            return;
        }

        if (DiscussionHandler.execute("Affichage d'une date", p.isDate())) {
            adapter.insertPhrase(new Phrase() {{
                setSentence(p.getDate());
            }}, Phrase.Type.date);
            nextOrChoice(p);
            return;
        }

        // DONE
        if (DiscussionHandler.execute("Affichage d'une information", p.isInfo())) {
            final String info = p.getSentence()
                    .replace("[", "")
                    .replace("\"", "")
                    .replace("\"", "")
                    .replace("]", "");
            p.setSentence(info);
            final Handler handler = new Handler(Looper.getMainLooper());
            Runnable runnable;
            handler.postDelayed(runnable = new Runnable() {
                @Override
                public void run() {
                    p.setSentence(Character.Companion.updateNames(MainActivity.this, p.getSentence()));
                    adapter.insertPhrase(p, Phrase.Type.info);

                    nextOrChoice(p);

                }
            }, p.getSeen());
            mh.push(handler, runnable);

            return;
        }

        if (DiscussionHandler.execute("La phrase s'affiche dans une notification", p.isInNotif())) {
            Handler handler = new Handler(Looper.getMainLooper());
            Runnable runnable;

            handler.postDelayed(runnable = new Runnable() {
                @Override
                public void run() {
                    changeMessageInNotif(p);
                    if (p.getAnswer(chapter.getDiscussion()) != null) {
                        discuss(currentPhrase = p.getAnswer(chapter.getDiscussion()));
                    }
                }
            }, p.getSeen());
            mh.push(handler, runnable);

            return;
        }

        if (DiscussionHandler.execute("Demande de lancement d'une image de fond", p.isBackgroundPicture())) {
            final Handler handler = new Handler(Looper.getMainLooper());
            Runnable runnable;
            handler.postDelayed(runnable = new Runnable() {
                @Override
                public void run() {
                    graphics.switchImage(MainActivity.this, p.backgroundPictureName(), support, requestManager);
                }
            }, p.getSeen());
            mh.push(handler, runnable);
            nextOrChoice(p);
            return;
        }

        if (DiscussionHandler.execute("Demande de lancement d'une video de fond", p.isBackgroundVideo())) {
            final Handler handler = new Handler(Looper.getMainLooper());
            Runnable runnable;
            handler.postDelayed(runnable = new Runnable() {
                @Override
                public void run() {
                    graphics.switchVideo(MainActivity.this, p.backgroundVideoName());
                }
            }, p.getSeen());
            mh.push(handler, runnable);
            if (p.getAnswer(chapter.getDiscussion()) != null) {
                discuss(currentPhrase = p.getAnswer(chapter.getDiscussion()));
            }
            return;
        }

        if (DiscussionHandler.execute("Un personnage montre une image", p.isContentImage())) {
            final Handler handler = new Handler(Looper.getMainLooper());
            Runnable runnable;
            handler.postDelayed(runnable = new Runnable() {
                @Override
                public void run() {
                    p.setSentence(p.getContentImage());
                    adapter.insertPhrase(p, Phrase.Type.image);

                    nextOrChoice(p);
                }
            }, p.getSeen());

            mh.push(handler, runnable);
            return;
        }

        if (DiscussionHandler.execute("Un message est caché", p.isHidden())) {
            adapter.insertPhrase(new Phrase() {{
                setId_author(p.getId_author());
                setSentence(Character.Companion.updateNames(MainActivity.this, p.getSentence()));
            }}, Phrase.Type.hidden);

            nextOrChoice(p);
            return;
        }

        if (DiscussionHandler.execute("Affichage d'une réponse conditionnée.", p.isAnswerCondition())) {
            String values[] = p.getAnswerCondition();
            String condition[] = values[0].replace("[", "").replace("]", "").replace(" ", "").split("==");
            Var var = new Var(condition[0], condition[1], -1);
            if (chapter.getDiscussion().symbols.condition(GlobalData.Game.FRIENDZONE3.getId(), var.getName(), var.getValue())) {
                p.setSentence(values[1]);
            } else {
                p.setSentence(values[2]);
            }

            Phrase pnew = TableOfPhrases.Companion.determineConditionResult(chapter.getDiscussion().symbols, values, chapter.getDiscussion().phrases);

            discuss(pnew);
            return;
        }

        if (DiscussionHandler.execute("Affichage d'un code du shell", p.isRobotCode(support))) {
            Handler handler = new Handler(Looper.getMainLooper());
            Runnable runnable;
            handler.postDelayed(runnable = new Runnable() {
                @Override
                public void run() {
                    p.setSentence(Character.Companion.updateNames(MainActivity.this, p.getRobotCode(support)));
                    adapter.insertPhrase(p, Phrase.Type.robotCode);
                    nextOrChoice(p);
                }
            }, p.getSeen());
            mh.push(handler, runnable);
            return;
        }

        if (DiscussionHandler.execute("Lancement d'un son", p.isSound())) {
            Handler handler = new Handler(Looper.getMainLooper());
            Runnable runnable;
            handler.postDelayed(runnable = new Runnable() {
                @Override
                public void run() {
                    String name = p.soundName();
                    sh.generate(name, MainActivity.this, false).play(name);
                    nextOrChoice(p);
                }
            }, p.getSeen());
            mh.push(handler, runnable);
            return;
        }

        if (DiscussionHandler.execute("Affichage d'une alerte", p.isAlert())) {
            Handler handler = new Handler(Looper.getMainLooper());
            Runnable runnable;
            handler.postDelayed(runnable = new Runnable() {
                @Override
                public void run() {
                    p.setSentence(Character.Companion.updateNames(MainActivity.this, p.getSentence()));
                    adapter.insertPhrase(p, Phrase.Type.alert);
                    if (p.getAnswer(chapter.getDiscussion()) != null) {
                        discuss(currentPhrase = p.getAnswer(chapter.getDiscussion()));
                    }
                }
            }, p.getSeen());
            mh.push(handler, runnable);
            return;
        }

        if (DiscussionHandler.execute("Demande de mise en ligne du correspondant", p.isAskForOnline())) {
            Handler handler = new Handler(Looper.getMainLooper());
            Runnable runnable;
            handler.postDelayed(runnable = new Runnable() {
                @Override
                public void run() {
                    graphics.changeStatus(MainActivity.this, getString(R.string.online), R.color.white, true, support);
                    nextOrChoice(p);
                }
            }, p.getSeen());
            mh.push(handler, runnable);
            return;
        }

        if (DiscussionHandler.execute("Demande de mise en vue d'un message", p.isAskForSeen())) {
            Handler handler = new Handler(Looper.getMainLooper());
            Runnable runnable;
            handler.postDelayed(runnable = new Runnable() {
                @Override
                public void run() {
                    setLastSeen();
                    nextOrChoice(p);
                }
            }, p.getSeen());
            mh.push(handler, runnable);
            return;
        }

        if (DiscussionHandler.execute("Affichage d'un texte du shell", p.isRobot(support))) {
            Handler handler = new Handler(Looper.getMainLooper());
            Runnable runnable;
            handler.postDelayed(runnable = new Runnable() {
                @Override
                public void run() {
                    final Phrase newp = new Phrase();
                    newp.setSentence(Character.Companion.updateNames(MainActivity.this, p.getRobot(support)));
                    adapter.insertPhrase(newp, Phrase.Type.robot);
                    Handler handler = new Handler(Looper.getMainLooper());
                    Runnable runnable;
                    handler.postDelayed(runnable = new Runnable() {
                        @Override
                        public void run() {
                            nextOrChoice(p);
                        }
                    }, adapter.getDelayAnimate() * p.getSentence().length());
                    mh.push(handler, runnable);
                }
            }, p.getSeen());
            mh.push(handler, runnable);
            return;
        }

        if (DiscussionHandler.execute("Nick parle", p.isMe(chapter.getDiscussion().symbols.getChapterCode(), chapter.getDiscussion().symbols))) {
            p.setSentence(Character.Companion.updateNames(MainActivity.this, p.getSentence()));
            adapter.insertPhrase(p, Phrase.Type.me);
            nextOrChoice(p);
            return;
        }

        // DONE
        DiscussionHandler.execute("Eva parle", new Runnable() {
            @Override
            public void run() {

                p.setSentence(Character.Companion.updateNames(MainActivity.this, p.getSentence()));
                if (p.getSeen() == 0 && p.getWait() == 0) {
                    adapter.insertPhrase(p, Phrase.Type.dest);
                    nextOrChoice(p);
                    return;
                }

                Handler handler = new Handler(Looper.getMainLooper());
                Runnable runnable;

                handler.postDelayed(runnable = new Runnable() {
                    @Override
                    public void run() {
                        if (p.getWait() > 0) {
                            adapter.insertPhrase(p, Phrase.Type.typing);
                            sh.generate("typing", MainActivity.this, false).play("typing");
                        }

                        Handler handler = new Handler(Looper.getMainLooper());
                        Runnable runnable;
                        handler.postDelayed(runnable = new Runnable() {
                            @Override
                            public void run() {
                                if (p.getWait() > 0) {
                                    adapter.editLast(p, Phrase.Type.dest);
                                    sh.generate("message", MainActivity.this, false).play("message");
                                } else {
                                    adapter.insertPhrase(p, Phrase.Type.dest);
                                }
                                nextOrChoice(p);
                            }
                        }, p.getWait());
                        mh.push(handler, runnable);
                    }
                }, p.getSeen());
                mh.push(handler, runnable);
            }
        });

    }

    /**
     * Determines if the last phrase needs to be seen.
     *
     * @return true if it is
     */
    private boolean needsLastSeen() {
        Phrase p = adapter.last();
        boolean isNotNull = p != null;
        if (!isNotNull) {
            return false;
        }
        boolean isChat = support == Support.NORMAL;
        boolean isMe = p.getType() == Phrase.Type.me;
        boolean asksForSeen = p.getWait() > 0;
        return isChat && isMe && asksForSeen;
    }

    /**
     * Sets the last phrase seen
     *
     * @return the delay of the seen field
     */
    private int setLastSeen() {
        final Phrase p = adapter.last();
        Handler handler = new Handler(Looper.getMainLooper());
        Runnable runnable;
        handler.postDelayed(runnable = new Runnable() {
            @Override
            public void run() {
                p.setWait(0);
                adapter.editLast(p, Phrase.Type.meSeen);
            }
        }, p.getWait());
        mh.push(handler, runnable);
        return p.getWait();
    }

    @Override
    public void onClickChoice(Phrase p) {

        closeChoiceBox();
        graphics.setButtonVisibility(this, false, support);
        for (; ; ) {
            adapter.insertPhrase(
                    new Phrase(Character.Companion.updateNames(this, p.getSentence()), p.getWait(), 0, p.getSeen(), ""),
                    Phrase.Type.me);


            if (null == p.getAnswer(chapter.getDiscussion())) {
                return;
                // throw new NullPointerException("Answer cannot be null. At MainActivity.listeners // p " + p.id + " - chapter " + chapter.getCode() + " Lang - " + Language.INSTANCE.determineLangDirectory());
            } else if (p.getAnswer(chapter.getDiscussion()).getId_author() != 0) {
                break;
            } else {
                p = p.getAnswer(chapter.getDiscussion());
            }

        }
        nextOrChoice(p);
    }

    @Override
    public void onInsertPhrase() {
        Std.debug("onInsertPhrase");
        toBottom();
    }

    @Override
    public void onClickSound(String name) {
        sp.playWithNameFromFullPath(name, this, new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onFinishSound() {
        adapter.notifyDataSetChanged();
    }

    /**
     * Manages the codes got from messages.
     *
     * @param code the number of the code asked
     */
    private void manageCode(int code, final Phrase p) {
        switch (code) {
            case 1: {

                graphics.changeStatus(
                        MainActivity.this,
                        getString(R.string.blocked),
                        R.color.warning,
                        false, support);

                Handler handler = new Handler(Looper.getMainLooper());
                Runnable runnable;

                handler.postDelayed(runnable = new Runnable() {
                    @Override
                    public void run() {
                        final ImageView i = findViewById(R.id.mainactivity_photo);
                        int d = R.drawable.friendzone3_anim_bug_photo;
                        i.setImageResource(R.color.transparent);
                        i.setBackgroundResource(d);
                        final AnimationDrawable a = (AnimationDrawable) i.getBackground();
                        a.setCallback(i);
                        a.setVisible(true, true);
                        a.start();
                        sh.generate("glitch_02", MainActivity.this, false).play("glitch_02");

                        Handler handler = new Handler(Looper.getMainLooper());
                        Runnable runnable;

                        handler.postDelayed(runnable = new Runnable() {
                            @Override
                            public void run() {
                                requestManager.load(
                                        OnlineAssetsManager.INSTANCE.getImageFilePath(MainActivity.this, GlobalData.Game.FRIENDZONE3.getId(), "friendzone3_no_avatar")
                                ).into(i);
                                i.setBackgroundResource(R.color.transparent);
                                graphics.changeStatus(MainActivity.this, getString(R.string.online), R.color.white, true, support);
                                nextOrChoice(currentPhrase);
                            }
                        }, a.getNumberOfFrames() * a.getDuration(0));
                        mh.push(handler, runnable);
                    }
                }, 4000);
                mh.push(handler, runnable);
                break;
            }
            case 2:
                adapter.insertPhrase(
                        new Phrase() {{
                            setSentence("[" + Language.INSTANCE.getSimpleCode() + "/psy01.mp3]");
                            setId_author(currentPhrase.getId_author());
                        }},
                        Phrase.Type.vocal);
                adapter.insertPhrase(
                        new Phrase() {{
                            setSentence("[" + Language.INSTANCE.getSimpleCode() + "/psy02.mp3]");
                            setId_author(currentPhrase.getId_author());
                        }},
                        Phrase.Type.vocal);
                nextOrChoice(currentPhrase);
                break;

            case 3: {
                View v = findViewById(R.id.mainactivity_overlay);
                v.setVisibility(View.VISIBLE);
                long delay = Animation.setAnimation(v, Animation.Animations.ANIMATION_SLIDE_IN_FROM_LEFT, this);
                Handler handler = new Handler(Looper.getMainLooper());
                Runnable runnable;
                handler.postDelayed(runnable = new Runnable() {
                    @Override
                    public void run() {
                        nextOrChoice(currentPhrase);
                    }
                }, delay + 2250);
                mh.push(handler, runnable);
                break;
            }

            case 4: {
                Handler handler = new Handler(Looper.getMainLooper());
                Runnable runnable;
                final View v = findViewById(R.id.mainactivity_overlay);
                v.setVisibility(View.VISIBLE);
                handler.postDelayed(runnable = new Runnable() {
                    @Override
                    public void run() {
                        changeMessageInNotif(R.color.notificationPurple, R.color.white, getString(R.string.mainactivity_click_notification));
                        Finger.Companion.defineOnTouch(v,
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        v.setVisibility(View.GONE);
                                        manageCode(5, p);
                                        nextOrChoice(currentPhrase);
                                    }
                                });

                    }
                }, currentPhrase.getSeen());
                mh.push(handler, runnable);
                break;
            }
            case 5: {
                graphics.changeStatus(this, getString(R.string.online), R.color.white, true, support);
                graphics.setNameConversation(this, getString(R.string.zoe_lana), support);
                graphics.setProfilPicture(this, "friendzone3_zoe_lana", support, requestManager);
                adapter.clear();
                break;
            }

            case 6: {
                graphics.changeStatus(this, getString(R.string.online), R.color.white, true, support);
                graphics.setNameConversation(this, getString(R.string.zoe_lana_marwin), support);
                graphics.setProfilPicture(this, "friendzone3_zoe_rick_marwin_lana", support, requestManager);

                if (nextOrChoice(currentPhrase)) {
                    return;
                }
                break;
            }

            case 7: {
                graphics.changeStatus(this, getString(R.string.online), R.color.white, true, support);
                graphics.setNameConversation(this, getString(R.string.zoe_lana_marwin_rick), support);
                graphics.setProfilPicture(this, "friendzone3_zoe_rick_marwin_lana", support, requestManager);
                if (nextOrChoice(currentPhrase)) {
                    return;
                }
                break;
            }

            case 8: {
                graphics.setButtonVisibility(this, true, support);
                currentState = State.WAITING_FOR_PLAYER_CHOICE;
                Finger.Companion.defineOnTouch(button, new Runnable() {
                    @Override
                    public void run() {
                        graphics.setButtonVisibility(MainActivity.this, false, support);
                        manageCode(9, p);
                    }
                });
                break;
            }

            case 9: {

                LinearLayoutManager lLayout = new LinearLayoutManager(MainActivity.this);
                lLayout.setStackFromEnd(false);
                recyclerView.setLayoutManager(lLayout);

                recyclerView.addItemDecoration(
                        new GridSpacingItemDecoration(
                                1,
                                Math.round(Measure.percent(Measure.Type.HEIGHT,
                                        1.5f,
                                        getWindowManager().getDefaultDisplay()
                                )), true, 0));

                graphics.changeStatus(this, getString(R.string.error), R.color.warning, false, support);
                changeHeaderColor(R.color.scaryHeader);
                findViewById(R.id.mainactivity_root).setBackgroundColor(ContextCompat.getColor(this, R.color.black));
                adapter.clear();
                sh.generate("glitch_03", this, false).play("glitch_03");
                final TextView t = findViewById(R.id.mainactivity_middle_text);
                t.setVisibility(View.VISIBLE);
                t.setTextColor(ContextCompat.getColor(this, R.color.warning));
                t.setText(getString(R.string.a_connexion_interrupted_your_conversation));
                Handler handler = new Handler(Looper.getMainLooper());
                Runnable runnable;
                handler.postDelayed(runnable = new Runnable() {
                    @Override
                    public void run() {
                        t.setVisibility(View.GONE);
                        sh.generate("glitch_03", MainActivity.this, false).play("glitch_03");
                        nextOrChoice(currentPhrase);
                    }
                }, 3000);
                mh.push(handler, runnable);
                break;
            }

            case 12: {
                final Phrase last = adapter.last();
                adapter.editLast(last, Phrase.Type.unHiding);
                Handler handler = new Handler(Looper.getMainLooper());
                Runnable runnable;
                handler.postDelayed(runnable = new Runnable() {
                    @Override
                    public void run() {
                        last.setSentence(last.getSentence());
                        last.code = "";
                        adapter.editLast(last, Phrase.Type.dest);
                        sh.generate("message", MainActivity.this, false).play("message");
                        nextOrChoice(currentPhrase);
                    }
                }, 2000);
                mh.push(handler, runnable);
                break;
            }

            case 13: {

                currentPhrase = Phrase.fast(getString(R.string.fear_level), 1000, 0, 3, "alert");
                currentPhrase.id = p.id;
                discuss(currentPhrase);
                break;
            }

            case 14: {
                Handler handler = new Handler(Looper.getMainLooper());
                Runnable runnable;

                handler.postDelayed(runnable = new Runnable() {
                    @Override
                    public void run() {
                        final ImageView i = findViewById(R.id.mainactivity_photo);
                        int d = R.drawable.friendzone3_anim_bug_photo;
                        i.setImageResource(R.color.transparent);
                        i.setBackgroundResource(d);
                        final AnimationDrawable a = (AnimationDrawable) i.getBackground();
                        a.setCallback(i);
                        a.setVisible(true, true);
                        a.start();
                        sh.generate("glitch_02", MainActivity.this, false).play("glitch_02");

                        Handler handler = new Handler(Looper.getMainLooper());
                        Runnable runnable;

                        handler.postDelayed(runnable = new Runnable() {
                            @Override
                            public void run() {
                                requestManager.load(
                                        OnlineAssetsManager.INSTANCE.getImageFilePath(MainActivity.this, GlobalData.Game.FRIENDZONE3.getId(), "friendzone3_chloe")
                                ).into(i);
                                graphics.changeStatus(MainActivity.this, getString(R.string.online), R.color.white, true, support);
                                Handler handler = new Handler(Looper.getMainLooper());
                                Runnable runnable;
                                handler.postDelayed(runnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        // clearScreenMedia();
                                        // setPicture(R.drawable.friendzone3_eva_zoe);
                                        nextOrChoice(p);
                                    }
                                }, 2500);
                                mh.push(handler, runnable);
                            }
                        }, a.getNumberOfFrames() * a.getDuration(0));
                        mh.push(handler, runnable);
                    }
                }, 4000);
                mh.push(handler, runnable);
                break;
            }

            case 15: {
                // Si tu n'as pas piraté Zoé

                if (!chapter.getDiscussion().symbols.condition(GlobalData.Game.FRIENDZONE3.getId(), "piratedMessage", "true")) {
                    MainActivity.hasBackgroundMedia = true;
                    Phrase p1 = Phrase.fast(getString(R.string.zoe_phone_password), 0, 0, -1, "");
                    p1.setType(Phrase.Type.info);
                    discuss(p1);
                } else {
                    currentPhrase = currentPhrase.getAnswer(chapter.getDiscussion());
                    startActivity(new Intent(MainActivity.this, Phone.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                }
                break;
            }

            case 17: {
                sh.generate("bg_cinematic", this, false)
                        .play("bg_cinematic", new Runnable() {
                            @Override
                            public void run() {
                                sh.generate("bg_cinematic", MainActivity.this, false)
                                        .play("bg_cinematic");
                            }
                        });
                nextOrChoice(p);
                break;
            }

            default:
                throw new IllegalArgumentException("MainActivity.manageCode not handled code " + code + ".");
        }
    }

    private void changeHeaderColor(int colorId) {
        switch (support) {
            case NORMAL:
                findViewById(R.id.mainactivity_header).setBackgroundColor(ContextCompat.getColor(this, colorId));
                break;
            default:
                throw new IllegalArgumentException("Not handled Support type.");
        }
    }

    /**
     * Scrolls to the end of the recyclerview
     */
    private void toBottom() {
        recyclerView.scrollToPosition(adapter.getItemCount() - 1);
    }

    /**
     * Do an specific action at the beginning of a chapter given his code
     */
    private void starter() {
        switch (chapter.getDiscussion().symbols.getChapterCode()) {
            case "2a":
                graphics.changeStatus(this, getString(R.string.mainactivity_2a_starter_libelle), R.color.white, false, support);
                break;

            case "4a":
                graphics.changeStatus(this, getString(R.string.offline), R.color.white, false, support);
                break;

            case "5a":
                graphics.setProfilPicture(this, "friendzone3_lake", support, requestManager);
                graphics.changeStatus(this, "", R.color.white, false, support);
                break;

            case "6a":
                graphics.setProfilPicture(this, "friendzone3_eva_zoe", support, requestManager);
                graphics.changeStatus(this, getString(R.string.online), R.color.white, true, support);
                graphics.switchVideo(this, "friendzone3_horror");
                break;

            case "7a":
                graphics.setProfilPicture(this, "friendzone3_bus_profil", support, requestManager);
                graphics.changeStatus(this, "", R.color.white, false, support);
                break;

            case "7b":
                graphics.setNameConversation(this, getString(R.string.belle_family), support);
                graphics.setProfilPicture(this, "friendzone3_b_profil", support, requestManager);
                graphics.changeStatus(this, "", R.color.white, false, support);
                break;

            case "8b":
                graphics.setNameConversation(this, getString(R.string.belle_family), support);
                graphics.setProfilPicture(this, "friendzone3_b_profil", support, requestManager);
                graphics.changeStatus(this, "", R.color.white, false, support);
                break;

            case "8a":
                graphics.setProfilPicture(this, "friendzone3_forest_profil", support, requestManager);
                graphics.setNameConversation(this, getString(R.string.in_forest), support);
                graphics.switchImage(this, "black", support, requestManager);
                graphics.changeStatus(this, "", R.color.white, false, support);
                break;

            default:
                graphics.changeStatus(this, getString(R.string.online), R.color.white, true, support);
                break;
        }
    }

    /**
     * Changes the message in the current displaying notification given :
     *
     * @param backgroundColorId the id of the color for the background
     * @param textColorId       the id of the color for the text
     * @param sentence          the sentence to display
     */
    private void changeMessageInNotif(int backgroundColorId, int textColorId, String sentence) {
        if (support == Support.SHELL) {
            throw new IllegalArgumentException("Illegal Support type found for that chapter");
        }

        View root = findViewById(R.id.mainactivity_overlay);
        TextView t = root.findViewById(R.id.phrase_notification_text);
        t.setText(Character.Companion.updateNames(this, sentence));
        t.setTextColor(ContextCompat.getColor(this, textColorId));
        GradientDrawable gd = (GradientDrawable) t.getBackground();
        gd.setColor(ContextCompat.getColor(this, backgroundColorId));
        sh.generate("message", this, false).play("message");
    }

    /**
     * Changes the message in the current displaying notification given :
     *
     * @param p the concerned phrase
     */
    private void changeMessageInNotif(Phrase p) {
        Personnage pers = Personnage.who(chapter.getDiscussion().symbols.getChapterCode(), p.getId_author(), chapter.getDiscussion().symbols);
        PersonnageStyle style = pers.getStyle();
        changeMessageInNotif(style.getIdBackgroundColor(), style.getIdTextColor(), p.getInNotf());
    }

    /**
     * Determines the type of support.
     *
     * @return the type of support.
     */
    private Support getSupport() {
        if (chapter.getDiscussion().symbols.getChapterCode().equals("3a")) {
            return Support.SHELL;
        }
        return Support.NORMAL;
    }

    private int determineLayout() {
        switch (support) {
            case NORMAL:
                return R.layout.fz3_activity_main_fixed;
            case SHELL:
                return R.layout.fz3_activity_main_shell;

            default:
                throw new IllegalArgumentException("Support Type not handled");
        }
    }

    /**
     * Reloads the backgroundVideo if needed.
     */
    private void reloadVideoIfNeeded(Support type) {
        switch (type) {
            case NORMAL:
                if (!graphics.getVideoNameToReload().equals("")) {
                    Video.play((VideoView) findViewById(R.id.mainactivty_background_video));
                }
                break;
            case SHELL:
                break;
            default:
                throw new IllegalArgumentException("MainActivity.reloadVideoIfNeeded cannot reload a video with Support type of ordinal " + " " + String.valueOf(getSupport().ordinal()));

        }
    }

    private void clearScreenMedia() {
        hasBackgroundMedia = false;
        findViewById(R.id.mainactivity_background).setVisibility(View.INVISIBLE);
    }

    public enum Support {
        NORMAL,
        SHELL
    }

    private enum State {
        PLAY,
        DESCRIPTION,
        WAITING_FOR_PLAYER_CHOICE,
        PAUSE
    }
}


