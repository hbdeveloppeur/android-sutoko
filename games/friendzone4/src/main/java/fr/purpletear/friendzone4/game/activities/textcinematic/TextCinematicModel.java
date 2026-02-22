package fr.purpletear.friendzone4.game.activities.textcinematic;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.content.Intent;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

import fr.purpletear.friendzone4.BuildConfig;
import fr.purpletear.friendzone4.R;
import fr.purpletear.friendzone4.custom.DiscussionHandler;
import fr.purpletear.friendzone4.game.activities.main.ChoicesController;
import fr.purpletear.friendzone4.game.activities.main.Contact;
import fr.purpletear.friendzone4.game.activities.main.MainInterface;
import fr.purpletear.friendzone4.game.activities.main.Phrase;
import fr.purpletear.friendzone4.game.config.Params;
import fr.purpletear.friendzone4.game.tables.TableOfLinks;
import fr.purpletear.friendzone4.game.tables.TableOfPhrases;
import fr.purpletear.friendzone4.game.tables.Var;
import fr.purpletear.friendzone4.purpleTearTools.Animation;
import fr.purpletear.friendzone4.purpleTearTools.MemoryHandler;
import fr.purpletear.friendzone4.purpleTearTools.Runnable2;
import fr.purpletear.friendzone4.purpleTearTools.SoundHandler;
import purpletear.fr.purpleteartools.GlobalData;
import purpletear.fr.purpleteartools.SimpleSound;
import purpletear.fr.purpleteartools.TableOfSymbols;

class TextCinematicModel {

    /**
     * Contains the sound data and controller structure
     *
     * @see SoundHandler
     */
    private SoundHandler sh;

    private boolean isFirstStart;

    /**
     * Contains the Phrases links structure
     *
     * @see TableOfLinks
     */
    private TableOfLinks tableOfLinks;

    /**
     * Contains the Phrases structure
     *
     * @see TableOfPhrases
     */
    private TableOfPhrases tableOfPhrases;

    private Params params;

    private TableOfSymbols tableOfSymbols;

    private Phrase currentPhrase;

    private int csa;

    MemoryHandler mh;

    TextCinematicModel(Activity c, String code, int choicesecondsaverage, TableOfSymbols tableOfSymbols, Params params) throws IOException {
        isFirstStart = true;
        mh = new MemoryHandler();
        sh = new SoundHandler();
        tableOfLinks = new TableOfLinks();
        tableOfLinks.read(c, code, tableOfSymbols.getStoryVersion(GlobalData.Game.FRIENDZONE4.getId()));
        tableOfPhrases = new TableOfPhrases();
        tableOfPhrases.read(c, code, tableOfSymbols.getStoryVersion(GlobalData.Game.FRIENDZONE4.getId()));
        csa = choicesecondsaverage;
        this.tableOfSymbols = tableOfSymbols;
        this.params = params;
    }

    void prepareSound(Activity a) {
        sh.generate("bg_menu", a, false);
    }

    void startSound() {
        sh.play("bg_menu");
    }

    void pauseSound() {
        sh.pause("bg_menu");
        sh.clear();
    }

    void startDiscuss(Activity a) {
        discuss(a, currentPhrase = tableOfPhrases.getPhrase(tableOfLinks.getDest(0).get(0)));
    }

    void discuss(Activity a) {
        discuss(a, currentPhrase);
    }

