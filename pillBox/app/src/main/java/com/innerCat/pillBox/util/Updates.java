package com.innerCat.pillBox.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.innerCat.pillBox.BuildConfig;
import com.innerCat.pillBox.factories.SharedPreferencesFactory;

public class Updates {
    /**
     * Sets update.
     *
     * @param context the context
     * @param seen    the seen
     */
    private static void setUpdate(Context context, boolean seen) {
        SharedPreferences.Editor editor = SharedPreferencesFactory.getSP(context).edit();
        editor.putBoolean(BuildConfig.VERSION_NAME, seen);
        editor.apply();
    }

    /**
     * Set a particular update as seen
     *
     * @param context the context
     */
    public static void setUpdateSeen(Context context) {
        setUpdate(context, true);
    }

    /**
     * Set a particular update as unseen
     *
     * @param context the context
     */
    public static void setUpdateUnseen(Context context) {
        setUpdate(context, false);
    }

    public static boolean shouldShowUpdateDialog(Context context) {
        return !SharedPreferencesFactory.getSP(context).getBoolean(BuildConfig.VERSION_NAME, false);
    }

    public static String getUpdateBodyString() {
        return "" +
                "<b>Tweaks</b>" +
                "<br>" +
                "- Tweaked the size of the toolbar, moving the title down" +
                "<br>" +
                "<br>" +
                "<b>Bugfixes</b>" +
                "<br>- Fixed a bug where the OK button was disabled by default when editing an item" +
                "<br>" +
                "<br>If you have any bug reports or feature requests, feel free to leave a review or post an issue on our <a href=\"https://github.com/isaacy2012/Pillbox\">GitHub repository</a>." +
                "<br>" +
                "<br>" +
                "Thanks for using Pillbox!";
    }
}
