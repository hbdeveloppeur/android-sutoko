package fr.purpletear.friendzone4.game.activities.loading;

import android.app.Activity;
import android.os.Handler;
import android.view.View;

import fr.purpletear.friendzone4.R;
import fr.purpletear.friendzone4.factories.Std;
import fr.purpletear.friendzone4.purpleTearTools.Animation;

class LoadingGraphics {

    void startLoadAnim(Activity a) {
        View v = a.findViewById(R.id.fz4_loading_image);
        Animation.setAnimation(
                v,
                Animation.Animations.ANIMATION_FADEIN,
                a
        );
        Std.animateBackground(
                a,
                v,
                "fz4_anim_loading"
        );
    }

    void fadeOutLogo(Activity a, Runnable onComplete) {
        long duration = Animation.setAnimation(

                a.findViewById(R.id.fz4_loading_image),
                Animation.Animations.ANIMATION_FADEOUT,
                a
        );

        new Handler().postDelayed(onComplete, duration);
    }
}
