package friendzone3.purpletear.fr.friendzon3;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.example.sutokosharedelements.OnlineAssetsManager;

import friendzone3.purpletear.fr.friendzon3.handlers.SoundHandler;
import purpletear.fr.purpleteartools.Finger;
import purpletear.fr.purpleteartools.GlobalData;

public class End extends AppCompatActivity {
    private SoundHandler sh = new SoundHandler();
    private Boolean isFirstStart = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fz3_activity_end);
        listeners();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus && isFirstStart) {
            isFirstStart = false;
            setImages(this, Glide.with(this));
        }
    }

    void setImages(Activity activity, RequestManager requestManager) {
        requestManager.load(
                OnlineAssetsManager.INSTANCE.getImageFilePath(activity, String.valueOf(GlobalData.Game.FRIENDZONE3.getId()), "friendzone3_end")
        ).into((ImageView) activity.findViewById(R.id.friendzone3_end_image_background));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    private void listeners() {
        Finger.Companion.defineOnTouch(findViewById(R.id.end_button_comment), new Runnable() {
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
}
