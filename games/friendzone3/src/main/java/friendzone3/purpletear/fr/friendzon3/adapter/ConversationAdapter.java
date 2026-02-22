package friendzone3.purpletear.fr.friendzon3.adapter;


import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.example.sutokosharedelements.OnlineAssetsManager;
import java.util.ArrayList;

import friendzone3.purpletear.fr.friendzon3.Data;
import friendzone3.purpletear.fr.friendzon3.MainActivity;
import friendzone3.purpletear.fr.friendzon3.R;
import friendzone3.purpletear.fr.friendzon3.custom.Personnage;
import friendzone3.purpletear.fr.friendzon3.custom.PersonnageStyle;
import friendzone3.purpletear.fr.friendzon3.custom.Phrase;
import friendzone3.purpletear.fr.friendzon3.custom.PhraseCallBack;
import purpletear.fr.purpleteartools.Finger;
import purpletear.fr.purpleteartools.GlobalData;
import purpletear.fr.purpleteartools.TableOfSymbols;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ViewHolder> {
    public ArrayList<Phrase> array = new ArrayList<>();
    private Context context;
    private PhraseCallBack callBack;
    private MainActivity.Support support;
    private String chapterCode;
    private TableOfSymbols symbols;
    private RequestManager requestManager;

    public ConversationAdapter(Context context, PhraseCallBack callBack, MainActivity.Support support, String chapterCode, TableOfSymbols symbols, RequestManager requestManager) {
        this.support = support;
        this.context = context;
        this.callBack = callBack;
        this.chapterCode = chapterCode;
        this.symbols = symbols;
        this.requestManager = requestManager;
    }

    @Override
    public int getItemCount() {
        return array.size();
    }


    public void  setArray(ArrayList<Phrase> array) {
        this.array = array;
    }
    /**
     * Clear the whole adapter.
     */
    public void clear() {
        array.clear();
        notifyDataSetChanged();
    }

    /**
     * Inserts a phrase in the adapter
     * @param p phrase
     * @param type type
     */
    public void insertPhrase(Phrase p, Phrase.Type type) {
        if(p.needsSkip()) return;
        p.setType(type);
        int position = array.size();
        array.add(p);
        notifyItemInserted(position);
        callBack.onInsertPhrase();
    }

    /**
     * Removes the last phrase.
     */
    public void editLast(Phrase p, Phrase.Type type) {
        int position = array.size() - 1;
        if(position == -1) {
            return;
        }
        p.setType(type);
        array.set(position, p);
        notifyItemChanged(position);
    }

    public boolean isLast(int number) {
        return number == array.size() - 1;
    }

    /**
     * Removes the last phrase if needed
     *
     */
    public void removeIfLastIs(Phrase.Type type) {
        int position = array.size() - 1;
        if(-1 == position) {
            return;
        }
        if(array.get(position).getType() != type) {
            return;
        }
        array.remove(position);
        notifyItemRemoved(position);
    }

    public Phrase last() {
        int position = array.size() - 1;
        if(position < 0) {
            return null;
        }
        return array.get(array.size() - 1);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflated;
        switch(Phrase.Type.values()[viewType]) {
            case me :
                inflated = LayoutInflater.from(parent.getContext()).inflate(R.layout.fz3_phrase_me, parent, false);

                break;
            case image:
                inflated = LayoutInflater.from(parent.getContext()).inflate(R.layout.fz3_phrase_image, parent, false);
                break;
            case meImage:
                inflated = LayoutInflater.from(parent.getContext()).inflate(R.layout.fz3_phrase_me_image, parent, false);
                break;
            case meSeen:
                inflated = LayoutInflater.from(parent.getContext()).inflate(R.layout.fz3_phrase_seen, parent, false);
                break;
            case typing:
                inflated = LayoutInflater.from(parent.getContext()).inflate(R.layout.fz3_phrase_typing, parent, false);
                break;
            case dest:
                inflated = LayoutInflater.from(parent.getContext()).inflate(R.layout.fz3_phrase_dest, parent, false);
                break;
            case info:
                inflated = LayoutInflater.from(parent.getContext()).inflate(R.layout.fz3_phrase_info, parent, false);
                break;
            case date:
                inflated = LayoutInflater.from(parent.getContext()).inflate(R.layout.fz3_phrase_date, parent, false);
                break;
            case vocal:
                inflated = LayoutInflater.from(parent.getContext()).inflate(R.layout.fz3_phrase_vocal, parent, false);
                break;
            case nextChapter:
                inflated = LayoutInflater.from(parent.getContext()).inflate(R.layout.fz3_phrase_end, parent, false);
                break;
            case hidden:
                inflated = LayoutInflater.from(parent.getContext()).inflate(R.layout.fz3_phrase_hidden, parent, false);
                break;
            case unHiding:
                inflated = LayoutInflater.from(parent.getContext()).inflate(R.layout.fz3_phrase_hidden, parent, false);
                break;
            case robot:
                inflated = LayoutInflater.from(parent.getContext()).inflate(R.layout.fz3_phrase_robot, parent, false);
                break;
            case robotCode:
                inflated = LayoutInflater.from(parent.getContext()).inflate(R.layout.fz3_phrase_robot_code, parent, false);
                break;
            case alert:
                inflated = LayoutInflater.from(parent.getContext()).inflate(R.layout.fz3_phrase_warning, parent, false);
                break;
            case trophy:
                inflated = LayoutInflater.from(parent.getContext()).inflate(com.example.sharedelements.R.layout.inc_trophy_unlocked, parent, false);
                break;
            default:
                throw new IllegalArgumentException("Unknown Phrase.Type in ConversationAdapter.onCreateViewHolder");
        }
        return new ViewHolder(inflated);
    }

    private void animate(final TextView t, final String str, final String tmp) {
        t.setText(tmp);
        callBack.onInsertPhrase();
        if(str.length() == tmp.length()) {
            return;
        }


        Handler handler = new Handler();
        Runnable runnable;
        handler.postDelayed(runnable = new Runnable() {
            @Override
            public void run() {
                int size = tmp.length();
                animate(t, str, str.substring(0, size + 1));
            }
        }, getDelayAnimate());
        MainActivity.mh.push(handler, runnable);
    }

    public long getDelayAnimate() {
        return 50;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Phrase p = array.get(position);
        holder.display(p);
    }

    @Override
    public int getItemViewType(int position) {
        final Phrase p = array.get(position);
        return p.getType().ordinal();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView text;
        private FrameLayout background;
        private ImageView image;

        ViewHolder(View itemView) {
            super(itemView);
        }

        void display(final Phrase p){
            switch(p.getType()) {
                case info: {
                    text = itemView.findViewById(R.id.phrase_info_text);
                    text.setText(p.getSentence());
                    if(MainActivity.hasBackgroundMedia) {
                        text.setTextColor(ContextCompat.getColor(context,  R.color.soft_white));
                    } else {
                        text.setTextColor(ContextCompat.getColor(context,  R.color.phrase_info_text));
                    }
                    break;
                }

                case date: {
                    text = itemView.findViewById(R.id.phrase_date_text);
                    text.setText(p.getSentence());
                    break;
                }

                case me : {
                    text = itemView.findViewById(R.id.phrase_me_text);
                    background = itemView.findViewById(R.id.phrase_me_background);

                    GradientDrawable gd = (GradientDrawable) background.getBackground();

                    text.setText(p.getSentence());
                    if(support == MainActivity.Support.NORMAL) {
                        gd.setColor(ContextCompat.getColor(context,  R.color.meBackground));
                        text.setTextColor(ContextCompat.getColor(context, R.color.white));
                    } else {
                        gd.setColor(ContextCompat.getColor(context,  R.color.white));
                        text.setTextColor(ContextCompat.getColor(context, R.color.black));
                    }

                    break;
                }

                case typing : {
                    Personnage pers = Personnage.who(chapterCode, p.getId_author(), symbols);
                    background = itemView.findViewById(R.id.phrase_typing_background);
                    GradientDrawable gd = (GradientDrawable) background.getBackground();
                    gd.setColor(ContextCompat.getColor(context,  pers.getStyle().getIdBackgroundColor()));
                    final ImageView i = itemView.findViewById(R.id.phrase_typing_anim);

                    int d = R.drawable.friendzone3_anim_istyping_gray;
                    if(pers.getStyleColor() ==  PersonnageStyle.StyleColor.second){
                        d = R.drawable.friendzone3_anim_istyping_whitefix;
                    }
                    i.setBackgroundResource(d);
                    final AnimationDrawable a = (AnimationDrawable) i.getBackground();
                    a.setCallback(i);
                    a.setVisible(true, true);
                    a.start();

                    break;
                }

                case dest: {
                    Personnage pers = Personnage.who(chapterCode, p.getId_author(), symbols);


                    text = itemView.findViewById(R.id.phrase_dest_text);
                    image = itemView.findViewById(R.id.phrase_dest_profil);
                    background = itemView.findViewById(R.id.phrase_dest_background);

                    GradientDrawable gd = (GradientDrawable) background.getBackground();
                    gd.setColor(ContextCompat.getColor(context, pers.getStyle().getIdBackgroundColor()));
                    text.setText(p.getSentence());
                    text.setTextColor(ContextCompat.getColor(context, pers.getStyle().getIdTextColor()));
                    requestManager.load(pers.getTickImage(context)).into(image);
                    break;
                }

                case hidden: {
                    Personnage pers = Personnage.who(chapterCode, p.getId_author(), symbols);


                    image = itemView.findViewById(R.id.phrase_hidden_profil);
                    requestManager.load(pers.getTickImage(context)).into(image);
                    break;
                }

                case unHiding: {
                    Personnage pers = Personnage.who(chapterCode, p.getId_author(), symbols);
                    image = itemView.findViewById(R.id.phrase_hidden_profil);
                    ProgressBar pb = itemView.findViewById(R.id.phrase_hidden_progressbar);
                    pb.getIndeterminateDrawable()
                            .setColorFilter(Color.GRAY, android.graphics.PorterDuff.Mode.SRC_IN);

                    pb.setVisibility(View.VISIBLE);
                    requestManager.load(pers.getTickImage(context)).into(image);
                    break;
                }

                case robot: {
                    text = itemView.findViewById(R.id.phrase_robot_text);
                    if(isLast(getAdapterPosition())) {
                        animate(text, p.getSentence(), "");
                    } else {
                        text.setText(p.getSentence());
                    }

                    break;
                }

                case robotCode: {
                    text = itemView.findViewById(R.id.phrase_robotCode_text);
                    text.setText(p.getSentence());
                    if(p.getSentence().replace(" ", "").equals("Success")) {
                        text.setTextColor(ContextCompat.getColor(context, R.color.success));
                    } else {
                        text.setTextColor(ContextCompat.getColor(context, R.color.robotCode));
                    }

                    break;
                }

                case vocal: {
                    String sname = p.getSentence()
                            .replace(" ", "")
                            .replace(".mp3", "")
                            .replace("[", "")
                            .replace("]", "");
                    final String soundName = Data.INSTANCE.selectSound(context, sname);
                    Personnage pers = Personnage.who(chapterCode, p.getId_author(), symbols);

                    if(MainActivity.sp.getCurrentSoundName().equals(soundName)) {
                        requestManager.load(
                                OnlineAssetsManager.INSTANCE.getImageFilePath(context, String.valueOf(GlobalData.Game.FRIENDZONE3.getId()), "friendzone3_vocal_sound_pause")
                        ).into((ImageView) itemView.findViewById(R.id.phrase_vocal_button));
                    } else {
                        requestManager.load(
                                OnlineAssetsManager.INSTANCE.getImageFilePath(context, String.valueOf(GlobalData.Game.FRIENDZONE3.getId()), "friendzone3_vocal_sound_play")
                        ).into((ImageView) itemView.findViewById(R.id.phrase_vocal_button));
                    }


                    if(MainActivity.sp.getCurrentSoundName().equals(soundName) && !MainActivity.sp.isPlaying()) {
                        requestManager.load(
                                OnlineAssetsManager.INSTANCE.getImageFilePath(context, String.valueOf(GlobalData.Game.FRIENDZONE3.getId()), "friendzone3_vocal_sound_play")
                        ).into((ImageView) itemView.findViewById(R.id.phrase_vocal_button));
                    } else if(MainActivity.sp.getCurrentSoundName().equals(soundName) && MainActivity.sp.isPlaying()) {
                        requestManager.load(
                                OnlineAssetsManager.INSTANCE.getImageFilePath(context, String.valueOf(GlobalData.Game.FRIENDZONE3.getId()), "friendzone3_vocal_sound_pause")
                        ).into((ImageView) itemView.findViewById(R.id.phrase_vocal_button));
                    } else if(MainActivity.sp.isPlaying()) {
                        // pause
                        requestManager.load(
                                OnlineAssetsManager.INSTANCE.getImageFilePath(context, String.valueOf(GlobalData.Game.FRIENDZONE3.getId()), "friendzone3_vocal_sound_play")
                        ).into((ImageView) itemView.findViewById(R.id.phrase_vocal_button));
                    } else {
                        requestManager.load(
                                OnlineAssetsManager.INSTANCE.getImageFilePath(context, String.valueOf(GlobalData.Game.FRIENDZONE3.getId()), "friendzone3_vocal_sound_play")
                        ).into((ImageView) itemView.findViewById(R.id.phrase_vocal_button));
                    }

                    image = itemView.findViewById(R.id.phrase_vocal_profil);
                    requestManager.load(pers.getTickImage(context)).into(image);
                    Finger.Companion.defineOnTouch(itemView.findViewById(R.id.phrase_vocal_background), new Runnable() {
                        @Override
                        public void run() {
                            notifyDataSetChanged();
                            callBack.onClickSound(soundName);
                        }
                    });
                    break;
                }

                case meSeen: {
                    text = itemView.findViewById(R.id.phrase_seen_text);
                    background = itemView.findViewById(R.id.phrase_seen_background);

                    GradientDrawable gd = (GradientDrawable) background.getBackground();
                    gd.setColor(ContextCompat.getColor(context, R.color.meBackground));
                    text.setText(p.getSentence());
                    text.setTextColor(ContextCompat.getColor(context, R.color.white));
                    final ImageView i = itemView.findViewById(R.id.phrase_seen_anim);
                    int d = R.drawable.friendzone3_anim_seen;
                    i.setBackgroundResource(d);
                    final AnimationDrawable a = (AnimationDrawable) i.getBackground();
                    a.setCallback(i);
                    a.setVisible(true, true);
                    a.start();
                    break;
                }

                case image: {
                    Personnage pers = Personnage.who(chapterCode, p.getId_author(), symbols);
                    image = itemView.findViewById(R.id.phrase_image_image);
                    ImageView profil = itemView.findViewById(R.id.phrase_image_profil);
                    requestManager.load(pers.getTickImage(context)).into(profil);

                    requestManager.load(
                            OnlineAssetsManager.INSTANCE.getImageFilePath(context, String.valueOf(GlobalData.Game.FRIENDZONE3.getId()),"friendzone3_" + p.getSentence())
                    ).into(image);
                    break;
                }

                case nextChapter: {
                    break;
                }

                case trophy: {
                    break;
                }

                case alert : {
                    text = itemView.findViewById(R.id.phrase_warning_text);
                    text.setText(p.getAlert());

                    break;
                }
                default:
                    throw new IllegalArgumentException("Unknown Phrase.Type in ConversationAdapter.ViewHolder.display");
            }
        }
    }
}
