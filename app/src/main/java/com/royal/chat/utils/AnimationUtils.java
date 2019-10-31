package com.royal.chat.utils;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;

import com.royal.chat.R;

public class AnimationUtils {

    private Context context;
    private static AnimationUtils instance;
    private Animation zoomIn, zoomOut;

    public static AnimationUtils getInstance(Context context) {
        if (instance == null) {
            instance = new AnimationUtils(context);
        }
        return instance;
    }

    private AnimationUtils(Context context) {
        this.context = context;
        zoomIn = android.view.animation.AnimationUtils.loadAnimation(context, R.anim.zoom_in);
        zoomOut = android.view.animation.AnimationUtils.loadAnimation(context, R.anim.zoom_out);
    }

    public void animate(final View view) {
        view.startAnimation(zoomIn);
        zoomIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.startAnimation(zoomOut);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }
}
