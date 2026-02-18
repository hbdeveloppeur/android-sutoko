package fr.purpletear.friendzone4.game.activities.hacking;

import android.app.Activity;
import android.content.Context;
import android.view.Display;
import android.widget.ImageView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;

import fr.purpletear.friendzone4.R;
import fr.purpletear.friendzone4.factories.Data;
import fr.purpletear.friendzone4.purpleTearTools.Animation;
import fr.purpletear.friendzone4.purpleTearTools.GridSpacingItemDecoration;
import fr.purpletear.friendzone4.purpleTearTools.Measure;

class HackingGameGraphics {

    void setImages(Activity a, RequestManager Glide, boolean isEnd) {
        Glide.load(R.drawable.fz4_ic_fprint).into((ImageView) a.findViewById(R.id.fz4_hacking_game_button));
        if(!isEnd) {
            Glide.load(Data.getImage(a, "fz4_basement")).into((ImageView) a.findViewById(R.id.fz4_hacking_game_background));
        } else {
            Glide
                    .load(R.drawable.fz4_fixed_logo)
                    .into((ImageView) a.findViewById(R.id.fz4_hacking_game_logo));
        }
    }

    /**
     * Loads the recyclerView
     *
     * @param context      Context
     * @param recyclerView RecyclerView
     * @param adapter      Adapter
     * @param display      Display
     */
    void setRecyclerView(Context context, RecyclerView recyclerView, Adapter adapter, Display display) {
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        LinearLayoutManager lLayout = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        recyclerView.addItemDecoration(
                new GridSpacingItemDecoration(
                        Math.round(Measure.percent(Measure.Type.HEIGHT,
                                .5f,
                                display
                        )), false));
        recyclerView.setLayoutManager(lLayout);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setItemAnimator(null);
    }

    /**
     * Fades in the logo and FadesOut the recyclerView
     * @param a
     */
    void change(Activity a) {
        Animation.setAnimation(
                a.findViewById(R.id.fz4_hacking_game_logo),
                Animation.Animations.ANIMATION_FADEIN,
                a,
                4000
        );

        Animation.setAnimation(
                a.findViewById(R.id.fz4_hacking_game_recyclerview),
                Animation.Animations.ANIMATION_FADEOUT,
                a,
                3800
        );

        Animation.setAnimation(
                a.findViewById(R.id.fz4_hacking_game_button),
                Animation.Animations.ANIMATION_FADEOUT,
                a,
                1000
        );
    }

    int end(Activity a) {
        return (int) Animation.setAnimation(
                a.findViewById(R.id.fz4_hacking_game_logo),
                Animation.Animations.ANIMATION_FADEOUT,
                a,
                2000
        );
    }
}
