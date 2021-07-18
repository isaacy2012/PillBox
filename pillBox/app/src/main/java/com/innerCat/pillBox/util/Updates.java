package com.innerCat.pillBox.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.innerCat.pillBox.factories.SharedPreferencesFactory;

public class Updates {
    /**
     * Sets update.
     *
     * @param updateString the update string
     * @param seen         the seen
     */
    private static void setUpdate(Context context, String updateString, boolean seen) {
        SharedPreferences.Editor editor = SharedPreferencesFactory.getSP(context).edit();
        editor.putBoolean(updateString, seen);
        editor.apply();
    }

    /**
     * Set a particular update as seen
     *
     * @param updateString the update string
     */
    public static void setUpdateSeen(Context context, String updateString) {
        setUpdate(context, updateString, true);
    }

    /**
     * Set a particular update as unseen
     *
     * @param updateString the update string
     */
    public static void setUpdateUnseen(Context context, String updateString) {
        setUpdate(context, updateString, false);
    }
}
