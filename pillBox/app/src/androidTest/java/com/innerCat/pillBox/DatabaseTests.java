package com.innerCat.pillBox;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.MediumTest;

import com.innerCat.pillBox.room.ItemDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


@MediumTest
@RunWith(AndroidJUnit4.class)
public class DatabaseTests {
    private Context context;
    private ItemDatabase itemDatabase;
    private SharedPreferences sharedPreferences;

    /**
     * Add a task into the database, setting its id in the process
     * @param item the task to add
     */
    public void addItem( Item item ) {
        int id = (int) itemDatabase.itemDao().insert(item);
        item.setId(id);
    }

    /**
     * Make and add a task with name and offset quickly
     *
     * @param name         the name of the task
     * @param initialStock the initial stock
     * @return the item
     */
    public Item makeAndAddItem( String name, int initialStock ) {
        Item newItem = new Item( name, initialStock );
        addItem(newItem);
        return newItem;
    }

    @Before
    public void createDb() {
        context = ApplicationProvider.getApplicationContext();
        itemDatabase = Room.inMemoryDatabaseBuilder(context, ItemDatabase.class).build();
        sharedPreferences = getPreferences();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();
    }

    /**
     * Get the test sharedPreferences
     * @return sharedPreferences
     */
    private SharedPreferences getPreferences() {
        return context.getSharedPreferences("test", Context.MODE_PRIVATE);
    }

    @After
    public void closeDb() throws IOException {
        itemDatabase.close();
    }

    /**
     * Check that insert and retrieve on 'id' returns the same task
     */
    @Test
    public void check_retrieve() {
        Item item = new Item("A");
        int id = (int) itemDatabase.itemDao().insert(item);
        item.setId(id);
        Item retrievedItem = itemDatabase.itemDao().getItem(id);
        assertEquals(retrievedItem, item);
    }

    /**
     * Check that deleting from database works
     */
    @Test
    public void check_delete() {
        Item item = new Item("A");
        int id = (int) itemDatabase.itemDao().insert(item);
        item.setId(id);
        itemDatabase.itemDao().removeById(id);
        Item retrievedItem = itemDatabase.itemDao().getItem(id);
        assertNull(retrievedItem);
    }

}
