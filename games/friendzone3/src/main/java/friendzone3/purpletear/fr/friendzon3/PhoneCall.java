package friendzone3.purpletear.fr.friendzon3;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.example.sharedelements.OnlineAssetsManager;

import friendzone3.purpletear.fr.friendzon3.handlers.MemoryHandler;
import purpletear.fr.purpleteartools.GlobalData;

public class PhoneCall extends AppCompatActivity {
    private MemoryHandler mh = new MemoryHandler();
    private Boolean isFirstStart = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fz3_activity_phone_call);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus && isFirstStart) {
            isFirstStart = false;
            setImages();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        vibrate(3);
    }

    private void vibrate(final int n) {
        if(n == 0) {
            finish();
            return;
        }
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (v!=null && v.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= 26) {
                v.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                v.vibrate(150);
            }
        }

        Handler handler = new Handler();
        Runnable runnable;
        handler.postDelayed(runnable = new Runnable() {
            @Override
            public void run() {
                vibrate(n - 1);
            }
        }, 1000);
        mh.push(handler, runnable);
    }
    private void setImages() {
        RequestManager requestManager = Glide.with(this);
        requestManager.load(lazyDeveloper("friendzone3_phone_hour")).into((ImageView) findViewById(R.id.phonecall_hour));
        requestManager.load(lazyDeveloper("friendzone3_phone_ico_call_1")).into((ImageView) findViewById(R.id.fz3_phone_call_icon_1));
        requestManager.load(lazyDeveloper("friendzone3_phone_ico_battery")).into((ImageView) findViewById(R.id.fz3_phone_call_icon_2));
        requestManager.load(lazyDeveloper("friendzone3_phone_call_background")).into((ImageView) findViewById(R.id.fz3_phone_call_background));
        requestManager.load(lazyDeveloper("friendzone3_phone_call_btn_1")).into((ImageView) findViewById(R.id.fz3_phone_call_btn1));
        requestManager.load(lazyDeveloper("friendzone3_phone_call_btn_2a")).into((ImageView) findViewById(R.id.fz3_phone_call_btn2));
    }

    private String lazyDeveloper(String name) {
        return OnlineAssetsManager.INSTANCE.getImageFilePath(this, String.valueOf(GlobalData.Game.FRIENDZONE3.getId()), name);
    }
}

