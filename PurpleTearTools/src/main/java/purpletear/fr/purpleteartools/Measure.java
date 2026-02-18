/*
 * Copyright (C) PurpleTear, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Hocine Belbouab <hbdeveloppeur@gmail.com>, 1/10/19 11:38 AM
 *
 */

package purpletear.fr.purpleteartools;

import android.content.Context;
import android.graphics.Point;
import android.util.TypedValue;
import android.view.Display;

public class Measure {

    public enum Type {
        WIDTH,
        HEIGHT
    }

    /**
     * Return px given the dp parameter
     */
    public static int px(int dp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    /**
     * Determines the size in pixel given the percent size of the screen
     * @return the size in pixel
     */
    public static float percent(Type type, float percent, Display display) {
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        if(type == Type.HEIGHT) {
            return percent * height / 100;
        } else {
            return percent * width / 100;
        }
    }
}
