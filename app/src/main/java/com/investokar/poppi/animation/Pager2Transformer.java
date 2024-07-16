package com.investokar.poppi.animation;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

public class Pager2Transformer implements ViewPager2.PageTransformer {

    private static final float ROTATION = -15f;
    private static final float MIN_SCALE = 0.9f;
    private static final float ALPHA_SCALE = 0.1f;

    @Override
    public void transformPage(@NonNull View page, float position) {
        float scaleFactor = MIN_SCALE + ( 1 - MIN_SCALE ) * ( 1 - Math.abs(position));
        page.setScaleX(scaleFactor);
        page.setScaleY(scaleFactor);
        float alphaFactor = ALPHA_SCALE + ( 1 - ALPHA_SCALE ) * ( 1 - Math.abs(position));
        page.setAlpha(alphaFactor);

        final float width = page.getWidth();
        final float height = page.getHeight();
        final float rotation = ROTATION * position * -1.25f;

        page.setPivotX(width*0.5f);
        page.setPivotY(height);
        page.setRotation(rotation);
    }

}