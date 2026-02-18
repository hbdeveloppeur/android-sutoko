/*
 * Copyright (C) PurpleTear, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Hocine Belbouab <hbdeveloppeur@gmail.com>, 1/10/19 11:38 AM
 *
 */

package purpletear.fr.purpleteartools.fonts;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

public class OldPtSans extends androidx.appcompat.widget.AppCompatTextView {

    public OldPtSans(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public OldPtSans(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public OldPtSans(Context context) {
        super(context);
        init();
    }

    private void init() {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(),
                "font/pt_sanswebregular.ttf");
        setTypeface(tf);
    }

}