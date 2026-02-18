package fr.purpletear.friendzone4.game.activities.main;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;

import fr.purpletear.friendzone4.GameData;
import fr.purpletear.friendzone4.R;
import fr.purpletear.friendzone4.purpleTearTools.Finger;
import fr.purpletear.friendzone4.purpleTearTools.Measure;
import purpletear.fr.purpleteartools.GlobalData;
import purpletear.fr.purpleteartools.TableOfSymbols;


public class ChoicesController {
    private static MainInterface callBack;

    public static void choose(Context context, ViewGroup parent, ArrayList<Phrase> phrases, MainInterface c, TableOfSymbols table, String username) {
        callBack = c;
        for (Phrase phrase : phrases) {
            if(phrase != null) {
                phrase.formatSentence(username, context);
                if(skip(phrase, table)) {
                    continue;
                } else {
                    if(phrase.isChoiceEqualCondition()) {
                        phrase.setSentence(context, phrase.getChoiceEqualConditionFormated());
                    } else if(phrase.isChoiceNotEqualCondition()) {
                        phrase.setSentence(context, phrase.getChoiceNotEqualConditionFormated());
                    }
                }
                parent.addView(buildChoice(phrase, parent.getContext()));
            }
        }
    }

    private static View buildChoice(final Phrase phrase, Context context) {
        int padding_top = Math.round(Measure.px(12, context));
        int padding_left = Math.round(Measure.px(10, context));
        TextView textView = new TextView(context);
        textView.setText(GameData.INSTANCE.updateNames(context, Emojis.translate(phrase.getSentence())));
        textView.setTextColor(ContextCompat.getColor(context, R.color.mainactivity_choices_text));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.choice_text_font));
        textView.setPadding(padding_left, padding_top, padding_left, padding_top);
        Finger.defineOnTouch(textView, context, new Runnable() {
            @Override
            public void run() {
                callBack.onClickChoice(phrase);
            }
        });
        return textView;
    }

    public static void countAnimation(View counterTextView, int seconds, float fromsp, float tosp) {
        final TextView v = (TextView) counterTextView;
        v.setText(String.valueOf(seconds));
        ValueAnimator animator = ValueAnimator.ofFloat(
                fromsp, tosp
        );
        animator.setDuration(1000);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                v.setTextSize((float) valueAnimator.getAnimatedValue());
            }
        });
        animator.start();
    }

    public static ValueAnimator progressBarAnimation(final View bar, int duration) {
        ValueAnimator animator = ValueAnimator.ofInt(
                bar.getWidth(), 1
        );

        animator.setDuration(duration);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                bar.getLayoutParams().width = (int) valueAnimator.getAnimatedValue();
            }
        });
        animator.start();
        return animator;
    }


    public static void clear(ViewGroup parent) {
        parent.removeAllViews();
    }

    private static boolean skip(Phrase p, TableOfSymbols table) {

        if(!p.isChoiceEqualCondition() && !p.isChoiceNotEqualCondition()){
            return false;
        }
        if(p.isChoiceEqualCondition()) {
            String[] values = {"" , ""};
            if(p.code != null && p.code.contains("==")) {
                values = p.code.replace(" ", "")
                        .replace("[", "")
                        .replace("]", "")
                        .split("==");
            }
            if(p.getSentence().contains("==")) {
                values = p.getSentence().replace(" ", "")
                        .replace("[", "")
                        .replace("]", "")
                        .split("==");
            }
            return !table.condition(GlobalData.Game.FRIENDZONE4.getId(), values[0], values[1]);
        } else if (p.isChoiceNotEqualCondition()) {
            String[] values = p.code.replace(" ", "")
                    .replace("[", "")
                    .replace("]", "")
                    .split("!=");
            if(p.getSentence().contains("!=")) {
                values = p.getSentence().replace(" ", "")
                        .replace("[", "")
                        .replace("]", "")
                        .split("!=");
            }
            return table.condition(GlobalData.Game.FRIENDZONE4.getId(), values[0], values[1]);
        }
        return false;
    }
}
