package com.hindbyte.dating.cardstackview.internal;

import android.view.animation.Interpolator;

import com.hindbyte.dating.cardstackview.Direction;


public interface AnimationSetting {
    Direction getDirection();
    int getDuration();
    Interpolator getInterpolator();
}
