package friendzone3.purpletear.fr.friendzon3.handlers;
import android.content.Context;
import androidx.core.content.ContextCompat;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import friendzone3.purpletear.fr.friendzon3.MainActivity;
import friendzone3.purpletear.fr.friendzon3.R;
import friendzone3.purpletear.fr.friendzon3.custom.Character;
import friendzone3.purpletear.fr.friendzon3.custom.Phrase;
import friendzone3.purpletear.fr.friendzon3.custom.PhraseCallBack;
import purpletear.fr.purpleteartools.Finger;
import purpletear.fr.purpleteartools.GlobalData;
import purpletear.fr.purpleteartools.Measure;
import purpletear.fr.purpleteartools.TableOfSymbols;

public class ChoicesController {
    static PhraseCallBack callBack;

    public static void choose(Context context, LinearLayout parent, ArrayList<Phrase> phrases, PhraseCallBack c, TableOfSymbols table, MainActivity.Support support) {
        callBack = c;
        for (Phrase phrase : phrases) {
            if(phrase != null) {
                if(skip(phrase, table)) {
                    continue;
                } else {
                    if(phrase.isChoiceCondition()) {
                        phrase.setSentence(phrase.getChoiceConditionFormated());
                    }
                }
                phrase.setSentence(Character.Companion.updateNames(context, phrase.getSentence()));
                phrase.controlEmojis(support);
                parent.addView(buildChoice(phrase, parent.getContext()));
            }

        }
    }

    private static View buildChoice(final Phrase phrase, Context context) {
        int padding_top = Math.round(Measure.px(9, context));
        int padding_left = Math.round(Measure.px(10, context));
        TextView textView = new TextView(context);
        textView.setText(phrase.getSentence());
        textView.setTextColor(ContextCompat.getColor(context, R.color.mainactivity_choices_text));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.choice_text_font));
        textView.setPadding(padding_left, padding_top, padding_left, padding_top);
        Finger.Companion.defineOnTouch(textView, new Runnable() {
            @Override
            public void run() {
                callBack.onClickChoice(phrase);
            }
        });
        return textView;
    }


    public static void clear(LinearLayout parent) {
        parent.removeAllViews();
    }

    private static boolean skip(Phrase p, TableOfSymbols table) {
        if(!p.isChoiceCondition()){
            return false;
        }
        String[] values = p.getChoiceCondition()
                .replace("[", "")
                .replace("]", "")
                .split("==");
        return !table.condition(GlobalData.Game.FRIENDZONE3.getId(), values[0], values[1]);
    }
}
