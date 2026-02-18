package fr.purpletear.friendzone2.activities.poetry;

import android.app.Activity;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;

import fr.purpletear.friendzone2.R;
import fr.purpletear.friendzone2.tables.Character;
import purpletear.fr.purpleteartools.Animation;

class PoetryGraphics {

    /**
     * Sets the background image
     *
     * @param a          Activity
     * @param rm         RequestManager
     * @param drawableId int
     */
    void setImage(Activity a, RequestManager rm, String drawableId) {
        rm.load(drawableId).into((ImageView) a.findViewById(R.id.poetry_background));
    }

    /**
     * Sets the text
     *
     * @param a    Activity
     * @param text String
     */
    void setText(Activity a, String text) {
        TextView v = a.findViewById(R.id.poetry_text);
        v.setClickable(false);
        v.setFocusableInTouchMode(false);
        v.setFocusable(false);
        v.setText(Character.Companion.updateNames(a, text));
    }
}
