package com.innerCat.pillBox.factories;

import android.content.Context;

import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.innerCat.pillBox.room.Database;

public class DatabaseFactory {
    public static Database create( Context context ) {
        return Room.databaseBuilder(context.getApplicationContext(),
                Database.class, "items")
//                .fallbackToDestructiveMigration()
                .addMigrations(MIGRATION_3_4)
                .build();
    }

    public static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE items "
                    + " ADD COLUMN lastUsedTime VARCHAR");
        }
    };
}
