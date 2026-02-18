package fr.purpletear.friendzone4.game.activities.phonecall;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import fr.purpletear.friendzone4.R;
import fr.purpletear.friendzone4.factories.Data;
import fr.purpletear.friendzone4.purpleTearTools.MemoryHandler;
import fr.purpletear.friendzone4.purpleTearTools.Runnable2;

@SuppressWarnings("ALL")
class PhoneCallGraphics {

    private int seconds;


    public PhoneCallGraphics() {
        seconds = 0;
    }

    /**
     * Sets the Activity's ImageViews using the Glide RequestManager
     *
     * @param a     Activity
     * @param Glide RequestManager
     */
    void setImages(Activity a, RequestManager Glide) {
        Glide.load(Data.getImage(a, "fz4_phonecallok")).apply(new RequestOptions().format(DecodeFormat.PREFER_RGB_565).override(Target.SIZE_ORIGINAL)).into((ImageView) a.findViewById(R.id.fz4_phone_call_button_ok));
        Glide.load(Data.getImage(a, "fz4_phonecallabort")).apply(new RequestOptions().format(DecodeFormat.PREFER_RGB_565).override(Target.SIZE_ORIGINAL)).into((ImageView) a.findViewById(R.id.fz4_phone_call_button_abort));
        Glide.load(Data.getImage(a, "fz4_lucie_phone_call")).apply(new RequestOptions().format(DecodeFormat.PREFER_RGB_565).override(Target.SIZE_ORIGINAL)).into((ImageView) a.findViewById(R.id.fz4_phone_call_image_contact));
        Glide.load(Data.getImage(a, "fz4_phonecallfilter")).into((ImageView) a.findViewById(R.id.fz4_phone_call_image_filter));
    }

    /**
     * Sets the contact name graphics
     *
     * @param a    Activity
     * @param name String
     */
    void setContactName(Activity a, String name) {
        ((TextView) a.findViewById(R.id.fz4_phone_call_text_contact_name)).setText(name);
    }

    /**
     * Hides the phone buttons
     *
     * @param a
     */
    void hideButtons(Activity a) {
        a.findViewById(R.id.fz4_phone_call_button_ok).setVisibility(View.GONE);
        a.findViewById(R.id.fz4_phone_call_button_abort).setVisibility(View.GONE);
    }

    /**
     * Recurse to count
     *
     * @param mh MemoryHandler
     */
    void recurseCounter(final Activity a, final MemoryHandler mh) {
        setCounterText(a);
        Runnable2 runnable = new Runnable2("counter", 1000) {
            @Override
            public void run() {
                seconds++;
                recurseCounter(a, mh);
            }
        };
        mh.push(runnable);
        mh.run(runnable);
    }

    /**
     * Stops the counter
     * @param a Activity
     * @param mh MemoryHandler
     */
    void stopCounter(Activity a, MemoryHandler mh) {
        a.findViewById(R.id.fz4_phone_call_text_counter).setVisibility(View.GONE);
        mh.removeAll("counter");
    }

    /**
     * Sets the counter's text
     *
     * @param a Activity
     */
    private void setCounterText(Activity a) {
        ((TextView) a.findViewById(R.id.fz4_phone_call_text_counter)).setText(a.getString(R.string.counter_text_format, getFormatedSeconds()));
    }

    /**
     * Returns the formated seconds
     *
     * @param seconds int
     * @return String
     */
    private String getFormatedSeconds() {
        if (seconds >= 0 && seconds < 10) {
            return String.valueOf("0" + seconds);
        }
        return String.valueOf(seconds);
    }
}
