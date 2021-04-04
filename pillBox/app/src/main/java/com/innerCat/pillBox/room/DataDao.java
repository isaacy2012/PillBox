package com.innerCat.pillBox.room;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.innerCat.pillBox.Item;
import com.innerCat.pillBox.Refill;

import java.util.List;


//The Data Access Object is the abstraction layer through which the database is manipulated
@Dao
public interface DataDao {

    /**
     * Inserts an Task
     * In the case of a conflict, it simply replaces the existing Task
     *
     * @param item the Task to insert
     * @return the rowID (primary key) of the Task
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public long insert( Item item );

    /**
     * Updates an task
     *
     * @param item the item
     */
    @Update
    public void update( Item item );

    /**
     * Removes an Task by id
     *
     * @param id the id of the Task to remove
     */
    @Query("DELETE FROM items WHERE id = :id")
    public void removeById( int id );

    /**
     * Get a single Task from the id
     *
     * @param id the id (primary key) of the task
     * @return the task
     */
    @Query("SELECT * FROM items WHERE id = :id")
    public Item getItem( int id );

    /**
     * Returns all Items as a List
     *
     * @return all the Items in the database as a List
     */
    @Query("SELECT * FROM items ORDER BY viewHolderPosition")
    public List<Item> getAllItems();

    /**
     * Gets all widget items.
     *
     * @return all Items that should be shown in the widget as a List
     */
    @Query("SELECT * FROM items WHERE showInWidget is 1 ORDER BY viewHolderPosition")
    public List<Item> getAllWidgetItems();

    /**
     * Gets refills of item itemId.
     *
     * @param itemId the itemId
     * @return the refills of item itemId
     */
    @Query("SELECT * FROM refills WHERE itemId = :itemId")
    public List<Refill> getRefillsOfItemId( int itemId );

    @Query("SELECT * FROM refills WHERE itemId = :itemId ORDER BY julianday(expiryDate) ASC LIMIT 1")
    public Refill getSoonestExpiringRefillOfItemId(int itemId);

    /**
     * Inserts a Refill
     * In the case of a conflict, it simply replaces the existing Task
     *
     * @param refill the refill
     * @return the id of the Refill in the database
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public long insert( Refill refill );


    /**
     * Updates an refill
     *
     * @param refill the refill to update
     */
    @Update
    public void update( Refill refill );

    /**
     * Returns all Refills as a List
     *
     * @return all the Refills in the database as a List
     */
    @Query("SELECT * FROM refills")
    public List<Refill> getAllRefills();

    @Query("DELETE FROM refills WHERE julianday(expiryDate) < julianday(:today)")
    public void deleteRefillsOlderThanToday(String today);


}
