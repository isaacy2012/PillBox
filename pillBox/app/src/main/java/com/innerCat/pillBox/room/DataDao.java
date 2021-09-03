package com.innerCat.pillBox.room;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.innerCat.pillBox.objects.Item;
import com.innerCat.pillBox.objects.Refill;

import java.util.List;


//The Data Access Object is the abstraction layer through which the database is manipulated
@Dao
public interface DataDao {

    /**
     * Inserts an Item
     * In the case of a conflict, it simply replaces the existing Item
     *
     * @param item the Item to insert
     * @return the rowID (primary key) of the Item
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert( Item item );

    /**
     * Updates an task
     *
     * @param item the item
     */
    @Update
    void update( Item item );

    /**
     * Removes an Item by id
     *
     * @param id the id of the Item to remove
     */
    @Query("DELETE FROM items WHERE id = :id")
    void removeItemById( int id );

    /**
     * Removes an Item by id
     *
     * @param id the id of the Item to remove
     */
    @Query("DELETE FROM refills WHERE itemId = :id")
    void removeRefillsOfItemId( int id );

    /**
     * Get a single Item from the id
     *
     * @param id the id (primary key) of the task
     * @return the task
     */
    @Query("SELECT * FROM items WHERE id = :id")
    Item getItem( int id );

    /**
     * Returns all Items as a List
     *
     * @return all the Items in the database as a List
     */
    @Query("SELECT * FROM items ORDER BY viewHolderPosition")
    List<Item> getAllItems();

    /**
     * Gets all widget items.
     *
     * @return all Items that should be shown in the widget as a List
     */
    @Query("SELECT * FROM items WHERE showInWidget is 1 ORDER BY viewHolderPosition")
    List<Item> getAllWidgetItems();

    /**
     * Gets refills of item itemId.
     *
     * @param itemId the itemId
     * @return the refills of item itemId
     */
    @Query("SELECT * FROM refills WHERE itemId = :itemId AND julianday(expiryDate) >= julianday(:today)")
    List<Refill> getFutureRefillsOfItemId( int itemId, String today );

    /**
     * Gets non expiring refills of item id.
     *
     * @param itemId the item id
     * @return the non expiring refills of item id
     */
    @Query("SELECT * FROM refills WHERE itemId = :itemId AND expires is 0")
    List<Refill> getNonExpiringRefillsOfItemId( int itemId );

    /**
     * Gets expired refills of item id.
     *
     * @param itemId the item id
     * @param today  the today
     * @return the expired refills of item id
     */
    @Query("SELECT * FROM refills WHERE itemId = :itemId AND julianday(expiryDate) < julianday(:today)")
    List<Refill> getExpiredRefillsOfItemId( int itemId, String today );

    /**
     * Gets soonest expiring refill of item id.
     *
     * @param itemId the item id
     * @param today  the today
     * @return the soonest expiring refill of item id
     */
    @Query( "SELECT * FROM refills " +
            "WHERE itemId = :itemId " +
            "AND expires is 1 " +
            "AND julianday(expiryDate) >= julianday(:today) " +
            "ORDER BY julianday(expiryDate) " +
            "ASC LIMIT 1")
    Refill getSoonestExpiringRefillOfItemId( int itemId, String today );


    /**
     * Removes a Refill by id
     *
     * @param id the id of the Item to remove
     */

    /**
     * Inserts a Refill
     * In the case of a conflict, it simply replaces the existing Item
     *
     * @param refill the refill
     * @return the id of the Refill in the database
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert( Refill refill );

    /**
     * Updates an refill
     *
     * @param refill the refill to update
     */
    @Update
    void update( Refill refill );

    /**
     * Returns all Refills as a List
     *
     * @return all the Refills in the database as a List
     */
    @Query("SELECT * FROM refills")
    List<Refill> getAllRefills();


    /**
     * Remove refill by id.
     *
     * @param id the id
     */
    @Query("DELETE FROM refills WHERE id = :id")
    void removeRefillById( int id );

}
