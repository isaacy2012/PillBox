package com.innerCat.pillBox.factories;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesFactory {
    public static SharedPreferences getSP( Context context ) {
        return context.getSharedPreferences("preferences", Context.MODE_PRIVATE);

    }
}
