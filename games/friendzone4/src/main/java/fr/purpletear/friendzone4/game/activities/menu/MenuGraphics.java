package fr.purpletear.friendzone4.game.activities.menu;

import android.app.Activity;
import android.net.Uri;
import android.os.Handler;
import android.text.Spanned;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.RequestManager;

import java.io.File;

import fr.purpletear.friendzone4.R;
import fr.purpletear.friendzone4.factories.Std;
import fr.purpletear.friendzone4.purpleTearTools.Animation;
import fr.purpletear.friendzone4.purpleTearTools.Video;
import purpletear.fr.purpleteartools.GlobalData;

class MenuGraphics {
    /**
     * Sets the Activity's images
     *
     * @param a     Activity
     * @param glide RequestManager
     */
    void setImages(Activity a, RequestManager glide) {
        glide
                .load(R.drawable.fz4_fixed_logo)
                .into((ImageView) a.findViewById(R.id.fz4_menu_page_main_image_logo));
        glide
                .load(R.drawable.fz4_ic_volume_on)
                .into((ImageView) a.findViewById(R.id.fz4_menu_main_btn_sound));
    }

    /**
     * Updates the UI sound state image
     *
     * @param a     Activity
     * @param glide RequestManager
     * @param isOn  boolean
     */
    void updateSoundStateImage(Activity a, RequestManager glide, boolean isOn) {
        View v = a.findViewById(R.id.fz4_menu_main_btn_sound);
        glide
                .load(isOn ? R.drawable.fz4_ic_volume_on : R.drawable.fz4_ic_volume_off)
                .into((ImageView) v);
    }

    /**
     * Determines the view id of the page P
     *
     * @param p page type
     * @return view's id
     * @see Menu.Page
     */
    private int pageViewId(Menu.Page p) {
        switch (p) {
            case MAIN:
                return R.id.fz4_menu_page_main;
            case INFO:
                return R.id.fz4_menu_page_info;
            case CHAPTERS:
                return R.id.fz4_menu_page_chapters;
        }
        throw new IllegalArgumentException("Not handled Page with ordinal " + String.valueOf(p.ordinal()));
    }

    /**
     * Displays a page given his linked Page Name
     *
     * @param p Page
     */
    Menu.Page display(Activity a, Menu.Page p) {
        a.findViewById(pageViewId(Menu.Page.MAIN)).setVisibility(p == Menu.Page.MAIN ? View.VISIBLE : View.GONE);
        a.findViewById(pageViewId(Menu.Page.CHAPTERS)).setVisibility(p == Menu.Page.CHAPTERS ? View.VISIBLE : View.GONE);
        a.findViewById(pageViewId(Menu.Page.INFO)).setVisibility(p == Menu.Page.INFO ? View.VISIBLE : View.GONE);
        return p;
    }

    /**
     * Sets the menu title.
     *
     * @param a    Activity
     * @param text String
     */
    void setMenuTitle(Activity a, int number, String text) {
        TextView v = a.findViewById(R.id.fz4_menu_chapters_description_title);
        v.setText(
                a.getString(R.string.title_format, number, text)
        );
    }

    /**
     * Starts the logo menu logo animation
     *
     * @param a Activity
     * @return long animation duration
     */
    long startLogoAppearance(Activity a) {
        return Animation.setAnimation(a.findViewById(R.id.fz4_menu_page_main_image_logo), Animation.Animations.ANIMATION_FADEIN, a);
    }

    /**
     * Starts the components animations
     *
     * @param a        Activity
     * @param duration int
     */
    void startComponentAppearance(final Activity a, long duration) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Std.hideBars(a.getWindow(), false, false);
                Animation.setAnimation(a.findViewById(R.id.fz4_menu_background_video), Animation.Animations.ANIMATION_FADEIN, a);
                Animation.setAnimation(a.findViewById(R.id.fz4_menu_button_play), Animation.Animations.ANIMATION_FADEIN, a);
                Animation.setAnimation(a.findViewById(R.id.fz4_menu_button_boutique), Animation.Animations.ANIMATION_FADEIN, a);
                Animation.setAnimation(a.findViewById(R.id.fz4_menu_text_appversion), Animation.Animations.ANIMATION_FADEIN, a);
                Animation.setAnimation(a.findViewById(R.id.fz4_menu_main_btn_sound), Animation.Animations.ANIMATION_FADEIN, a);
            }
        };

        new Handler().postDelayed(runnable, duration + 1280);
    }

    /**
     * Changes the video's state
     * @param a Activity
     * @param isRunning boolean
     */
    void videoState(Activity a, boolean isRunning) {
        if(isRunning) {
            Video.put((VideoView) a.findViewById(R.id.fz4_menu_background_video),
                    Uri.parse("android.resource://" + a.getPackageName() + File.separator + R.raw.bg_menu),
                    true);
            return;
        }

        Video.pause((VideoView) a.findViewById(R.id.fz4_menu_background_video));
    }

    /**
     * Enables or disable a navigation button
     *
     * @param a          Activity
     * @param navigation ChapterNavigation
     * @param isEnabled  boolean
     */
    void updateNavigationButtonsAbility(Activity a, MenuModel.Navigation navigation, boolean isEnabled) {
        switch (navigation) {
            case NEXT: {
                View v = a.findViewById(R.id.fz4_menu_chapters_next);
                updateNavigationButtonsAbility(
                        v,
                        isEnabled
                );
                break;
            }
            case PREVIOUS: {
                View v = a.findViewById(R.id.fz4_menu_chapters_previous);
                updateNavigationButtonsAbility(
                        v,
                        isEnabled
                );
                break;
            }
        }
    }

    /**
     * Enables or disable a navigation button
     *
     * @param v         View
     * @param isEnabled boolean
     */
    private void updateNavigationButtonsAbility(View v, boolean isEnabled) {
        v.setEnabled(isEnabled);
        v.setVisibility(isEnabled ? View.VISIBLE : View.INVISIBLE);
    }

    /**
     * Updates the navigation buttons text
     * @param a Activity
     * @param previousChapter int
     * @param nextChapter int
     */
    void updateNavigationButtonsText(Activity a, int previousChapter, int nextChapter) {
        ((TextView) a.findViewById(R.id.fz4_menu_chapters_previous)).setText(a.getString(R.string.menu_chapters_previous, previousChapter));
        ((TextView) a.findViewById(R.id.fz4_menu_chapters_next)).setText(a.getString(R.string.menu_chapters_next, nextChapter));
    }


    /**
     * Fills the textView menu.page.info.content with the content of the file html/info.html
     */
    void setInfoContent(Activity a, Spanned text) {
        ((TextView) a.findViewById(R.id.fz4_menu_page_info_content))
                .setText(text);
    }
}
