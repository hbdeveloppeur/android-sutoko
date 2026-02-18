package fr.purpletear.friendzone4.purpleTearTools;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import fr.purpletear.friendzone4.factories.Std;

public class Finger {
    private static Rect rect;

    /**
     * Determines if the user finger is out of the rect or not.
     */
     public static boolean isFingerIn(MotionEvent event, View v) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                rect = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
                v.getHitRect(rect);
                return true;
            default:
                return (rect.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY()));
        }
    }

    /**
     * Defines the event onTouch and the action to run when it get fired.
     * @param v the view
     * @param c the context
     * @param r the action
     */
    public static void defineOnTouch(View v, final Context c, final Runnable r){
        if(v == null)
            return;
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Handler(c.getMainLooper()).post(r);
            }
        });
        v.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(isFingerIn(event, v) && event.getAction() == MotionEvent.ACTION_UP)
                    v.performClick();
                return true;
            }
        });
    }

    /**
     * Defines the event onTouch and the action to run when it get fired.
     * @param v the view
     * @param r the action
     */
    public static void defineOnLongTouch(View v, final Runnable r, final Runnable onSimpleTouch, final long duration){
        if(v == null)
            return;

        v.setOnTouchListener(new View.OnTouchListener() {
            Thread thread;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(isFingerIn(event, v) && event.getAction() == MotionEvent.ACTION_DOWN) {
                     thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(duration);
                                new Handler(Looper.getMainLooper()).post(r);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    thread.start();
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    if(thread.isAlive()) {
                        new Handler().post(onSimpleTouch);
                    }
                    thread.interrupt();
                    thread = null;
                    return false;
                } else if(event.getActionMasked() == MotionEvent.ACTION_UP ||
                        event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
                    if(thread != null && thread.isAlive()) {
                        thread.interrupt();
                        thread = null;
                    }
                    return false;
                }
                return true;
            }
        });
    }


    /**
     * Defines the event onTouch with more abilities
     * @param v View
     * @param c Context
     * @param in Action to run when the user press in
     * @param up Action to run when the user release the button
     * @param out Action to run when the user move his finger out
     */
    public static void defineOnTouch(View v, final Context c, final Runnable in, final Runnable up, final Runnable out){
        if(v == null)
            return;
        v.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(isFingerIn(event, v) && event.getAction() == MotionEvent.ACTION_DOWN) {
                    new Handler(c.getMainLooper()).post(in);
                } else if(isFingerIn(event, v) && event.getAction() == MotionEvent.ACTION_UP) {
                    new Handler(c.getMainLooper()).post(up);
                }
                else if(!isFingerIn(event, v)) {
                    new Handler(c.getMainLooper()).post(out);
                }
                return true;
            }
        });
    }
}
