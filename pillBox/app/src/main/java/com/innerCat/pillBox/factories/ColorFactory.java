package com.innerCat.pillBox.factories;

import android.content.Context;
import android.content.res.TypedArray;

import androidx.core.content.ContextCompat;

import com.innerCat.pillBox.R;

public class ColorFactory {

    /**
     * Gets default text color.
     *
     * @param context the context
     * @return the default text color
     */
    public static int getDefaultTextColor( Context context ) {
        int[] attribute = new int[]{ android.R.attr.textColor };
        TypedArray array = context.getTheme().obtainStyledAttributes(attribute);
        return array.getColor(0, ContextCompat.getColor(context, R.color.transparent));
    }
}
