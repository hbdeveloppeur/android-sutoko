package fr.purpletear.friendzone4.custom;

import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;


public class PageIndicator {
    private final View container;
    private final View bar;
    private SlidePosition currentPosition;
    private final long delay;
    private final ArrayList<View> icons;


    public enum SlidePosition {
        LEFT,
        RIGHT
    }

    public PageIndicator(View container, View bar, SlidePosition currentPosition, ArrayList<View> icons, long delay) {
        this.container = container;
        this.bar = bar;
        this.currentPosition = currentPosition;
        this.delay = delay;
        this.icons = icons;
    }

    public void setAlpha() {
        icons.get(currentPosition == SlidePosition.LEFT ? 0 : 1).setAlpha(1);
        icons.get(currentPosition == SlidePosition.LEFT ? 1 : 0).setAlpha(.6f);
    }

    public void setSize() {
        if (!(bar.getLayoutParams() instanceof ViewGroup.MarginLayoutParams)) {
            throw new IllegalArgumentException();
        }
        ViewGroup.LayoutParams p = bar.getLayoutParams();
        p.width = container.getWidth() / 2;
        p.height = container.getHeight();
        bar.setLayoutParams(p);

        if(currentPosition == SlidePosition.RIGHT) {
            if (!(bar.getLayoutParams() instanceof ViewGroup.MarginLayoutParams)) {
                throw new IllegalArgumentException();
            }
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) bar.getLayoutParams();
            params.setMargins(container.getWidth() / 2, 0, 0, 0);
            bar.requestLayout();
        }
    }

    public void slide(SlidePosition position) {
        if (currentPosition == position) {
            return;
        }
        currentPosition = position;

        switch (position) {
            case LEFT:
                slideLeft();
                break;
            case RIGHT:
                slideRight();
                break;
        }
    }

    private void slideLeft() {
        final int margin = container.getWidth() / 2;

        ValueAnimator animator = ValueAnimator.ofInt(margin, 0);
        animator.setDuration(delay);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int animatedValue = (int) valueAnimator.getAnimatedValue();

                if (!(bar.getLayoutParams() instanceof ViewGroup.MarginLayoutParams)) {
                    throw new IllegalArgumentException();
                }
                ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) bar.getLayoutParams();
                p.setMargins(animatedValue, 0, 0, 0);
                bar.requestLayout();
            }
        });

        animator.start();
    }

    private void slideRight() {
        final int margin = container.getWidth() / 2;

        ValueAnimator animator = ValueAnimator.ofInt(0, margin);
        animator.setDuration(delay);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int animatedValue = (int) valueAnimator.getAnimatedValue();

                if (!(bar.getLayoutParams() instanceof ViewGroup.MarginLayoutParams)) {
                    throw new IllegalArgumentException();
                }
                ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) bar.getLayoutParams();
                p.setMargins(animatedValue, 0, 0, 0);
                bar.requestLayout();
            }
        });

        animator.start();
    }

    public void fadeIn(final int position) {
        fade(position, .6f, 1);
    }

    public void fadeOut(final int position) {
        fade(position, 1, .6f);
    }

    private void fade(final int position, float start, float end) {
        ValueAnimator animator = ValueAnimator.ofFloat(start, end);
        animator.setDuration(delay);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                icons.get(position).setAlpha((float) valueAnimator.getAnimatedValue());
            }
        });

        animator.start();
    }
}