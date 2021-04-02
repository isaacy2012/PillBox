package com.innerCat.pillBox.factories;

import android.content.Context;

import androidx.room.Room;

import com.innerCat.pillBox.room.ItemDatabase;

public class ItemDatabaseFactory {
    public static ItemDatabase getItemDatabase( Context context ) {
        return Room.databaseBuilder(context.getApplicationContext(),
                ItemDatabase.class, "items")
                .fallbackToDestructiveMigration()
                .build();
    }
}
