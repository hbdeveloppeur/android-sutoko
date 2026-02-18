package fr.purpletear.friendzone4.game.activities.poetry;

import android.app.Activity;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;

import fr.purpletear.friendzone4.GameData;
import fr.purpletear.friendzone4.R;
import fr.purpletear.friendzone4.purpleTearTools.Animation;

class PoetryGraphics {

    /**
     * Sets the background image
     *
     * @param a          Activity
     * @param rm         RequestManager
     * @param drawableId int
     */
    void setImage(Activity a, RequestManager rm, String drawableId) {
        rm.load(drawableId).into((ImageView) a.findViewById(R.id.fz4_poetry_background));
    }

    /**
     * Sets the text
     *
     * @param a    Activity
     * @param text String
     */
    void setText(Activity a, String text) {
        TextView v = a.findViewById(R.id.fz4_poetry_text);
        v.setClickable(false);
        v.setFocusableInTouchMode(false);
        v.setFocusable(false);
        v.setText(GameData.INSTANCE.updateNames(a, text));
    }

    /**
     * Changes the textVisibility with a fade animation
     *
     * @param a         Activity
     * @param isVisible boolean
     */
    void setTextVisibility(Activity a, boolean isVisible) {
        Animation.setAnimation(
                a.findViewById(R.id.fz4_poetry_text),
                isVisible ? Animation.Animations.ANIMATION_FADEIN : Animation.Animations.ANIMATION_FADEOUT,
                a
        );
    }
}
