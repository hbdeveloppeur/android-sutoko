package friendzone3.purpletear.fr.friendzon3;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.example.sharedelements.OnlineAssetsManager;

import friendzone3.purpletear.fr.friendzon3.handlers.SoundHandler;
import purpletear.fr.purpleteartools.Finger;
import purpletear.fr.purpleteartools.GlobalData;
import purpletear.fr.purpleteartools.Std;

public class Phone extends AppCompatActivity {
    private SoundHandler sh = new SoundHandler();
    private Boolean isFirstStart = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fz3_activity_phone);
        listeners();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus && isFirstStart) {
            isFirstStart = false;
            setImages();
        }
    }

    /**
     *
     */
    private void listeners() {
        Finger.Companion.defineOnTouch(findViewById(R.id.phone_btn_call), new Runnable() {
            @Override
            public void run() {
                sh.generate("wrong", Phone.this, false);
            }
        });

        Finger.Companion.defineOnTouch(findViewById(R.id.phone_btn_photos), new Runnable() {
            @Override
            public void run() {
                sh.generate("wrong", Phone.this, false);
            }
        });

        Finger.Companion.defineOnTouch(findViewById(R.id.phone_btn_messages), new Runnable() {
            @Override
            public void run() {
                call();
            }
        });
    }

    private void call() {
        startActivity(new Intent(Phone.this, PhoneCall.class));
    }


    private void setImages() {
        RequestManager requestManager = Glide.with(this);
        requestManager.load(lazyDeveloper("friendzone3_phone_hour")).into((ImageView) findViewById(R.id.fz3_phone_clock));
        requestManager.load(lazyDeveloper("friendzone3_btn_call")).into((ImageView) findViewById(R.id.phone_btn_call));
        requestManager.load(lazyDeveloper("friendzone3_btn_photos")).into((ImageView) findViewById(R.id.phone_btn_photos));
        requestManager.load(lazyDeveloper("friendzone3_btn_sms")).into((ImageView) findViewById(R.id.phone_btn_messages));
        requestManager.load(lazyDeveloper("friendzone3_phone_background")).into((ImageView) findViewById(R.id.fz3_phone_background));
        requestManager.load(lazyDeveloper("friendzone3_phone_ico_call_1")).into((ImageView) findViewById(R.id.fz3_phone_ico_call));
        requestManager.load(lazyDeveloper("friendzone3_phone_ico_battery")).into((ImageView) findViewById(R.id.fz3_phone_ico_battery));
    }

    private String lazyDeveloper(String name) {
        return OnlineAssetsManager.INSTANCE.getImageFilePath(this, GlobalData.Game.FRIENDZONE3.getId(), name);
    }
}
