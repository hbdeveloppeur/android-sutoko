package fr.purpletear.friendzone4.purpleTearTools;

import android.content.Context;
import android.content.res.Resources;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.TimeUnit;

import fr.purpletear.friendzone4.BuildConfig;


public class Std {
    

    public static boolean in(String code, String[] tab){
        for(String str : tab){
            if(str.equals(code))
                return true;
        }
        return false;
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    /**
     * Defines the event onTouch and the action to run when it get fired.
     * @param v the view
     * @param r the action
     */
    public static void defineOnTouch(View v, final Runnable r){
        if(v == null)
            return;
        v.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(Finger.isFingerIn(event, v) && event.getAction() == MotionEvent.ACTION_UP)
                    new Handler().post(r);
                return true;
            }
        });
    }

    public enum Code {
        ERROR,
        VERBOSE
    }

    /**
     * Display a message when debug mode.
     * @param message the message to display
     */
    public static void debug(String message, Code code) {
        String tag = "PurpleTearDebug";
        if(BuildConfig.DEBUG) {
            switch(code) {
                case ERROR:
                    Log.e(tag, message);
                    break;
                case VERBOSE:
                    Log.v(tag, message);
                    break;
            }
        }
    }

    public static void debug(String message) {
        Std.debug(message, Code.VERBOSE);
    }

    /**
     * Gets the drawable id from his name.
     * @param name of the drawable
     * @param c the context
     * @return the id of the drawable if not 0, throw an exception else
     */
    public static int drawableFromName(String name, Context c) {
        int id = c.getResources().getIdentifier("fz4_" + name, "drawable", c.getPackageName());
        if(id == 0) {
            throw new IllegalArgumentException(name + " not found");
        }
        return id;
    }

    /**
     * Gets text form an url
     * @param link the url link
     * @return the text from that url
     */
    public static String getTextFromUrl(String link){
        StringBuilder str = new StringBuilder();

        try{
            URL url = new URL(link);
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;

            try {
                while ((line = br.readLine()) != null) {
                    str.append(line);
                }
            } finally {
                br.close();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return str.toString();
    }

    /**
     * Returns the percent value of current compared to max
     * @param current double
     * @param max double
     * @return double
     */
    public static double percent(double current, double max) {
        if(0 == current) {
            Std.debug("STD::PERCENT : current == 0", Code.ERROR);
            return 0;
        } else if(0 == max) {
            Std.debug("STD::PERCENT : max == 0", Code.ERROR);
        }
        return current * 100 / max;
    }

    public static int dpToPx(float dip, Resources r) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dip,
                r.getDisplayMetrics()
        );
    }

    /**
     *
     * @param id int
     * @param r Resource
     * @return float
     */
    public static float dpFromResource(int id, Resources r) {
        return r.getDimension(id) / r.getDisplayMetrics().density;
    }

    /**
     * Returns the current TimeStamp in Seconds
     * @return long
     */
    public static long getCurrentTimeStampSeconds() {
        return TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
    }

    /**
     * Returns the current TimeStamp in Milliseconds
     * @return long
     */
    public static long getCurrentTimeStampMilliseconds() {
        return System.currentTimeMillis();
    }

    public static void setMargins (View v, int l, int t, int r, int b) {
        if (!(v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams)) {
            throw new IllegalArgumentException();
        }
        ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
        p.setMargins(l, t, r, b);
        v.requestLayout();
    }

    public static void setLeftMargin(View v,  int margin) {
        Std.setMargins(v, margin, 0, 0, 0);
    }

    public static void setViewSize(View v, int width, int height) {
        if (!(v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams)) {
            throw new IllegalArgumentException();
        }
        ViewGroup.LayoutParams p = v.getLayoutParams();
        p.width = width;
        p.height = height;
        v.setLayoutParams(p);
    }
}
