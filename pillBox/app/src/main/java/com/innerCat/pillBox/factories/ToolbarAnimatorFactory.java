package com.innerCat.pillBox.factories;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.view.MenuItem;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.color.MaterialColors;
import com.innerCat.pillBox.R;

public class ToolbarAnimatorFactory {
    public static ValueAnimator create( Context context, boolean editMode,
                                        MenuItem editButton, CollapsingToolbarLayout toolbarLayout ) {
        int defaultColor = MaterialColors.getColor(context, R.attr.colorOnToolbar, Color.TRANSPARENT);
        int primaryColor = ContextCompat.getColor(context, R.color.primaryColor);
        ValueAnimator colorAnimator = new ValueAnimator();
        if (editMode) {
            //set the toolbar to red
            colorAnimator.setIntValues(defaultColor, primaryColor);
            editButton.setIcon(R.drawable.ic_baseline_close_24);
        } else {
            //set the toolbar to clear
            colorAnimator.setIntValues(primaryColor, defaultColor);
            editButton.setIcon(R.drawable.ic_baseline_edit_24);
        }
        colorAnimator.setEvaluator(new ArgbEvaluator());
        colorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate( ValueAnimator animation ) {
                int animatedValue = (int) animation.getAnimatedValue();
                toolbarLayout.setContentScrimColor(animatedValue);
            }
        });
        //colorAnimator.setDuration(ANIMATION_DURATION);
        colorAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        return colorAnimator;
    }
}
