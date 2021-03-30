package com.innerCat.pillBox.room;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.innerCat.pillBox.Item;

import java.util.List;


//The Data Access Object is the abstraction layer through which the database is manipulated
@Dao
public interface ItemDao {

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
     * @param item
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
     * Returns all Tasks as a List
     *
     * @return all the Tasks in the database as a List
     */
    @Query("SELECT * FROM items ORDER BY viewHolderPosition")
    public List<Item> getAllItems();

}
