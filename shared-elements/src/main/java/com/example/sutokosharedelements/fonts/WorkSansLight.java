/*
 * Copyright (C) PurpleTear, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Hocine Belbouab <hbdeveloppeur@gmail.com>, 1/10/19 11:38 AM
 *
 */

package com.example.sutokosharedelements.fonts;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.annotation.Keep;
import androidx.appcompat.widget.AppCompatTextView;

@Keep
public class WorkSansLight extends AppCompatTextView {

    public WorkSansLight(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public WorkSansLight(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WorkSansLight(Context context) {
        super(context);
        init();
    }

    private void init() {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(),
                "fonts/WorkSans-Light.otf");
        setTypeface(tf);
        setIncludeFontPadding(false);
    }

}