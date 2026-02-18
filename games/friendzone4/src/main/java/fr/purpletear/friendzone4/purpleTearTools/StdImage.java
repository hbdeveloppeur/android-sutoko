package fr.purpletear.friendzone4.purpleTearTools;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.net.Uri;


import com.bumptech.glide.Glide;

import java.io.IOException;
import java.io.InputStream;

import static com.bumptech.glide.load.resource.bitmap.TransformationUtils.getExifOrientationDegrees;
import static com.bumptech.glide.load.resource.bitmap.TransformationUtils.rotateImage;

public class StdImage {

    public static Bitmap resizeBitmap(Bitmap bitmap, int bound) {
        return resizeBitmap(bitmap, bound, true);
    }

    public enum ResizeType {
        FORCE_WIDTH,
        FORCE_HEIGHT
    }

    /**
     * Resizes an image by its axis
     *
     * @param bitmap Bitmap
     * @param px     int
     * @param type   ResizeType
     * @return Bitmap
     */
    public static Bitmap resizeForce(Bitmap bitmap, int px, ResizeType type) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        switch (type) {
            case FORCE_WIDTH: {
                if (width <= px) {
                    return bitmap;
                }

                float ratio = (float) width / height;
                Bitmap res = Bitmap.createScaledBitmap(
                        bitmap, px, (int) (px / ratio), false);
                bitmap.recycle();
                return res;
            }

            case FORCE_HEIGHT: {
                if (height <= px) {
                    return bitmap;
                }

                float ratio = (float) height / width;
                Bitmap res = Bitmap.createScaledBitmap(
                        bitmap, (int) (px / ratio), px, false);
                bitmap.recycle();
                return res;
            }

            default:
                throw new IllegalArgumentException();
        }
    }

    /**
     * Resizes a Bitmap to the limit bound
     *
     * @param bitmap Bitmap
     * @param bound  int
     * @return Bitmap
     */
    @SuppressWarnings("SameParameterValue")
    public static Bitmap resizeBitmap(Bitmap bitmap, int bound, boolean recycle) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (bound >= width && bound >= height) {
            return bitmap;
        }

        boolean isLandscape = width > height;
        if (isLandscape) {
            float ratio = (float) width / height;
            Bitmap res = Bitmap.createScaledBitmap(
                    bitmap, bound, (int) (bound / ratio), false);
            if (recycle) bitmap.recycle();
            return res;

        } else {
            float ratio = (float) height / width;
            Bitmap res = Bitmap.createScaledBitmap(
                    bitmap, (int) (bound / ratio), bound, false);
            if (recycle) bitmap.recycle();
            return res;
        }
    }
}