    private void discuss(final Activity a, final Phrase p) {

        if (DiscussionHandler.execute("Verification d'une condition", p.is(Phrase.Type.condition))) {
            String values[] = p.getAnswerCondition();
            String condition[] = values[0].replace("[", "").replace("]", "").replace(" ", "").split("==");
            int mThen = Integer.parseInt(values[1]);
            int mElse = Integer.parseInt(values[2]);

            Var v = new Var(condition[0], condition[1], -1);
            Phrase next;
            if (tableOfSymbols.condition(GlobalData.Game.FRIENDZONE4.getId(), v.getName(), v.getValue())) {
                next = tableOfPhrases.getPhrase(mThen);
            } else {
                next = tableOfPhrases.getPhrase(mElse);
            }
            currentPhrase = next;

            discuss(a, next);
            return;
        }
        if (DiscussionHandler.execute("Lancement d'un nouveau chapitre", p.isNextChapter())) {
            final String chapter = p.getNextChapter();
            Runnable2 runnable = new Runnable2("Nouveau chapitre", 2500) {
                @Override
                public void run() {
                    params.setChapterCode(chapter);
                    tableOfSymbols.setChapterCode(chapter);
                    params.save(a);
                    tableOfSymbols.save(a);
                    a.setResult(RESULT_OK, new Intent()
                            .putExtra("params", (Parcelable) params)
                            .putExtra("symbols", (Parcelable) tableOfSymbols)
                    );
                    a.finish();
                }
            };
            mh.push(runnable);
            mh.run(runnable);
            return;
        }

        if (DiscussionHandler.execute("Le joueur débloque un trophée", p.isTrophy())) {

            Runnable2 runnable = new Runnable2("Un personnage envoie une image", p.getSeen()) {
                @Override
                public void run() {
                    discuss(a, p);
                }
            };
            mh.push(runnable);
            mh.run(runnable);
            return;
        }

        Runnable2 runnable = new Runnable2("A", p.getSeen()) {
            @Override
            public void run() {
                if (p.getSentence().replace(" ", "").equals("[TIMED_CHOICE]")) {
                    openChoiceBox(a);
                    countAnimation(a, 7, callback(a));
                    ChoicesController.progressBarAnimation(
                            a.findViewById(R.id.fz4_choicebox_progressbar),
                            7000
                    );
                    return;
                }

                final View v = a.findViewById(R.id.fz4_textcinematic_text);
                updateText(p.getSentence(), v);
                Animation.setAnimation(v, Animation.Animations.ANIMATION_FADEIN, a);

                Runnable2 runnable = new Runnable2("B", p.getWait()) {
                    @Override
                    public void run() {
                        Animation.setAnimation(v, Animation.Animations.ANIMATION_FADEOUT, a);

                        if (tableOfLinks.hasAnswer(currentPhrase.getId())) {
                            discuss(a, currentPhrase = tableOfPhrases.getPhrase(tableOfLinks.getDest(p.getId()).get(0)));
                        } else {
                            long duration = Animation.setAnimation(v, Animation.Animations.ANIMATION_FADEOUT, a);
                            Runnable2 runnable = new Runnable2("C", duration) {
                                @Override
                                public void run() {
                                    a.setResult(RESULT_OK);
                                    a.finish();

                                }
                            };
                            mh.push(runnable);
                            mh.run(runnable);
                        }
                    }
                };

                mh.push(runnable);
                mh.run(runnable);
            }
        };

        mh.push(runnable);
        mh.run(runnable);
    }

    /**
     * Updates the UI of the text
     *
     * @param text String
     */
    private void updateText(String text, View v) {
        String sentence = text.replace("[[time]]", String.valueOf(csa));
        ((TextView) v).setText(sentence);

    }

    private void openChoiceBox(final Activity a) {
        ArrayList<Phrase> phrases = new ArrayList<>();
        phrases.add(Phrase.fast(0, 0, "Chocolat", 0, 0));
        phrases.add(Phrase.fast(0, 0, "Chocolatine", 0, 0));

        ChoicesController.choose(a,
                (ViewGroup) a.findViewById(R.id.fz4_mainactivity_choicebox_parent),
                phrases,
                callback(a),
                tableOfSymbols,
                tableOfSymbols.getGlobalFirstName()
        );

        Animation.setAnimation(
                a.findViewById(R.id.fz4_textcinematic_choicebox),
                Animation.Animations.ANIMATION_FADEIN,
                a
        );
    }

    private void countAnimation(final Activity a, final int s, final MainInterface c) {
        if (s == 0) {
            c.onMissedChoice(null);
            return;
        }
        ChoicesController.countAnimation(
                a.findViewById(R.id.fz4_choicebox_text_counter),
                s,
                18f,
                12f

        );
        Runnable2 runnable = new Runnable2("seconds", 1000) {
            @Override
            public void run() {
                countAnimation(a, s - 1, c);
            }
        };

        mh.push(runnable);
        mh.run(runnable);
    }


    boolean isFirstStart() {
        boolean value = isFirstStart;
        isFirstStart = false;
        return value;
    }

    private MainInterface callback(final Activity a) {
        return new MainInterface() {
            @Override
            public void onClickChoice(Phrase p) {
                long duration = Animation.setAnimation(
                        a.findViewById(R.id.fz4_textcinematic_choicebox),
                        Animation.Animations.ANIMATION_FADEOUT,
                        a
                );
                Runnable2 runnable = new Runnable2("finish", duration) {
                    @Override
                    public void run() {
                        a.setResult(RESULT_OK);
                        a.finish();
                    }
                };
                mh.push(runnable);
                mh.run(runnable);
            }

            @Override
            public void onInsertPhrase(int position, boolean isSmoothScroll) {

            }


            @Override
            public void onClickContact(String code, Contact.Type type) {

            }

            @Override
            public void onClickItem(String type, String name) {

            }

            @Override
            public void onMissedChoice(Phrase p) {
                long duration = Animation.setAnimation(
                        a.findViewById(R.id.fz4_textcinematic_choicebox),
                        Animation.Animations.ANIMATION_FADEOUT,
                        a
                );
                Runnable2 runnable = new Runnable2("finish", duration) {
                    @Override
                    public void run() {
                        a.setResult(RESULT_OK);
                        a.finish();
                    }
                };
                mh.push(runnable);
                mh.run(runnable);
            }

            @Override
            public void onTouchJoystick() {

            }

            @Override
            public void onReleaseJoystick() {

            }

            @Override
            public void onJoystickInfoHit(long ms, String code) {

            }
        };
    }
}
