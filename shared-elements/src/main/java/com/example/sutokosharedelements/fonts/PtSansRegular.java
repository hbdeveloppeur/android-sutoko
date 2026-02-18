/*
 * Copyright (C) PurpleTear, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Hocine Belbouab <hbdeveloppeur@gmail.com>, 1/10/19 11:38 AM
 *
 */

package com.example.sharedelements.fonts;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.annotation.Keep;
import androidx.appcompat.widget.AppCompatTextView;

@Keep
public class PtSansRegular extends AppCompatTextView {

    public PtSansRegular(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public PtSansRegular(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PtSansRegular(Context context) {
        super(context);
        init();
    }

    private void init() {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(),
                "fonts/PtSansRegular.ttf");
        setTypeface(tf);
        setIncludeFontPadding(false);
    }

}