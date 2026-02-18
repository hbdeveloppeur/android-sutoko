package fr.purpletear.friendzone4.game.activities.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import fr.purpletear.friendzone4.R;
import fr.purpletear.friendzone4.factories.Std;
import fr.purpletear.friendzone4.game.activities.menu.Menu;

public class SplashActivity extends AppCompatActivity {

    /**
     * Handles graphic settings
     *
     * @see SplashGraphics
     */
    private SplashGraphics graphics;

    /**
     * Handles model settings
     */
    private SplashModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fz4_activity_splash);
        load();
    }

    private void load() {
        model = new SplashModel();
        graphics = new SplashGraphics();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            Std.hideBars(getWindow(), true, true);
            model.startAnimation(
                    this,
                    findViewById(R.id.fz4_splashscreenactivity_animation_logo),
                    new SplashInterface() {
                        @Override
                        public void onCompletion() {
                            Intent i = new Intent(SplashActivity.this, Menu.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            i.putExtra("smart_ads", (Parcelable) getIntent().getParcelableExtra("smart_ads"));
                            i.putExtra("granted", getIntent().getBooleanExtra("granted", false));
                            i.putExtra("isPremiumGame", getIntent().getBooleanExtra("isPremiumGame", false));
                            startActivity(i);
                        }
                    }
            );
        } else {
            reload();
            model.kill();
        }
    }

    private void reload() {
        graphics.reload(Glide.with(this), findViewById(R.id.fz4_splashscreenactivity_animation_logo));
    }
}
