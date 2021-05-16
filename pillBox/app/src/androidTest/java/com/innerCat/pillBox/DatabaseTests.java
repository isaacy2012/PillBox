package com.innerCat.pillBox;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.MediumTest;

import com.innerCat.pillBox.objects.Item;
import com.innerCat.pillBox.objects.Refill;
import com.innerCat.pillBox.room.Converters;
import com.innerCat.pillBox.room.Database;

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
    private Database database;
    private SharedPreferences sharedPreferences;

    /**
     * Add a item into the database, setting its id in the process
     * @param item the item to add
     */
    public void addItem( Item item ) {
        int id = (int) database.getDao().insert(item);
        item.setId(id);
    }

    /**
     * Add refill.
     *
     * @param refill the refill
     */
    public void addRefill( Refill refill ) {
        int id = (int) database.getDao().insert(refill);
        refill.setId(id);
    }

    /**
     * Make and add item item with name, initial stock, and widget quickly
     *
     * @param name         the name of the item
     * @param initialStock the initial stock
     * @return the item
     */
    public Item makeAndAddItem( String name, int initialStock, boolean showInWidget) {
        Item newItem = new Item( name, initialStock, showInWidget);
        addItem(newItem);
        return newItem;
    }

    /**
     * Make and add item item with name, initial stock quickly
     *
     * @param name         the name
     * @param initialStock the initial stock
     * @return the item
     */
    public Item makeAndAddItem( String name, int initialStock) {
        Item newItem = new Item( name, initialStock, false);
        addItem(newItem);
        return newItem;
    }

    /**
     * Make and add refill with itemId, amount, and expiryDate.
     *
     * @param itemId     the item id
     * @param amount     the amount
     * @param expiryDate the expiry date
     * @return the refill
     */
    public Refill makeAndAddRefill( int itemId, int amount, LocalDate expiryDate ) {
        Refill newRefill = new Refill( itemId, amount, expiryDate);
        addRefill(newRefill);
        return newRefill;
    }

    /**
     * Make and add refill with itemId and amount.
     *
     * @param itemId     the item id
     * @param amount     the amount
     * @return the refill
     */
    public Refill makeAndAddRefill( int itemId, int amount ) {
        Refill newRefill = new Refill( itemId, amount, null);
        addRefill(newRefill);
        return newRefill;
    }

    @Before
    public void createDb() {
        context = ApplicationProvider.getApplicationContext();
        database = Room.inMemoryDatabaseBuilder(context, Database.class).build();
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
        database.close();
    }

    /**
     * Check that insert and retrieve on 'id' returns the same item
     */
    @Test
    public void check_retrieve() {
        Item item = new Item("A");
        int id = (int) database.getDao().insert(item);
        item.setId(id);
        Item retrievedItem = database.getDao().getItem(id);
        assertEquals(retrievedItem, item);
    }

    /**
     * Check that deleting from database works
     */
    @Test
    public void check_delete() {
        Item item = new Item("A");
        int id = (int) database.getDao().insert(item);
        item.setId(id);
        database.getDao().removeItemById(id);
        Item retrievedItem = database.getDao().getItem(id);
        assertNull(retrievedItem);
    }

    @Test
    public void soonest_expiring_refill() {
        Item item = makeAndAddItem("Test", 1, false);
        LocalDate earlier = LocalDate.now().plusDays(360);
        LocalDate later = LocalDate.now().plusDays(370);
        makeAndAddRefill(item.getId(), 15, earlier);
        makeAndAddRefill(item.getId(), 15, later);
        assertEquals(earlier, database.getDao()
                .getSoonestExpiringRefillOfItemId(item.getId(), Converters.todayString())
                .getExpiryDate());

    }

}
