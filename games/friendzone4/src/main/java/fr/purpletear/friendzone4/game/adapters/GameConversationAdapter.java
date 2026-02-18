package fr.purpletear.friendzone4.game.adapters;


import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.example.sutokosharedelements.tables.trophies.TrophyItemDecoration;

import java.util.ArrayList;

import fr.purpletear.friendzone4.R;
import fr.purpletear.friendzone4.custom.Character;
import fr.purpletear.friendzone4.factories.Data;
import fr.purpletear.friendzone4.game.activities.main.MainInterface;
import fr.purpletear.friendzone4.game.activities.main.Phrase;
import fr.purpletear.friendzone4.game.tables.TableOfCharacters;
import fr.purpletear.friendzone4.purpleTearTools.Finger;
import fr.purpletear.friendzone4.purpleTearTools.Std;

public class GameConversationAdapter extends RecyclerView.Adapter<GameConversationAdapter.ViewHolder> {
    /**
     * Contains the phrases
     *
     * @see Phrase
     */
    private ArrayList<Phrase> array = new ArrayList<>();

    /**
     * Represents the callback of the phrase
     *
     * @see fr.purpletear.friendzone4.game.activities.main.MainInterface
     */
    private MainInterface callBack;

    /**
     * Contains the list of every characters
     *
     * @see Character
     */
    private TableOfCharacters tableOfCharacters;

    /**
     * Contains a reference to the Activity's Context
     */
    private Context context;


    /**
     * Contains the chapter code
     */
    private String code;

    private RequestManager Glide;

    private boolean hasBackgroundMedia;

    private boolean isNoSeen;

    private boolean dateColorAlter = false;

    public GameConversationAdapter(Context context, RequestManager r, MainInterface callBack, TableOfCharacters tableOfCharacters, String code, boolean hasBackgroundMedia, boolean isNoSeen) {
        this.context = context;
        this.callBack = callBack;
        this.tableOfCharacters = tableOfCharacters;
        this.code = code;
        this.Glide = r;
        this.hasBackgroundMedia = hasBackgroundMedia;
        this.isNoSeen = isNoSeen;
    }

    public ArrayList<Phrase> getAll() {
        return array;
    }

    public void setArray(ArrayList<Phrase> array) {
        this.array = array;
    }

    public boolean isEmpty() {
        return  array.size() == 0;
    }

    @Override
    public int getItemCount() {
        return array.size();
    }

    public void clear() {
        int size = array.size();
        array.clear();
        notifyItemRangeRemoved(0, size);
    }

    /**
     * Inserts a phrase in the adapter without smoothscroll
     *
     * @param p    phrase
     * @param type type
     */
    public void insertPhrase(Phrase p, Phrase.Type type) {
        insertPhrase(p, type, false);
    }

    /**
     * Inserts a phrase in the adapter
     *
     * @param p    phrase
     * @param type type
     */
    public void insertPhrase(Phrase p, Phrase.Type type, boolean smooth) {
        if (p.needsSkip()) return;
        if(isNoSeen && type == Phrase.Type.info) {
            p.setType(Phrase.determineTypeCode(Phrase.Type.date));
        } else {
            p.setType(Phrase.determineTypeCode(type));
        }
        int position = array.size();
        array.add(p);
        notifyItemInserted(position);

        callBack.onInsertPhrase(position, smooth);
    }

    /**
     * Returns the last item of the array
     *
     * @return Phrase
     */
    public Phrase getLastItem() {
        int size = array.size();
        return array.get(size - 1);
    }

    /**
     * Sets the last message as meSeen
     */
    public void setLastSeen() {
        int position = array.size() - 1;
        Phrase last = array.get(position);
        last.setType(Phrase.Type.meSeen);
        array.set(array.size() - 1, last);
        notifyItemChanged(position);
    }

    /**
     * Removes the last phrase.
     */
    public void editLast(Phrase p, Phrase.Type type) {
        int position = array.size() - 1;
        p.setType(type);
        array.set(position, p);
        notifyItemChanged(position);
    }

    /**
     * Removes the last phrase.
     */
    public void editLastIfIs(Phrase.Type type, Phrase.Type toType) {
        int position = array.size() - 1;
        if (!lastIs(type)) {
            return;
        }
        Phrase p = getLastItem();
        p.setType(toType);
        array.set(position, p);
        notifyItemChanged(position);
    }

