package com.innerCat.pillBox;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.MediumTest;

import com.innerCat.pillBox.room.Converters;
import com.innerCat.pillBox.room.DBMethods;
import com.innerCat.pillBox.room.ItemDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.time.LocalDate;

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
    public void addTask( Item item ) {
        int id = (int) itemDatabase.itemDao().insert(item);
        item.setId(id);
    }

    /**
     * Make and add a task with name and offset quickly
     * @param name the name of the task
     * @param dayOffset the dayOffset (from today)
     */
    public Item makeAndAddTask( String name, int dayOffset, boolean completed) {
        if (dayOffset > 0) {
            Item addItem = new Item(name, LocalDate.now().plusDays(dayOffset));
            if (completed == true) {
                addItem.setCompleteDate(addItem.getLastUsed());
            }
            addTask(addItem);
            return addItem;
        } else if (dayOffset < 0) {
            Item addItem = new Item(name, LocalDate.now().minusDays(-dayOffset));
            if (completed == true) {
                addItem.setCompleteDate(addItem.getLastUsed());
            }
            addTask(addItem);
            return addItem;
        } else {
            Item addItem = new Item(name);
            if (completed == true) {
                addItem.setCompleteDate(addItem.getLastUsed());
            }
            addTask(addItem);
            return addItem;
        }
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

    @Test
    public void check_getDateTasks1() {
        Item expected = makeAndAddTask("D", -4, false);
        makeAndAddTask("A", -3, true);
        makeAndAddTask("B", -3, true);
        makeAndAddTask("C", -3, true);
        Item actual = itemDatabase.itemDao().getLastStreakTask(Converters.todayString());
        assertEquals(expected, actual);
    }

    @Test
    public void check_getDateTasks2() {
        makeAndAddTask("A", -4, false);
        makeAndAddTask("B", -3, true);
        makeAndAddTask("C", -3, true);
        Item expected = makeAndAddTask("D", -3, false);
        Item actual = itemDatabase.itemDao().getLastStreakTask(Converters.todayString());
        assertEquals(expected, actual);
    }

    @Test
    public void check_maxStreak() {
        int maxStreak = 10;
        makeAndAddTask("A", -(maxStreak+1), false);
        makeAndAddTask("B", -4, true);
        makeAndAddTask("C", -3, true);
        makeAndAddTask("D", -2, true);
        makeAndAddTask("E", -1, true);
        int actualMaxStreak = DBMethods.calculateMaxStreak(context, itemDatabase);
        assertEquals(maxStreak, actualMaxStreak);
    }



}
