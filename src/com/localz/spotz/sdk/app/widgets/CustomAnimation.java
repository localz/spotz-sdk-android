package com.localz.spotz.sdk.app.widgets;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

public class CustomAnimation {
    public static final void startWaveAnimation(final View view) {
        ObjectAnimator scaleYAnim = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.5f);
        scaleYAnim.setDuration(2000);
        scaleYAnim.setInterpolator(new DecelerateInterpolator());

        ObjectAnimator scaleXAnim = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.5f);
        scaleXAnim.setDuration(2000);
        scaleXAnim.setInterpolator(new DecelerateInterpolator());

        ObjectAnimator alphaANim = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
        alphaANim.setDuration(1600);
        alphaANim.setStartDelay(400);

        final AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleYAnim, scaleXAnim, alphaANim);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                view.setAlpha(1);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                view.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        set.start();
                    }
                }, 1000);
            }
        });
        set.start();
    }
}
