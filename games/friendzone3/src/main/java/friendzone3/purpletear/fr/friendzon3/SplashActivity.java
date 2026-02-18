package friendzone3.purpletear.fr.friendzon3;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import friendzone3.purpletear.fr.friendzon3.handlers.MemoryHandler;
import friendzone3.purpletear.fr.friendzon3.handlers.SoundHandler;
import purpletear.fr.purpleteartools.Animation;

public class SplashActivity extends AppCompatActivity {
    private MemoryHandler mh = new MemoryHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fz3_activity_splash);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus) {
            animate();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mh.kill();
        reload();
    }

    /**
     * Starts the whole animation.
     * - FADEIN
     * - Then calls animation
     * - FADEOUT
     */
    private void animate() {
        final Context context = this;
        Handler handler = new Handler();
        Runnable runnable;
        handler.postDelayed(runnable = new Runnable() {
            @Override
            public void run() {
                /* FADEIN */
                Animation.setAnimation(
                        findViewById(R.id.splashscreenactivity_animation_logo),
                        Animation.Animations.ANIMATION_FADEIN,
                        context,
                        1250);
                Handler handler = new Handler();
                Runnable runnable;
                handler.postDelayed(runnable = new Runnable() {
                    @Override
                    public void run() {
                        animation(new Runnable() {
                            @Override
                            public void run() {
                                /* FADEOUT */
                                Animation.setAnimation(
                                        findViewById(R.id.splashscreenactivity_animation_logo),
                                        Animation.Animations.ANIMATION_FADEOUT,
                                        context,
                                        1250);
                                Handler handler = new Handler();
                                Runnable runnable;
                                handler.postDelayed(runnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        startActivity(new Intent(SplashActivity.this, Load.class));
                                    }
                                }, 1300);
                                mh.push(handler, runnable);
                            }
                        });
                    }
                }, 1250);
                mh.push(handler, runnable);
            }
        }, 1000);
        mh.push(handler, runnable);
    }

    /**
     * Starts the image animation
     */
    private void animation(Runnable then) {
        animateImage((ImageView) findViewById(R.id.splashscreenactivity_animation_logo), this);
        Handler handler = new Handler();
        handler.postDelayed(then, 57 * 15);
        mh.push(handler, then);
    }

    /**
     *  @param i the imageView
     * @param activity the activity
     */
    private void animateImage(ImageView i, Activity activity) {
        int id = getResources().getIdentifier("anim_logo_purpletear", "drawable", getPackageName());
        if(id == 0) {
            throw new IllegalArgumentException("Animation" + " " + "anim_logo_purpletear" + " " + "Not found");
        }
        i.setBackgroundResource(id);
        final AnimationDrawable a = (AnimationDrawable) i.getBackground();
        a.setCallback(i);
        a.setVisible(true, true);
        a.start();

        new SoundHandler().generate("glitch_01", activity, false).play("glitch_01");
    }

    /**
     * Reloads the view.
     */
    private void reload() {
        findViewById(R.id.splashscreenactivity_animation_logo).setVisibility(View.INVISIBLE);
    }
}
