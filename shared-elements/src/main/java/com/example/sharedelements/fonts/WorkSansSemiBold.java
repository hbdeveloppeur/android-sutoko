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
public class WorkSansSemiBold extends AppCompatTextView {

    public WorkSansSemiBold(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public WorkSansSemiBold(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WorkSansSemiBold(Context context) {
        super(context);
        init();
    }

    private void init() {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(),
                "fonts/font_worksans_semibold.otf");
        setTypeface(tf);
        setIncludeFontPadding(false);
    }

}