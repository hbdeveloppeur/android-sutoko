package fr.purpletear.friendzone4.game.activities.splash;

import android.view.View;

import com.bumptech.glide.RequestManager;

class SplashGraphics {

    SplashGraphics() {

    }

    /**
     * Reloads the animation view
     *
     * @param glide RequestManager
     * @param v     View
     */
    void reload(RequestManager glide, View v) {
        v.setVisibility(View.GONE);
        clearImage(glide, v);
    }


    /**
     * Clears the logo animation image
     *
     * @param glide RequestManager
     * @param i     the imageView
     */
    private void clearImage(RequestManager glide, View i) {
        if (i != null) {
            glide.clear(i);
        }
    }
}
