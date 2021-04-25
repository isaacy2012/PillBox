package com.innerCat.pillBox.factories;

import android.app.Activity;
import android.content.Context;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.appbar.AppBarLayout;
import com.innerCat.pillBox.R;

public class OnOffsetChangedListenerFactory {

    public static AppBarLayout.OnOffsetChangedListener create( Context context ) {
        return new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

                CoordinatorLayout coordinatorLayout = ((Activity) context).findViewById(R.id.coordinatorLayout);
                if (Math.abs(verticalOffset)-appBarLayout.getTotalScrollRange() == 0) {
                    //  Collapsed
                    coordinatorLayout.setClipChildren(true);

                } else {
                    //Expanded
                    coordinatorLayout.setClipChildren(false);
                }
            }
        };
    }


}