    public void setHasBackgroundMedia(boolean hasBackgroundMedia) {
        this.hasBackgroundMedia = hasBackgroundMedia;
    }

    /**
     * Removes the last phrase if needed
     */
    public void removeIfLastIs(Phrase.Type type) {
        int position = array.size() - 1;
        if (-1 == position) {
            return;
        }
        if (array.get(position).getType() != Phrase.determineTypeCode(type)) {
            return;
        }
        array.remove(position);
        notifyItemRemoved(position);
    }

    /**
     * Removes the last phrase if needed
     */
    public boolean lastIs(Phrase.Type type) {
        int position = array.size() - 1;
        if (-1 == position) {
            return false;
        }
        if (array.get(position).getType() == Phrase.determineTypeCode(type)) {
            return true;
        }
        return false;
    }

    public Phrase last() {
        int position = array.size() - 1;
        if (position < 0) {
            return null;
        }
        return array.get(array.size() - 1);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflated;
        switch (Phrase.determineTypeEnum(viewType)) {
            case me:
                inflated = LayoutInflater.from(parent.getContext()).inflate(R.layout.fz4_phrase_me, parent, false);
                break;
            case image:
                inflated = LayoutInflater.from(parent.getContext()).inflate(R.layout.fz4_phrase_image, parent, false);
                break;
            case meSeen:
                inflated = LayoutInflater.from(parent.getContext()).inflate(R.layout.fz4_phrase_me, parent, false);
                break;
            case typing:
                inflated = LayoutInflater.from(parent.getContext()).inflate(R.layout.fz4_phrase_typing, parent, false);
                break;
            case dest:
                if(isNoSeen) {
                    inflated = LayoutInflater.from(parent.getContext()).inflate(R.layout.fz4_phrase_dest_no_seen, parent, false);
                } else {
                    inflated = LayoutInflater.from(parent.getContext()).inflate(R.layout.fz4_phrase_dest, parent, false);
                }
                break;
            case info:
                inflated = LayoutInflater.from(parent.getContext()).inflate(R.layout.fz4_phrase_info, parent, false);
                break;
            case date:
                inflated = LayoutInflater.from(parent.getContext()).inflate(R.layout.fz4_phrase_date, parent, false);
                break;
            case nextChapter:
                inflated = LayoutInflater.from(parent.getContext()).inflate(R.layout.fz4_phrase_next_chapter, parent, false);
                break;
            case gif:
                inflated = LayoutInflater.from(parent.getContext()).inflate(R.layout.fz4_phrase_gif, parent, false);
                break;
            case paused:
                inflated = LayoutInflater.from(parent.getContext()).inflate(R.layout.fz4_phrase_game_paused, parent, false);
                break;
            case minigame:
                inflated = LayoutInflater.from(parent.getContext()).inflate(R.layout.fz4_phrase_mini_game, parent, false);
                break;

            case trophy:
                inflated = LayoutInflater.from(parent.getContext()).inflate(com.example.sutokosharedelements.R.layout.inc_trophy_unlocked, parent, false);
                break;
            default:
                throw new IllegalArgumentException("Unknown Phrase.Type in GameConversationAdapter.onCreateViewHolder " + "viewType(" + viewType + ") " + Phrase.determineTypeEnum(viewType).name());
        }
        return new ViewHolder(inflated);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Phrase p = array.get(position);
        holder.display(p);
    }

