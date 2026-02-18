/*
 * Copyright (C) PurpleTear, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Hocine Belbouab <hbdeveloppeur@gmail.com>, 1/10/19 11:38 AM
 *
 */

package fr.purpletear.friendzone.activities.game;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import fr.purpletear.friendzone.R;
import purpletear.fr.purpleteartools.*;


public class Game extends AppCompatActivity {
    private Context context;
    private int screenWidth = 0;
    private MemoryHandler mh = new MemoryHandler();
    private Direction direction = Direction.right;
    private int zombieLife = 8;

    private enum Direction {
        left,
        right
    }

    private enum Sentence {
        saveZoe,
        superb,
        notBad,
        notZoe,
        win
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.friendzone1_activity_game);
        graphics();
        load();
        initOnCreate();
    }

    private void graphics() {
        RequestManager requestManager = Glide.with(this);
        requestManager.load(R.drawable.game_bottom_floor)
                .into((ImageView) this.findViewById(R.id.game_design_floor));

        requestManager.load(R.drawable.game_bg)
                .into((ImageView) this.findViewById(R.id.game_design_bg));


        requestManager.load(R.drawable.game_character_nick)
                .into((ImageView) this.findViewById(R.id.game_character_nick));
    }

    @Override
    protected void onStart() {
        super.onStart();
        initOnStart();
    }

    private void load() {
        context = getApplicationContext();
    }

    /**
     * Inits the onCreate event.
     */
    private void initOnCreate() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        listeners();
        setNames();
    }

    /**
     * Inits the onStart event.
     */
    private void initOnStart() {
        contextAnimation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        animateCharacters();
    }

    private void updateLife(){
        ImageView image =  findViewById(R.id.game_character_bryan_life);
        zombieLife--;
        switch (zombieLife){
            case 8 :
                break;
            case 6 :
                break;
            case 4 :
                break;
            case 2 :
                break;
            case 0 :
                Animation.setAnimation(image, Animation.Animations.ANIMATION_FADEOUT, this);
                Animation.setAnimation(findViewById(R.id.game_character_bryan), Animation.Animations.ANIMATION_FADEOUT, this);
                Animation.setAnimation(findViewById(R.id.game_character_bryan_name), Animation.Animations.ANIMATION_FADEOUT, this);
                finish();
                break;
        }
    }

    /**
     * Animates onStart the imageView Context of the game
     */
    private void contextAnimation() {
        final ImageView i = findViewById(R.id.game_context);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Animation.setAnimation(i, Animation.Animations.ANIMATION_FADEOUT, context);
                i.setVisibility(View.INVISIBLE);
            }
        }, 3000);
    }

    /**
     * Starts animation of every characters.
     */
    private void animateCharacters() {
        final ImageView zoe = findViewById(R.id.game_character_zoe);
        final ImageView bryan = findViewById(R.id.game_character_bryan);
        animate("animation_game_zoe", zoe);
        animate("animation_game_bryan", bryan);

    }

    private void animate(String name, ImageView i) {
        int id = getResources().getIdentifier(name, "drawable", getPackageName());
        if(id == 0) {
            throw new IllegalArgumentException("Animation" + " " + name + " " + "Not found");
        }
        i.setBackgroundResource(id);
        final AnimationDrawable a = (AnimationDrawable) i.getBackground();
        a.setCallback(i);
        a.setVisible(true, true);
        a.start();
    }

    private Sentence randomSentence() {
        final int min = 0;
        final int max = 1;
        switch (min + (int)(Math.random() * ((max - min) + 1))){
            case 0 :
                return Sentence.superb;
            case 1 :
                return Sentence.notBad;
        }
        throw new IllegalArgumentException();
    }

    private void displaySentence(Sentence sentence) {
        final ImageView i = findViewById(R.id.game_context);
        i.setImageResource(getIdFromSentence(sentence));
        i.setVisibility(View.VISIBLE);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Animation.setAnimation(i, Animation.Animations.ANIMATION_FADEOUT, context);
                i.setVisibility(View.INVISIBLE);
            }
        }, 200);

    }

    private int getIdFromSentence(Sentence sentence) {
        switch(sentence) {
            case saveZoe:
                return R.drawable.game_context;
            case superb:
                return R.drawable.game_super;
            case notBad:
                return R.drawable.game_not_bad;
            case notZoe:
                return R.drawable.game_wrong_hit;
            case win:
                return R.drawable.game_win;
        }
        throw new IllegalArgumentException();
    }

    private void goForward(Direction direction) {
        float percentStep = .05f;
        this.direction = direction;
        switch (direction) {
            case left: {
                final ImageView frame = findViewById(R.id.game_character_nick);
                final View t = findViewById(R.id.game_character_nick_name);
                float x = frame.getX();
                float x2 = t.getX();
                float mStep = screenWidth * percentStep;
                if (x - mStep <= 0)
                    return;
                frame.setX(x - mStep);
                t.setX(x2 - mStep);
                frame.setScaleX(-1);
                break;
            }

            case right: {
                final ImageView frame = findViewById(R.id.game_character_nick);
                final View t = findViewById(R.id.game_character_nick_name);
                float x = frame.getX();
                float x2 = t.getX();
                float mStep = screenWidth * percentStep;
                if (x + frame.getWidth() + mStep > screenWidth)
                    return;
                frame.setX(x + mStep);
                t.setX(x2 + mStep);
                frame.setScaleX(1);
                break;
            }
        }

    }

    private void hit() {
        animationHit();
        if(touches((findViewById(R.id.game_character_zoe)))){
            displaySentence(Sentence.notZoe);
        } else if(touches(findViewById(R.id.game_character_bryan)) && bryanIsAlive()){
            if(zombieLife == 1) {
                displaySentence(Sentence.win);
            } else {
                displaySentence(randomSentence());
            }
            updateLife();
        }

    }

    private boolean bryanIsAlive() {
        return zombieLife > 0;
    }

    private boolean touches(View w){
        View v = findViewById(R.id.game_character_nick);
        return (w.getX() < v.getX() + ((direction == Direction.left) ? 0f : v.getWidth())
                && v.getX() + ((direction == Direction.left) ? 0f : v.getWidth()) < w.getX() + w.getWidth());
    }

    private void animationHit() {
        final ImageView i = findViewById(R.id.game_character_nick);
        i.setImageResource(R.drawable.game_character_nick_attack);
        Runnable2 runnable = new Runnable2("", 100) {
            @Override
            public void run() {
                i.setImageResource(R.drawable.game_character_nick);
            }
        };

        mh.push(runnable);
        mh.run(runnable);
    }

    private void listeners() {
        Finger.Companion.defineOnTouch(findViewById(R.id.game_button_right), this, new Runnable() {
            @Override
            public void run() {
                goForward(Direction.right);
            }
        });

        Finger.Companion.defineOnTouch(findViewById(R.id.game_button_left), this, new Runnable() {
            @Override
            public void run() {
                goForward(Direction.left);
            }
        });

        findViewById(R.id.game_button_hit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hit();
            }
        });
    }

    private void setNames() {
        final TextView nick = findViewById(R.id.game_character_nick_name);
        final TextView zoe = findViewById(R.id.game_character_zoe_name);
        final TextView bryan = findViewById(R.id.game_character_bryan_name);
        zoe.setText(R.string.zoe);
        TableOfSymbols symbols = getIntent().getParcelableExtra("symbols");
        nick.setText(symbols == null ? "Nick" : symbols.getFirstName());
        bryan.setText(R.string.bryan);
    }
}
