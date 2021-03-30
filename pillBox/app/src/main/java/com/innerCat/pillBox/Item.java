package com.innerCat.pillBox;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.time.LocalDate;
import java.util.Objects;

/**
 * The type Item.
 */
//table name is 'entries'
@Entity(tableName = "items")
public class Item  {

    //set the primary key to auto generate and increment
    @PrimaryKey(autoGenerate = true)
    //placeholder id
    private int id = 0;
    private String name;
    private LocalDate lastUsed;
    @ColumnInfo(defaultValue = "0")
    private int stock = 0;
    private int viewHolderPosition;

    /**
     * Instantiates a new Item.
     *
     * @param name the name
     */
    public Item(String name) {
        this.name = name;
    }

    /**
     * Instantiates a new Item.
     *
     * @param name  the name
     * @param stock the stock
     */
    @Ignore
    public Item(String name, int stock) {
        this.name = name;
        this.stock = stock;
    }

    /**
     * Gets id.
     *
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * Sets id.
     *
     * @param id the id
     */
    public void setId( int id ) {
        this.id = id;
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name.
     *
     * @param name the name
     */
    public void setName( String name ) {
        this.name = name;
    }

    /**
     * Gets last updated.
     *
     * @return the last updated
     */
    public LocalDate getLastUsed() {
        return lastUsed;
    }

    /**
     * Sets last updated.
     *
     * @param lastUsed the last updated
     */
    public void setLastUsed( LocalDate lastUsed ) {
        this.lastUsed = lastUsed;
    }

    /**
     * Gets stock.
     *
     * @return the stock
     */
    public int getStock() {
        return stock;
    }

    /**
     * Sets stock.
     *
     * @param stock the stock
     */
    public void setStock( int stock ) {
        this.stock = stock;
    }

    /**
     * Decrements the stock and sets the last used to now
     */
    public void decrementStock() {
        if (this.stock > 0) {
            setLastUsed(LocalDate.now());
            this.stock = this.stock - 1;
        }
    }

    /**
     * Gets view holder position.
     *
     * @return the view holder position
     */
    public int getViewHolderPosition() {
        return viewHolderPosition;
    }

    /**
     * Sets view holder position.
     *
     * @param viewHolderPosition the view holder position
     */
    public void setViewHolderPosition( int viewHolderPosition ) {
        this.viewHolderPosition = viewHolderPosition;
    }

    /**
     * Refill.
     *
     * @param refillAmount the refill amount
     */
    public void refill(int refillAmount) {
        this.stock = this.stock+refillAmount;
    }


    @Override
    public boolean equals( Object o ) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return id == item.id &&
                stock == item.stock &&
                Objects.equals(name, item.name) &&
                Objects.equals(lastUsed, item.lastUsed);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, lastUsed, stock);
    }

    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", lastUpdated=" + lastUsed +
                ", stock=" + stock +
                '}';
    }
}