    @Override
    public int getItemViewType(int position) {
        if(position >= array.size()) {
            return Phrase.determineTypeCode(Phrase.Type.undetermined);
        }
        final Phrase p = array.get(position);
        return p.getType();
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder.image != null) {
            Glide.clear(holder.image);
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView text;
        private FrameLayout background;
        private ImageView image;

        ViewHolder(View itemView) {
            super(itemView);
        }

        void display(final Phrase p) {
            switch (Phrase.determineTypeEnum(p.getType())) {
                case info: {
                    text = itemView.findViewById(R.id.fz4_phrase_info_text);
                    text.setText(p.getSentence());
                    text.setTextColor(ContextCompat.getColor(context, R.color.phrase_info_text));
                    break;
                }

                case date: {
                    text = itemView.findViewById(R.id.fz4_phrase_date_text);
                    text.setText(p.getSentence());
                    if(dateColorAlter) {
                       text.setAlpha(.8f);
                    } else {
                        text.setAlpha(1);
                    }
                    dateColorAlter = !dateColorAlter;
                    text.setTextColor(ContextCompat.getColor(context, R.color.phrase_info_text));
                    break;
                }

                case me: {
                    text = itemView.findViewById(R.id.fz4_phrase_me_text);
                    background = itemView.findViewById(R.id.fz4_phrase_me_background);

                    itemView.findViewById(R.id.fz4_phrase_me_seen).setVisibility(View.GONE);
                    itemView.findViewById(R.id.fz4_phrase_me_seen_text).setVisibility(View.GONE);
                    text.setText(p.getSentence());

                    GradientDrawable gd = (GradientDrawable) background.getBackground();
                    gd.setColor(ContextCompat.getColor(context, isNoSeen ? R.color.noSeenMeBackground : R.color.meBackground));
                    text.setTextColor(ContextCompat.getColor(context, R.color.colorWhite));
                    break;
                }

                case typing: {
                    Character c = tableOfCharacters.getCharacter(p.getId_author());

                    background = itemView.findViewById(R.id.fz4_phrase_typing_background);
                    GradientDrawable gd = (GradientDrawable) background.getBackground();
                    gd.setColor(ContextCompat.getColor(context, c.getColorId()));
                    final View i = itemView.findViewById(R.id.fz4_phrase_typing_anim);

                    int d = c.getTypingAnim();

                    i.setBackgroundResource(d);
                    final AnimationDrawable a = (AnimationDrawable) i.getBackground();
                    a.setCallback(i);
                    a.setVisible(true, true);
                    a.start();

                    break;
                }

                case gif: {
                    try {
                        Character c = tableOfCharacters.getCharacter(p.getId_author());

                        final View i = itemView.findViewById(R.id.fz4_gif_phrase_gif_image);

                        int d = Std.drawableFromName(p.getGifName(), context);

                        i.setBackgroundResource(d);
                        final AnimationDrawable a = (AnimationDrawable) i.getBackground();
                        a.setCallback(i);
                        a.setVisible(true, true);
                        a.start();

                        Glide
                                .load(c.getSmallImageId())
                                .into(image = itemView.findViewById(R.id.fz4_gif_phrase_gif_profil));
                    } catch (Exception e) {
                        Std.debug(e.getMessage());
                    }
                    break;
                }

                case dest: {
                    Character pers = tableOfCharacters.getCharacter(p.getId_author());


                    if(isNoSeen){
                        text = itemView.findViewById(R.id.fz4_noseen_phrase_dest_text);
                        background = itemView.findViewById(R.id.fz4_noseen_phrase_dest_background);
                        image = itemView.findViewById(R.id.fz4_noseen_phrase_dest_profil);
                    } else {
                        text = itemView.findViewById(R.id.fz4_phrase_dest_text);
                        background = itemView.findViewById(R.id.fz4_phrase_dest_background);
                        image = itemView.findViewById(R.id.fz4_phrase_dest_profil);
                    }

                    text.setText(p.getSentence());

                    Glide
                            .load(Data.getImage(context, pers.getSmallImageId()))
                            .into(image);
                    text.setTextColor(ContextCompat.getColor(context, pers.getTextColorId()));
                    GradientDrawable gd = (GradientDrawable) background.getBackground();
                    gd.setColor(ContextCompat.getColor(context, pers.getColorId()));

                    break;
                }

                case meSeen: {
                    text = itemView.findViewById(R.id.fz4_phrase_me_text);
                    background = itemView.findViewById(R.id.fz4_phrase_me_background);


                    itemView.findViewById(R.id.fz4_phrase_me_seen).setVisibility(View.VISIBLE);
                    itemView.findViewById(R.id.fz4_phrase_me_seen_text).setVisibility(View.VISIBLE);
                    if (hasBackgroundMedia) {
                        ((TextView) itemView.findViewById(R.id.fz4_phrase_me_seen_text)).setTextColor(Color.parseColor("#88FFFFFF"));
                    } else {
                        ((TextView) itemView.findViewById(R.id.fz4_phrase_me_seen_text)).setTextColor(Color.parseColor("#88000000"));
                    }
                    TableOfCharacters characters = new TableOfCharacters();
                    characters.load(code);
                    Glide
                            .load(Data.getImage(context, characters.getSeen()))
                            .into(image = itemView.findViewById(R.id.fz4_phrase_me_seen));
                    GradientDrawable gd = (GradientDrawable) background.getBackground();
                    text.setText(p.getSentence());
                    gd.setColor(ContextCompat.getColor(context, R.color.meBackground));
                    text.setTextColor(ContextCompat.getColor(context, R.color.colorWhite));
                    break;
                }

                case image: {
                    Character pers = tableOfCharacters.getCharacter(p.getId_author());
                    image = itemView.findViewById(R.id.fz4_phrase_image_image);
                    ImageView profil = itemView.findViewById(R.id.fz4_phrase_image_profil);

                    Glide
                            .load(Data.getImage(context, pers.getSmallImageId()))
                            .into(profil);

                    Glide.load(Data.getImage(context, p.getContentImageName())).into(image);

                    Finger.defineOnTouch(
                            image,
                            context,
                            new Runnable() {
                                @Override
                                public void run() {
                                    callBack.onClickItem("image", p.getContentImageName());
                                }
                            }
                    );
                    break;
                }

                case nextChapter: {
                    TextView text = itemView.findViewById(R.id.fz4_phrase_next_chapter_text);
                    text.setTextColor(ContextCompat.getColor(context, R.color.phrase_info_text));
                    break;
                }

                case paused: {
                    TextView text = itemView.findViewById(R.id.fz4_phrase_game_paused_text);
                    if (hasBackgroundMedia) {
                        text.setTextColor(ContextCompat.getColor(context, R.color.info_with_background_media));
                    } else {
                        text.setTextColor(ContextCompat.getColor(context, R.color.phrase_info_text));
                    }
                    break;
                }

                case minigame: {
                    ImageView joystick = itemView.findViewById(R.id.fz4_phrase_minigame_button_joystick);
                    joystick.setX(0);
                    Glide.load(Data.getImage(context, "fz4_joystick")).into(image = joystick);
                    ((TextView) itemView.findViewById(R.id.fz4_phrase_minigame_text)).setText(p.getSentence().replace("9c3", "").replace("9c4", ""));
                    String code = "";
                    if(p.getSentence().contains("9c3")) {
                        code = "9c3";
                    } else if(p.getSentence().contains("9c4")) {
                        code = "9c4";
                    }
                    minigameListener(itemView, joystick, System.currentTimeMillis(), code);
                    break;
                }

                case trophy:
                   TrophyItemDecoration.Companion.design(itemView, Glide);
                    return;

                default:
                    throw new IllegalArgumentException("Unknown Phrase.Type in GameConversationAdapter.ViewHolder.display");
            }
        }

        private void minigameListener(View line, final View joystick, final long ms, final String code) {
            line.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(!Finger.isFingerIn(event, v)) {
                        callBack.onReleaseJoystick();
                        return false;
                    }
                    if(event.getAction() == MotionEvent.ACTION_DOWN) {
                        callBack.onTouchJoystick();
                    }
                    if(event.getAction() == MotionEvent.ACTION_UP) {
                        callBack.onReleaseJoystick();
                        return false;
                    }
                    if(event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                        callBack.onReleaseJoystick();
                        return false;
                    }
                    if(event.getAction() == MotionEvent.ACTION_CANCEL) {
                        callBack.onReleaseJoystick();
                        return false;
                    }

                    if(event.getAction() == MotionEvent.ACTION_MOVE) {
                        joystick.setX(getJoystickX(event.getRawX(), joystick.getWidth(), ms, code));
                    }
                    return true;
                }
            });
        }

        private boolean left = true;
        private boolean right = false;

        /**
         * Returns the joystick pos
         * @param pos
         * @param width
         * @return
         */
        private float getJoystickX(float pos, int width, long ms, String code) {
            float res = pos - width / 2;
            if(res < 0) {
                return 0;
            } else if(res + width > itemView.getWidth()) {
                return itemView.getWidth() - width;
            }
            handleHit( pos, width, ms, code);
            return res;
        }

        private void handleHit( float pos, int width, long ms, String code) {
            if(left && percentJoystickPosition(pos, width) >= 70) {
                left = false;
                right = true;
                callBack.onJoystickInfoHit(ms, code);
            } else if(right && percentJoystickPosition(pos, width) <= 30) {
                left = true;
                right = false;
                callBack.onJoystickInfoHit(ms, code);
            }
        }

        private float percentJoystickPosition(float pos, int width) {
            float x = pos - width / 2;

            return x * 100 / itemView.getWidth();
        }

    }
}
