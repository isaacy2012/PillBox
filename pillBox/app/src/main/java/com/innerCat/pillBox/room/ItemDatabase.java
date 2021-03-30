package com.innerCat.pillBox.room;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.innerCat.pillBox.Item;

@Database(entities = { Item.class }, version = 1)
@TypeConverters({ Converters.class })
public abstract class ItemDatabase extends RoomDatabase {
    public abstract ItemDao itemDao();
}


