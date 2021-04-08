package com.innerCat.pillBox.room;

import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.innerCat.pillBox.objects.Item;
import com.innerCat.pillBox.objects.Refill;

@androidx.room.Database(entities = { Item.class, Refill.class }, version = 2)
@TypeConverters({ Converters.class })
public abstract class Database extends RoomDatabase {
    public abstract DataDao getDao();
}


