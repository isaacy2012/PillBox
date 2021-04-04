package com.innerCat.pillBox.room;

import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.innerCat.pillBox.Item;
import com.innerCat.pillBox.Refill;

@androidx.room.Database(entities = { Item.class, Refill.class }, version = 1)
@TypeConverters({ Converters.class })
public abstract class Database extends RoomDatabase {
    public abstract DataDao getDao();
}


