package fr.purpletear.friendzone4.game.activities.main;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.LayoutTransition;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;

import fr.purpletear.friendzone4.GameData;
import fr.purpletear.friendzone4.R;
import fr.purpletear.friendzone4.factories.Data;
import fr.purpletear.friendzone4.factories.Std;
import fr.purpletear.friendzone4.purpleTearTools.Animation;
import fr.purpletear.friendzone4.purpleTearTools.Measure;
import fr.purpletear.friendzone4.purpleTearTools.MemoryHandler;
import fr.purpletear.friendzone4.purpleTearTools.Runnable2;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

/**
 * Handles the graphic settings of the Main Activity
 *
 * @see Main
 */
class MainGraphics implements Parcelable {

    private String conversationName;
    private String conversationStatus;
    private String videoToReload;
    private String overlaySentence;
    private boolean isOnlinePointVisible;
    private boolean isMainButtonVisible;
    private boolean isFasterButtonVisible;
    private boolean isOverlayNotificationVisible;
    private boolean isContactVisible;
    private boolean isBlackFilterVisible;
    private String conversationPictureId;
    private String overlayImageId;
    private String backgroundImageId;

    protected MainGraphics(Parcel in) {
        this.conversationName = in.readString();
        this.overlaySentence = in.readString();
        this.conversationStatus = in.readString();
        this.videoToReload = in.readString();

        this.isOnlinePointVisible = in.readByte() == 1;
        this.isMainButtonVisible = in.readByte() == 1;
        this.isFasterButtonVisible = in.readByte() == 1;
        this.isOverlayNotificationVisible = in.readByte() == 1;
        this.isContactVisible = in.readByte() == 1;
        this.isBlackFilterVisible = in.readByte() == 1;

        this.conversationPictureId = in.readString();
        this.overlayImageId = in.readString();
        this.backgroundImageId = in.readString();
    }

