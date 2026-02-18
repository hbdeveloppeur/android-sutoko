/*
 * Copyright (C) PurpleTear, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Hocine Belbouab <hbdeveloppeur@gmail.com>, 1/10/19 11:38 AM
 *
 */
package purpletear.fr.purpleteartools

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.res.Resources
import android.graphics.Color
import android.os.*
import android.text.Html
import android.text.Spanned
import android.util.Log
import android.util.TypedValue
import android.view.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.regex.Pattern

object Std {

    @Throws(IOException::class)
    fun getFileFromAssets(context: Context, fileName: String): File =
        File(context.cacheDir, fileName)
            .also {
                it.outputStream()
                    .use { cache -> context.assets.open(fileName).use { it.copyTo(cache) } }
            }

    /**
     * Returns the id of the raw resources given the mname of the resource and the context.
     * @param name the mname of the resource (without the extension)
     * @param context the calling activity context
     * @return id of the drawable.
     */
    fun getDrawableFrom(name: String, context: Context): Int? {
        val id = context.resources.getIdentifier(name, "drawable", context.packageName)
        return if (id == 0) null else id
    }


    fun dpToPx(dip: Float, r: Resources): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dip,
            r.displayMetrics
        ).toInt()
    }

    @SuppressLint("InlinedApi")
    fun hideStatusBar(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.window?.insetsController?.let { insetsController ->
                insetsController.hide(WindowInsets.Type.statusBars())
                insetsController.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.window?.apply {
                addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    @Suppress("DEPRECATION")
                    decorView.systemUiVisibility =
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                } else {
                    @Suppress("DEPRECATION")
                    decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                }
                statusBarColor = Color.TRANSPARENT
            }
        }
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
    fun confirm(
        title: String,
        message: String?,
        positiveText: String,
        negativeText: String,
        positive: Runnable?,
        negative: Runnable?,
        context: Context
    ) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        if (message != null) {
            builder.setMessage(message)
        }
        builder.setNegativeButton(negativeText) { dialog, id ->
            if (negative != null) {
                Handler(context.mainLooper).post(negative)
            }
        }
        builder.setOnCancelListener {
            if (negative != null) {
                Handler(context.mainLooper).post(negative)
            }
        }
        builder.setPositiveButton(positiveText) { dialog, id ->
            if (positive != null) {
                Handler(context.mainLooper).post(positive)
            }
        }
        val dialog = builder.create()
        dialog.show()
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
    fun confirm(
        title: String,
        positiveText: String,
        negativeText: String,
        positive: Runnable?,
        negative: Runnable?,
        context: Context
    ) {
        confirm(title, null, positiveText, negativeText, positive, negative, context)
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
    fun confirm(
        c: Context, titleId: Int,
        positiveId: Int,
        negativeId: Int,
        positive: Runnable?,
        negative: Runnable?
    ) {
        MaterialAlertDialogBuilder(c)
            .setTitle(c.getString(titleId))
            .setPositiveButton(c.getString(positiveId)) { dialog: DialogInterface?, which: Int ->
                Handler(Looper.getMainLooper())
                    .post(
                        positive!!
                    )
            }
            .setNegativeButton(c.getString(negativeId)) { dialog: DialogInterface?, which: Int ->
                Handler(Looper.getMainLooper())
                    .post(
                        negative!!
                    )
            }
            .show()
    }

    /**
     * Builds an alert dialog
     *
     * @param c
     * @param titleId
     * @param messageId
     * @param actionId
     * @param positive
     */
    fun confirm(
        c: Context, titleId: Int,
        messageId: Int,
        actionId: Int,
        positive: Runnable?
    ) {
        MaterialAlertDialogBuilder(c)
            .setTitle(c.getString(titleId))
            .setMessage(c.getString(messageId))
            .setPositiveButton(c.getString(actionId)) { dialog: DialogInterface?, which: Int ->
                Handler(Looper.getMainLooper())
                    .post(
                        positive!!
                    )
            }
            .show()
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
    fun confirm(
        c: Context, titleId: Int, messageId: Int,
        positiveId: Int,
        negativeId: Int,
        positive: Runnable?,
        negative: Runnable?
    ) {
        MaterialAlertDialogBuilder(c)
            .setTitle(c.getString(titleId))
            .setMessage(c.getString(messageId))
            .setPositiveButton(c.getString(positiveId)) { dialog: DialogInterface?, which: Int ->
                Handler(Looper.getMainLooper())
                    .post(
                        positive!!
                    )
            }
            .setNegativeButton(c.getString(negativeId)) { dialog: DialogInterface?, which: Int ->
                Handler(Looper.getMainLooper())
                    .post(
                        negative!!
                    )
            }
            .show()
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
    fun confirm(
        c: Context, titleId: Int, messageId: Int,
        positiveId: Int,
        neutralId: Int,
        negativeId: Int,
        positive: Runnable?,
        neutral: Runnable?,
        negative: Runnable?
    ) {
        MaterialAlertDialogBuilder(c)
            .setTitle(c.getString(titleId))
            .setMessage(c.getString(messageId))
            .setNeutralButton(c.getString(negativeId)) { dialog: DialogInterface?, which: Int ->
                Handler(Looper.getMainLooper())
                    .post(
                        negative!!
                    )
            }
            .setPositiveButton(c.getString(positiveId)) { dialog: DialogInterface?, which: Int ->
                Handler(Looper.getMainLooper())
                    .post(
                        positive!!
                    )
            }
            .setNegativeButton(c.getString(neutralId)) { dialog: DialogInterface?, which: Int ->
                Handler(Looper.getMainLooper())
                    .post(
                        neutral!!
                    )
            }
            .show()
    }

    /**
     * Builds an alert dialog
     *
     * @param c          Context
     * @param titleId    int
     * @param positiveId int
     * @param positive   Runnable
     */
    fun confirm(
        c: Context, titleId: Int,
        positiveId: Int,
        positive: Runnable?
    ) {
        confirm(
            c.getString(
                titleId
            ),
            c.getString(positiveId),
            "",
            positive,
            positive,
            c
        )
    }

    /**
     * Interprets HTML
     *
     * @param html HTML
     * @return Spanned
     */
    private fun fromHtml(html: String): Spanned {
        val result: Spanned
        result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(html)
        }
        return result
    }


    fun vibrate(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }

    /**
     * Vibrates
     *
     * @param activity Activity
     */
    @SuppressLint("MissingPermission")
    fun vibrate(activity: Activity) {
        val v = activity.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (v != null && v.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= 26) {
                v.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                v.vibrate(150)
            }
        }
    }

    /**
     * Reads the content of an html file given his name.
     *
     * @param filename the name of the file (Not the extension)
     * @param a        the calling activity
     * @return the content of the file
     */
    fun getTextFromHtmlFile(filename: String, a: Activity): Spanned {
        val `is`: InputStream
        var str = ""
        try {
            `is` = a.assets.open("html/$filename.html")
            val size = `is`.available()
            val buffer = ByteArray(size)
            val read = `is`.read(buffer)
            debug("$read characters read for $filename")
            `is`.close()
            str = String(buffer)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return fromHtml(str)
    }

    /**
     * Display a message when debug mode.
     *
     * @param message the message to display
     */
    fun debug(code: Code?, vararg message: Any?) {
        val tag = "PurpleTearDebug"
        val text = StringBuilder()
        for (m in message) {
            text.append(m)
            text.append(" ")
        }
        if (BuildConfig.DEBUG) {
            when (code) {
                Code.ERROR -> Log.e(tag, text.toString())
                Code.VERBOSE -> Log.v(tag, text.toString())
                else -> Log.d(tag, text.toString())
            }
        }
    }

    /**
     * Displays a message when debug mode in verbose
     *
     * @param message Object...
     */
    @JvmStatic
    fun debug(vararg message: Any?) {
        debug(Code.VERBOSE, *message)
    }

    /**
     * Finds the regex occurence inside a String
     *
     * @param str   String
     * @param regex String
     * @return String
     */
    fun find(str: String, regex: String): String {
        val pattern = Pattern.compile(regex)
        val matcher = pattern.matcher(str)
        require(matcher.find()) { "Didn't find regex $regex for str : ($str)" }
        return matcher.group()
    }

    /**
     * Returns the resource's id from its name
     *
     * @param c         Context
     * @param name      String
     * @param type      String
     * @param defaultId the default id, -1 if you want to throw IllegalArgumentException else
     * @return int
     */
    @JvmStatic
    fun getResourceIdFromName(c: Context, name: String, type: String, defaultId: Int): Int {
        val id = c.resources.getIdentifier(name, type, c.packageName)
        if (id == 0) {
            if (defaultId != -1) {
                return defaultId
            }
            throw IllegalArgumentException("$type resource name $name not found")
        }
        debug(id == defaultId)
        return id
    }

    enum class Code {
        ERROR, VERBOSE
    }
}