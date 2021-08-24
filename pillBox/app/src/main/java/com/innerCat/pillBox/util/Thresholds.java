package com.innerCat.pillBox.util;

import android.content.Context;

import com.innerCat.pillBox.R;
import com.innerCat.pillBox.factories.SharedPreferencesFactory;
import com.innerCat.pillBox.room.Converters;

public class Thresholds {

    /**
     * Gets stock threshold.
     *
     * @param context the context
     * @return the stock threshold
     */
    public static int getStockThreshold(Context context) {
        int defaultRedThreshold = context.getResources().getInteger(R.integer.default_red_stock_threshold);
        String stockThresholdString = SharedPreferencesFactory.getSP(context)
                .getString(context.getString(R.string.sp_red_stock_threshold), null);
        return Converters.getIntFromStringSharedPreferences(context, stockThresholdString, defaultRedThreshold);
    }

    /**
     * Gets warning day threshold.
     *
     * @param context the context
     * @return the warning day threshold
     */
    public static int getWarningDayThreshold(Context context) {
        int defaultWarningDayThreshold = context.getResources().getInteger(R.integer.default_warning_day_threshold);
        String warningDayThresholdString = SharedPreferencesFactory.getSP(context)
                .getString(context.getString(R.string.sp_warning_day_threshold), null);
        return Converters.getIntFromStringSharedPreferences(context, warningDayThresholdString, defaultWarningDayThreshold);
    }

    /**
     * Gets red day threshold.
     *
     * @param context the context
     * @return the red day threshold
     */
    public static int getRedDayThreshold(Context context) {
        int defaultRedDayThreshold = context.getResources().getInteger(R.integer.default_red_day_threshold);
        String redDayThresholdString = SharedPreferencesFactory.getSP(context)
                .getString(context.getString(R.string.sp_red_day_threshold), null);
        return Converters.getIntFromStringSharedPreferences(context, redDayThresholdString, defaultRedDayThreshold);
    }
}