    public static final Creator<MainGraphics> CREATOR = new Creator<MainGraphics>() {
        @Override
        public MainGraphics createFromParcel(Parcel in) {
            return new MainGraphics(in);
        }

        @Override
        public MainGraphics[] newArray(int size) {
            return new MainGraphics[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(conversationName);
        dest.writeString(overlaySentence);
        dest.writeString(conversationStatus);
        dest.writeString(videoToReload);

        dest.writeByte((byte) (isOnlinePointVisible ? 1 : 0));
        dest.writeByte((byte) (isMainButtonVisible ? 1 : 0));
        dest.writeByte((byte) (isFasterButtonVisible ? 1 : 0));
        dest.writeByte((byte) (isOverlayNotificationVisible ? 1 : 0));
        dest.writeByte((byte) (isContactVisible ? 1 : 0));
        dest.writeByte((byte) (isBlackFilterVisible ? 1 : 0));

        dest.writeString(conversationPictureId);
        dest.writeString(overlayImageId);
        dest.writeString(backgroundImageId);

    }

    public MainGraphics(String conversationName, String conversationStatus, String videoToReload, String overlaySentence, boolean isOnlineButtonVisible, boolean isMainButtonVisible, boolean isFasterButtonVisible, boolean isOverlayNotificationVisible, boolean isContactVisible, boolean isBlackFilterVisible, String conversationPictureId, String overlayImageId, String backgroundImageId) {
        this.conversationName = conversationName;
        this.conversationStatus = conversationStatus;
        this.videoToReload = videoToReload;
        this.overlaySentence = overlaySentence;
        this.isOnlinePointVisible = isOnlineButtonVisible;
        this.isMainButtonVisible = isMainButtonVisible;
        this.isFasterButtonVisible = isFasterButtonVisible;
        this.isOverlayNotificationVisible = isOverlayNotificationVisible;
        this.isContactVisible = isContactVisible;
        this.isBlackFilterVisible = isBlackFilterVisible;
        this.conversationPictureId = conversationPictureId;
        this.overlayImageId = overlayImageId;
        this.backgroundImageId = backgroundImageId;
    }

    /**
     * Sets the Activity's images
     *
     * @param a     Activity
     * @param glide RequestManager
     */
    void setImages(Activity a, RequestManager glide, boolean isConversation) {

        if (isConversation) {
            glide.load(Data.getImage(a, "fz4_online_point")).into((ImageView) a.findViewById(R.id.fz4_mainactivity_online_point));
            glide.load(Data.getImage(a, "fz4_btn_pause")).into((ImageView) a.findViewById(R.id.fz4_mainactivity_button_pause));
            glide.load(Data.getImage(a, "fz4_ico_call")).into((ImageView) a.findViewById(R.id.fz4_mainactivity_icons_call));
            glide.load(Data.getImage(a, "fz4_ico_share")).into((ImageView) a.findViewById(R.id.fz4_mainactivity_icons_share));
            glide.load(Data.getImage(a, "fz4_ico_info")).into((ImageView) a.findViewById(R.id.fz4_mainactivity_icons_info));
        } else {
            a.findViewById(R.id.fz4_mainactivity_button_pause).setVisibility(View.GONE);
            a.findViewById(R.id.fz4_mainactivity_icons_call).setVisibility(View.GONE);
            a.findViewById(R.id.fz4_mainactivity_icons_share).setVisibility(View.GONE);
            a.findViewById(R.id.fz4_mainactivity_icons_info).setVisibility(View.GONE);
        }
    }

    /**
     * Updates the game state button
     *
     * @param gs    GameState
     * @param a     Activity
     * @param glide RequestManager
     */
    void updateGameStateButton(Activity a, RequestManager glide, MainModel.GameState gs) {
        switch (gs) {
            case PLAYING:
                glide.load(Data.getImage(a, "fz4_btn_pause")).into((ImageView) a.findViewById(R.id.fz4_mainactivity_button_pause));
                break;
            case USER_PAUSED:
                glide.load(Data.getImage(a, "fz4_btn_play")).into((ImageView) a.findViewById(R.id.fz4_mainactivity_button_pause));
                break;
        }
    }


    void setContactOptionIcon(Activity a, RequestManager glide) {
        glide.load(R.drawable.fz4_ic_options_grey).into((ImageView) a.findViewById(R.id.fz4_main_contacts_options));
    }

    void setContactName(Activity a, String name) {
        ((TextView) a.findViewById(R.id.fz4_main_contacts_name)).setText(GameData.INSTANCE.updateNames(a, name));
        ((TextView) a.findViewById(R.id.fz4_main_contacts_title)).setText(GameData.INSTANCE.updateNames(a, a.getString(R.string.contact_messages)));
    }

    /**
     * Sets the title's text
     *
     * @param a    Activity
     * @param text String
     */
    void setTitle(Activity a, String text) {
        ((TextView) a.findViewById(R.id.fz4_mainactivity_description_title)).setText(GameData.INSTANCE.updateNames(a, text));
    }

    /**
     * Sets the description's text
     *
     * @param a    Activity
     * @param text String
     */
    void setDescription(Activity a, String text) {
        ((TextView) a.findViewById(R.id.fz4_mainactivity_description_content)).setText(GameData.INSTANCE.updateNames(a, text));
    }

    /**
     * Sets the conversation name's text
     *
     * @param a    Activity
     * @param text String
     */
    void setConversationName(Activity a, String text) {
        conversationName = GameData.INSTANCE.updateNames(a, text);
        ((TextView) a.findViewById(R.id.fz4_mainactivity_name)).setText(GameData.INSTANCE.updateNames(a, text));
    }

    /**
     * Sets the conversation name's text
     *
     * @param a Activity
     */
    void setConversationName(Activity a) {
        setConversationName(a, conversationName);
    }

    /**
     * Sets the conversation status's text
     *
     * @param a    Activity
     * @param text String
     */
    void setConversationStatus(Activity a, String text) {
        conversationStatus = GameData.INSTANCE.updateNames(a, text);
        boolean hasOnlinePoint = text.equals(a.getString(R.string.online));
        a.findViewById(R.id.fz4_mainactivity_online_point).setVisibility(hasOnlinePoint ? View.VISIBLE : View.INVISIBLE);
        ((TextView) a.findViewById(R.id.fz4_mainactivity_status)).setText(GameData.INSTANCE.updateNames(a, text));
    }

    /**
     * Sets the conversation status's text
     *
     * @param a Activity
     */
    void setConversationStatus(Activity a) {
        setConversationStatus(a, GameData.INSTANCE.updateNames(a, conversationStatus));
    }

    /**
     * Sets the conversation image
     *
     * @param a               Activity
     * @param imageResourceId int
     * @param glide           RequestManager
     */
    void setConversationImage(Activity a, String imageResourceId, RequestManager glide) {
        conversationPictureId = imageResourceId;
        glide.load(imageResourceId).into((ImageView) a.findViewById(R.id.fz4_mainactivity_photo));
    }

    /**
     * Sets the conversation image
     *
     * @param a     Activity
     * @param glide RequestManager
     */
    void setConversationImage(Activity a, RequestManager glide) {
        setConversationImage(a, conversationPictureId, glide);
    }

    void fadeOutBlackFilter(Activity a) {
        if (!isBlackFilterVisible) {
            a.findViewById(R.id.fz4_main_main_filter_black).setVisibility(View.GONE);
            return;
        }
        isBlackFilterVisible = false;
        Animation.setAnimation(
                a.findViewById(R.id.fz4_main_main_filter_black),
                Animation.Animations.ANIMATION_FADEOUT,
                a
        );
    }

    /**
     * Fades the description screen
     */
    void fadeDescription(Activity a) {
        Animation.setAnimation(
                a.findViewById(R.id.fz4_mainactivity_filter),
                Animation.Animations.ANIMATION_FADEOUT,
                a,
                560
        );
    }

    /**
     * Hides the description.
     *
     * @param a Activity
     */
    void hideDescription(Activity a) {
        a.findViewById(R.id.fz4_mainactivity_filter).setVisibility(View.GONE);
    }

    public String getVideoToReload() {
        return videoToReload;
    }

    /**
     * Hides the description
     *
     * @param a Activity
     */
    void delayDescription(final Activity a, final MemoryHandler mh, final Runnable onCompletion, final int duration) {
        Runnable2 runnable = new Runnable2("description", duration) {
            @Override
            public void run() {
                fadeDescription(a);
                onCompletion.run();
            }
        };
        mh.push(runnable);
        mh.run(runnable);
    }

    /**
     * Sets the main button's visibility
     *
     * @param a         Activity
     * @param isVisible boolean
     */
    void setButtonVisibility(Activity a, boolean isVisible) {
        isMainButtonVisible = isVisible;
        a.findViewById(R.id.fz4_mainactivity_button).setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
    }

    /**
     * Sets the main button's visibility
     *
     * @param a Activity
     */
    void setButtonVisibility(Activity a) {
        setButtonVisibility(a, isMainButtonVisible);
    }

    /**
     * Sets the choice area visibility
     *
     * @param a         Activity
     * @param isVisible boolean
     */
    void setChoiceAreaVisibility(Activity a, boolean isVisible) {
        a.findViewById(R.id.fz4_mainactivity_choicebox_area).setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
    }

    /**
     * Returns the choices parent
     *
     * @param a Activity
     * @return View
     */
    ViewGroup getChoiceParentView(Activity a) {
        return a.findViewById(R.id.fz4_mainactivity_choicebox_parent);
    }

    /**
     * Sets the choicebox's visibility
     *
     * @param a         Activity
     * @param isVisible boolean
     */
    void setChoiceBoxVisibility(Activity a, boolean isVisible) {
        a.findViewById(R.id.fz4_mainactivity_choicebox_area).setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
    }

    /**
     * Sets the recyclerView
     */
    void setRecyclerView(Activity a, RecyclerView.Adapter adapter, Display display) {
        RecyclerView recyclerView = a.findViewById(R.id.fz4_mainActivity_recyclerview_fix);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        recyclerView.setNestedScrollingEnabled(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            ((ViewGroup) a.findViewById(R.id.fz4_main_ll)).getLayoutTransition()
                    .enableTransitionType(LayoutTransition.CHANGING);
        }
        LinearLayoutManager lLayout = new CustomLinearLayoutManager(a, 6);
        lLayout.setStackFromEnd(true);
        recyclerView.addItemDecoration(
                new GameGridItemDecoration(
                        Math.round(Measure.percent(Measure.Type.HEIGHT,
                                1.5f,
                                display
                        )), false));
        recyclerView.setLayoutManager(lLayout);
    }

    /**
     * Sets the recyclerView
     */
    void setContactRecyclerView(Activity a, RecyclerView.Adapter adapter, Display display) {
        RecyclerView recyclerView = a.findViewById(R.id.fz4_main_contacts_recyclerviews);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        recyclerView.setNestedScrollingEnabled(false);
        LinearLayoutManager lLayout = new LinearLayoutManager(a);
        recyclerView.addItemDecoration(
                new GameGridItemDecoration(
                        Math.round(Measure.percent(Measure.Type.HEIGHT,
                                .1f,
                                display
                        )), false));
        recyclerView.setLayoutManager(lLayout);
    }

    /**
     * Scroll the RecyclerView to the given position
     *
     * @param a        Activity
     * @param position int
     */
    void scrollToPosition(final Activity a, final RecyclerView r, final int position, boolean isSmooth) {
        if (isSmooth) {
            r.smoothScrollToPosition(position);
        } else {
            r.scrollToPosition(position);
        }
    }

    /**
     * Sets the status online or offline.
     *
     * @param online the status value.
     */
    void status(Activity a, Boolean online) {
        String text = a.getString(online ? R.string.online : R.string.offline);
        conversationStatus = text;
        a.findViewById(R.id.fz4_mainactivity_online_point).setVisibility(online ? View.VISIBLE : View.INVISIBLE);
        ((TextView) a.findViewById(R.id.fz4_mainactivity_status)).setText(text);
    }

    /**
     * Sets the text and style of the status
     *
     * @param text    String The text to set
     * @param colorId int the id of the color
     */
    void setStatusTextAndStyle(Activity a, String text, int colorId) {
        TextView t = a.findViewById(R.id.fz4_mainactivity_status);
        t.setText(GameData.INSTANCE.updateNames(a, text));
        t.setTextColor(ContextCompat.getColor(a, colorId));
    }

    void glitchit(Activity a, MemoryHandler mh, Runnable onCompletion) {
        View header_black_filter = a.findViewById(R.id.fz4_mainactivity_header_black_filter);
        View black_background = a.findViewById(R.id.fz4_mainactivity_glitch_black_filter);
        glitchBlackScreen(a, mh, header_black_filter, black_background, onCompletion);
    }

    private void glitchBlackScreen(final Activity a, final MemoryHandler mh, final View header, final View glitch, final Runnable onCompletion) {
        setVisibilityBlackScreen(a, header, glitch, true);
        Runnable2 runnable = new Runnable2("glitch", 2500) {
            @Override
            public void run() {
                setVisibilityBlackScreen(a, header, glitch, false);
                Runnable2 runnable = new Runnable2("disappear", 280) {
                    @Override
                    public void run() {
                        if (onCompletion != null) {
                            onCompletion.run();
                        }
                    }
                };
                mh.push(runnable);
                mh.run(runnable);
            }
        };
        mh.push(runnable);
        mh.run(runnable);
    }
    boolean isBlackScreen = false;
    void setVisibilityBlackScreen(final Activity a, final View header, final View glitch, final boolean visibility) {
        isBlackScreen = visibility;
        a.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (visibility) {
                    header.setVisibility(View.VISIBLE);
                    glitch.setVisibility(View.VISIBLE);
                } else {
                    Animation.setAnimation(header, Animation.Animations.ANIMATION_FADEOUT, a);
                    Animation.setAnimation(glitch, Animation.Animations.ANIMATION_FADEOUT, a);
                }
            }
        });
    }

    void focusTrash(Activity a) {
        View v = a.findViewById(R.id.fz4_mainactivity_focus_trash);
        v.requestFocus();
    }

    /**
     * updates the notification overlay's visibility
     *
     * @param a         Activity
     * @param isVisible boolean
     */
    @SuppressWarnings("SameParameterValue")
    void setOverlayNotificationVisibility(Activity a, boolean isVisible) {
        isOverlayNotificationVisible = isVisible;
        a.findViewById(R.id.fz4_mainactivity_overlay).setVisibility(isVisible ? View.VISIBLE : View.GONE);
        a.findViewById(R.id.fz4_mainactivity_overlay_filter).setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    /**
     * Animates the notification overlay
     *
     * @param a Activity
     * @return long animation duration.
     */
    long animateOverlayNotification(Activity a) {
        Animation.setAnimation(
                a.findViewById(R.id.fz4_mainactivity_overlay_filter),
                Animation.Animations.ANIMATION_FADEIN,
                a
        );

        return Animation.setAnimation(
                a.findViewById(R.id.fz4_mainactivity_overlay),
                Animation.Animations.ANIMATION_SLIDE_IN_FROM_LEFT,
                a
        );
    }

    /**
     * Sets the overlay's notification sentence.
     *
     * @param a        Activity
     * @param sentence String
     */
    void setOverlaySentence(Activity a, String sentence) {
        overlaySentence = sentence;
        ((TextView) a.findViewById(R.id.fz4_phrase_notification_text)).setText(GameData.INSTANCE.updateNames(a, sentence));
    }

    /**
     * Sets the overlay's image
     *
     * @param a       Activity
     * @param imageId int
     */
    void setOverlayImage(Activity a, RequestManager rm, String imageId) {
        overlayImageId = imageId;
        rm.load(imageId).into((ImageView) a.findViewById(R.id.fz4_phrase_notification_image));
    }

    /**
     * Sets the overlay color's id
     *
     * @param a       Activity
     * @param colorId int
     */
    void setOverlayColor(Activity a, int colorId) {
        GradientDrawable gd = (GradientDrawable) a.findViewById(R.id.fz4_phrase_notification_text).getBackground();
        gd.setColor(ContextCompat.getColor(a, colorId));
    }

    void setOverlayTextColor(final Activity a, final int colorId) {
        Std.debug(colorId == R.color.colorWhite ? "white" : "black");
        ((TextView) a.findViewById(R.id.fz4_phrase_notification_text)).setTextColor(ContextCompat.getColor(a, colorId));
    }

    private void marginRoot(View root, int value, int width) {
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) root.getLayoutParams();
        lp.leftMargin = value;
        lp.width = width;
        root.setLayoutParams(lp);
    }

    void animateContact(final Activity a, boolean isVisible, final Runnable onCompletion) {
        isContactVisible = isVisible;
        final View v = a.findViewById(R.id.fz4_mainactivity_root);
        float value = (v.getWidth() * .8f);
        final int width = v.getWidth();
        ValueAnimator animator = ValueAnimator.ofInt(
                isVisible ? 0 : (int) value,
                isVisible ? (int) value : 0);
        animator.setDuration(560);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int animatedValue = (int) valueAnimator.getAnimatedValue();
                marginRoot(v, animatedValue, width);
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (onCompletion != null) {
                    new Handler(a.getMainLooper()).post(onCompletion);
                }
                super.onAnimationEnd(animation);
            }
        });
        animator.start();
    }

    /**
     * Updates the UI fasterButton visibility
     *
     * @param a         Activity
     * @param anim      boolean
     * @param isVisible boolean
     */
    void setFasterButtonVisibility(Activity a, RequestManager Glide, boolean anim, boolean isVisible) {
        isFasterButtonVisible = isVisible;
        if (anim && isVisible) {
            Glide.load(Data.getImage(a, "fz4_btn_faster"))
                    .transition(withCrossFade())
                    .into((ImageView) a.findViewById(R.id.fz4_mainactivity_button_faster_build));
        } else if (isVisible) {

            Glide.load(Data.getImage(a, "fz4_btn_faster"))
                    .transition(withCrossFade())
                    .into((ImageView) a.findViewById(R.id.fz4_mainactivity_button_faster_build));
        }
        a.findViewById(R.id.fz4_mainactivity_button_faster_build).setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    void setVideoToReload(String name) {
        videoToReload = name;
    }


    /**
     * Switch the background media to the given image name
     *
     * @param a     Activity
     * @param Glide RequestManager
     * @param id    int
     */
    void switchBackgroundImage(Activity a, RequestManager Glide, String id) {
        backgroundImageId = id;
        ImageView i = a.findViewById(R.id.fz4_mainactivty_background_image);
        Glide.load(backgroundImageId).into(i);
        Animation.setAnimation(
                i,
                Animation.Animations.ANIMATION_FADEIN,
                a
        );

        View r = a.findViewById(R.id.fz4_mainactivity_background);
        Animation.setAnimation(r, Animation.Animations.ANIMATION_FADEIN, a);
    }

    /**
     * Switch the background media to the given image name
     *
     * @param a     Activity
     * @param Glide RequestManager
     */
    void switchBackgroundImage(Activity a, RequestManager Glide) {
        if ("".equals(backgroundImageId)) {
            return;
        }
        switchBackgroundImage(a, Glide, backgroundImageId);
    }

    /**
     * Sets the header alpha
     *
     * @param a             Activity
     * @param isTransparent boolean
     */
    void setHeaderAlpha(Activity a, boolean isTransparent) {
        if (!isTransparent) {
            return;
        }
        a.findViewById(R.id.fz4_mainactivity_header).setBackgroundColor(
                ContextCompat.getColor(a, R.color.game_header_transparent_background)
        );
    }

    void setButtonAlpha(Activity a, boolean isTransparent) {
        if (!isTransparent) {
            return;
        }
        GradientDrawable gd = (GradientDrawable) a.findViewById(R.id.fz4_mainactivity_button_bg).getBackground();
        gd.setColor(ContextCompat.getColor(a, R.color.game_main_button_fade));
        ((TextView) a.findViewById(R.id.fz4_mainactivity_button_text)).setTextColor(ContextCompat.getColor(a, R.color.game_main_button_fade_text));
    }

    /**
     * Updates the timedchoice interface visibility
     *
     * @param a         Activity
     * @param isVisible boolean
     */
    void setTimedChoiceVisibility(Activity a, boolean isVisible) {
        View counter = a.findViewById(R.id.fz4_choicebox_text_counter);
        View progressbar = a.findViewById(R.id.fz4_choicebox_progressbar);

        resetTimedChoiceSize(a, progressbar);

        counter.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        progressbar.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
    }

    /**
     * Resets the timedChoice progressbar size
     *
     * @param a           Activity
     * @param progressbar View
     */
    private void resetTimedChoiceSize(Activity a, View progressbar) {
        View model = a.findViewById(R.id.fz4_choicebox_separator);
        progressbar.getLayoutParams().width = model.getWidth();
    }


    /**
     * Enables the timed choice animation
     *
     * @param a    Activity
     * @param time int
     */
    ValueAnimator enableTimedChoiceAnimation(Activity a, int time) {
        View progressbar = a.findViewById(R.id.fz4_choicebox_progressbar);
        return ChoicesController.progressBarAnimation(
                progressbar,
                time * 1000
        );
    }

    void countAnimation(final Activity a, final MemoryHandler mh, final int s, final MainInterface c, final Phrase p) {
        if (s == 0) {
            c.onMissedChoice(p);
            return;
        }
        ChoicesController.countAnimation(
                a.findViewById(R.id.fz4_choicebox_text_counter),
                s,
                18f,
                12f

        );
        Runnable2 runnable = new Runnable2("countAnimation", 1000) {
            @Override
            public void run() {
                Std.debug("countAnimation");
                countAnimation(a, mh, s - 1, c, p);
            }
        };

        mh.push(runnable);
        mh.run(runnable);
    }

    void setFilterAlpha(Activity a, float alpha) {
        setFilterAlpha(a, alpha, 280);
    }

    /**
     * Updates the UI imageFilter
     * @param a Activity
     * @param alpha Filter alpha value
     */
    void setFilterAlpha(Activity a, float alpha, int duration) {
        View image_filter = a.findViewById(R.id.fz4_mainactivity_image_filter);
        image_filter.setAlpha(alpha);
        Animation.setAnimation(
                image_filter,
                Animation.Animations.ANIMATION_FADEIN,
                a,
                duration
        );
    }

    /**
     * Stops the recyclerView scrolling
     * @param a Activity
     */
    void stopRecyclerViewScroll(Activity a) {
        RecyclerView v = a.findViewById(R.id.fz4_mainActivity_recyclerview_fix);
        v.stopScroll();
    }

    @SuppressWarnings("ConstantConditions")
    void setRecyclerViewScrollingState(Activity a, boolean isEnabled) {
        RecyclerView r = a.findViewById(R.id.fz4_mainActivity_recyclerview_fix);
        ((CustomLinearLayoutManager) r.getLayoutManager()).setScrollEnabled(isEnabled);
    }

    void bloodFilter(Activity a, int duration, boolean isVisible) {
        View v = a.findViewById(R.id.fz4_main_main_filter_black);
        v.setClickable(false);
        v.setFocusable(false);
        v.setFocusableInTouchMode(false);
        v.setBackgroundColor(Color.parseColor("#22FF0000"));
        Animation.setAnimation(
                v,
                isVisible ? Animation.Animations.ANIMATION_FADEIN : Animation.Animations.ANIMATION_FADEOUT,
                a,
                duration
        );
    }

    void bloodFilterPlus(Activity a, int duration) {
        View v = a.findViewById(R.id.fz4_main_main_filter_black);
        v.setClickable(false);
        v.setFocusable(false);
        v.setFocusableInTouchMode(false);
        v.setBackgroundColor(Color.parseColor("#44FF0000"));
        Animation.setAnimation(
                v,
                Animation.Animations.ANIMATION_FADEIN,
                a,
                duration
        );
    }

    void blackFilter(Activity a, int duration, boolean isVisible) {
        View v = a.findViewById(R.id.fz4_main_main_filter_black);
        v.setClickable(false);
        v.setFocusable(false);
        v.setFocusableInTouchMode(false);
        v.setBackgroundColor(Color.parseColor("#000000"));
        Animation.setAnimation(
                v,
                isVisible ? Animation.Animations.ANIMATION_FADEIN : Animation.Animations.ANIMATION_FADEOUT,
                a,
                duration
        );
    }

    void setLostImage(Activity a, RequestManager rm) {
        rm.load(R.drawable.fz4_friendzone).into((ImageView) a.findViewById(R.id.fz4_main_lost));
    }

    void friendzoned(Activity a) {
        a.findViewById(R.id.fz4_main_lost).setVisibility(View.VISIBLE);
    }
}
