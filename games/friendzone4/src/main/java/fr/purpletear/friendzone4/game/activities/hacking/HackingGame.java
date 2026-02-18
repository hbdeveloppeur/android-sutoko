package fr.purpletear.friendzone4.game.activities.hacking;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import fr.purpletear.friendzone4.R;
import fr.purpletear.friendzone4.callback.IntroAnimationCallback;
import fr.purpletear.friendzone4.purpleTearTools.Finger;
import fr.purpletear.friendzone4.purpleTearTools.MemoryHandler;
import fr.purpletear.friendzone4.purpleTearTools.Runnable2;

public class HackingGame extends AppCompatActivity {
    MemoryHandler mh;
    /**
     * Contains the model values
     */
    private HackingGameModel model;
    /**
     * Contains the graphics settings
     */
    private HackingGameGraphics graphics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fz4_activity_hacking_game);
        load();
    }

    @Override
    protected void onDestroy() {
        mh.kill();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void load() {
        model = new HackingGameModel(getIntent().getBooleanExtra("isEnd", false));
        mh = new MemoryHandler();
        graphics = new HackingGameGraphics();
        listeners();

        graphics.setRecyclerView(
                this,
                (RecyclerView) findViewById(R.id.fz4_hacking_game_recyclerview),
                model.getAdapter(),
                getWindowManager().getDefaultDisplay()
        );
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus && model.isFirstStart()) {
            graphics();
        }
    }

    private void graphics() {
        graphics.setImages(HackingGame.this, Glide.with(HackingGame.this), model.isEnd());
    }

    private void listeners() {
        Finger.defineOnTouch(
                findViewById(R.id.fz4_hacking_game_button),
                HackingGame.this,
                new Runnable() {
                    @Override
                    public void run() {
                        start();
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        if(!model.isEnd()) {
                            stop();
                        }
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                    }
                }
        );
    }

    /**
     *
     */
    private void start () {
        if(model.isRunning){
           return;
        }
        model.isRunning = true;
        model.animate(this, new IntroAnimationCallback() {
            @Override
            public void onFinish() {
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onInsertPhrase(final int position) {
                runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                ((RecyclerView) findViewById(R.id.fz4_hacking_game_recyclerview)).scrollToPosition(position);
                            }
                        }
                );
            }

            @Override
            public void onMiddleAnimation() {
                if(model.isEnd()) {
                    graphics.change(HackingGame.this);
                    Runnable2 runnable = new Runnable2("toEnd", 5000) {
                        @Override
                        public void run() {
                            int duration = graphics.end(HackingGame.this);

                            Runnable2 runnable = new Runnable2("toEnd", duration) {
                                @Override
                                public void run() {
                                    setResult(RESULT_OK);
                                    finish();
                                }
                            };
                            mh.push(runnable);
                            mh.run(runnable);
                        }
                    };
                    mh.push(runnable);
                    mh.run(runnable);
                }
            }
        });
    }

    /**
     *
     */
    private void stop () {
        model.isRunning = false;
    }
}
