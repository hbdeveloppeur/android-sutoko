package fr.purpletear.friendzone4.game.activities.end;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import fr.purpletear.friendzone4.R;
import fr.purpletear.friendzone4.factories.Data;
import fr.purpletear.friendzone4.purpleTearTools.SoundHandler;
import fr.purpletear.friendzone4.purpleTearTools.Std;

public class End extends AppCompatActivity {
    private SoundHandler sh = new SoundHandler();
    private EndModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fz4_activity_end);
        listeners();
        load();
        sh.generate("bg_end", this, false);
    }

    private void load() {
        model = new EndModel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sh.play("bg_end");
    }

    @Override
    protected void onPause() {
        super.onPause();
        sh.pause("bg_end");
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus && model.isFirstStart()) {
            graphics();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void listeners() {
        Std.defineOnTouch(findViewById(R.id.fz4_end_button_comment), new Runnable() {
            @Override
            public void run() {
                rate();
            }
        });
    }

    /**
     * Rates the app
     */
    private void rate() {
        final String appPackageName = getPackageName();
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    private void graphics() {
        Glide.with(this).load(
                Data.getImage(this, "fz4_end")
        ).into((ImageView) findViewById(R.id.fz4_end_background));
        Glide.with(this).load(R.drawable.fz4_fixed_logo).into((ImageView) findViewById(R.id.fz4_end_logo));
        Glide.with(this).load(R.drawable.fz4_rate).into((ImageView) findViewById(R.id.fz4_end_button_comment));
    }
}
