package com.innerCat.pillBox.factories;

import android.content.Context;

import androidx.room.Room;

import com.innerCat.pillBox.room.Database;

public class DatabaseFactory {
    public static Database create( Context context ) {
        return Room.databaseBuilder(context.getApplicationContext(),
                Database.class, "items")
                .fallbackToDestructiveMigration()
                .build();
    }
}
