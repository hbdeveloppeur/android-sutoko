package fr.purpletear.friendzone4.factories;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.purpletear.friendzone4.BuildConfig;

public class Std {

    /**
     * Build an AlertDialog.
     *
     * @param title        The title
     * @param positiveText the text of the positive button
     * @param negativeText the text of the negative button
     * @param positive     the runnable of the positive button
     * @param negative     the runnable of the negative button
     * @param context      the concerned context
     */
    static void confirm(String title,
                        @Nullable String message,
                        String positiveText,
                        String negativeText,
                        final Runnable positive,
                        final Runnable negative,
                        final Context context) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        builder.setTitle(title);
        if (message != null) {
            builder.setMessage(message);
        }
        builder.setNegativeButton(negativeText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (negative != null) {
                    new Handler(context.getMainLooper()).post(negative);
                }
            }
        });

        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                if (negative != null) {
                    new Handler(context.getMainLooper()).post(negative);
                }
            }
        });

        builder.setPositiveButton(positiveText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (positive != null) {
                    new Handler(context.getMainLooper()).post(positive);
                }
            }
        });

        final android.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Build an AlertDialog.
     *
     * @param title        The title
     * @param positiveText the text of the positive button
     * @param negativeText the text of the negative button
     * @param positive     the runnable of the positive button
     * @param negative     the runnable of the negative button
     * @param context      the concerned context
     */
    public static void confirm(String title,
                               String positiveText,
                               String negativeText,
                               final Runnable positive,
                               final Runnable negative,
                               Context context) {
        confirm(title, null, positiveText, negativeText, positive, negative, context);
    }

    /**
     * Builds an alert dialog
     *
     * @param c          Context
     * @param titleId    int
     * @param positiveId int
     * @param negativeId int
     * @param positive   Runnable
     * @param negative   Runnable
     */
    public static void confirm(Context c, int titleId,
                               int positiveId,
                               int negativeId,
                               final Runnable positive,
                               final Runnable negative) {
        confirm(c.getString(
                titleId),
                c.getString(positiveId),
                c.getString(negativeId),
                positive,
                negative,
                c);
    }

    /**
     * Builds an alert dialog
     *
     * @param c          Context
     * @param titleId    int
     * @param positiveId int
     * @param messageId  int
     * @param negativeId int
     * @param positive   Runnable
     * @param negative   Runnable
     */
    public static void confirm(Context c, int titleId,
                               int messageId,
                               int positiveId,
                               int negativeId,
                               final Runnable positive,
                               final Runnable negative) {
        confirm(c.getString(
                titleId),
                c.getString(messageId),
                c.getString(positiveId),
                c.getString(negativeId),
                positive,
                negative,
                c);
    }

    /**
     * Interprets HTML
     *
     * @param html HTML
     * @return Spanned
     */
    private static Spanned fromHtml(String html) {
        Spanned result;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            result = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(html);
        }
        return result;
    }

    /**
     * Reads the content of an html file given his name.
     *
     * @param filename the name of the file (Not the extension)
     * @param a        the calling activity
     * @return the content of the file
     */
    public static Spanned getTextFromHtmlFile(String filename, Activity a) {
        InputStream is;
        String str = "";
        try {
            is = a.getAssets().open("html/" + filename + ".html");
            int size = is.available();

            byte[] buffer = new byte[size];
            int read = is.read(buffer);
            debug(String.valueOf(read) + " characters read for " + filename);
            is.close();
            str = new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fromHtml(str);
    }

    public enum Code {
        ERROR,
        VERBOSE
    }

    /**
     * Display a message when debug mode.
     *
     * @param message the message to display
     */
    public static void debug(Code code, Object... message) {
        String tag = "PurpleTearDebug";
        StringBuilder text = new StringBuilder();
        for (Object m : message) {
            text.append(String.valueOf(m));
            text.append(" ");
        }

        if (BuildConfig.DEBUG) {
            switch (code) {
                case ERROR:
                    Log.e(tag, text.toString());
                    break;
                case VERBOSE:
                    Log.v(tag, text.toString());
                    break;
            }
        }
    }

    /**
     * Displays a message when debug mode in verbose
     *
     * @param message Object...
     */
    public static void debug(Object... message) {
        debug(Code.VERBOSE, message);
    }

    /**
     * Finds the regex occurence inside a String
     *
     * @param str   String
     * @param regex String
     * @return String
     */
    public static String find(String str, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Didn't find regex " + regex + " for str : (" + str + ")");
        }
        return matcher.group();
    }


    /**
     * Hides the status and nav bar
     *
     * @param window Window
     */
    public static void hideBars(Window window, boolean hideNavBar, boolean hideStatusBar) {
        int options;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            if (hideNavBar && hideStatusBar) {
                options = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
            } else if (hideNavBar) {
                options = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            } else {
                options = View.SYSTEM_UI_FLAG_FULLSCREEN;
            }
            View decorView = window.getDecorView();
            decorView.setSystemUiVisibility(options);
        } else {
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    /**
     * Animations a View's background given the animation resource name.
     *
     * @param a    Activity
     * @param i    View
     * @param name String
     */
    public static void animateBackground(final Activity a, final View i, final String name) {
        a.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int id = a.getResources().getIdentifier(name, "drawable", a.getPackageName());
                if (id == 0) {
                    throw new IllegalArgumentException("Animation" + " " + name + " " + "Not found");
                }
                i.setBackgroundResource(id);
                final AnimationDrawable d = (AnimationDrawable) i.getBackground();
                d.setCallback(i);
                d.setVisible(true, true);
                d.start();
            }
        });
    }

    /**
     * Makes the phone vibrating
     *
     * @param o Object
     */
    public static void vibrate(final Object o) {
        try {
            final Vibrator v = (Vibrator) o;
            if (v != null && v.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= 26) {
                    v.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    v.vibrate(150);
                }
            }
        } catch (Exception e) {
            Std.debug(e.getMessage());
        }
    }
}
