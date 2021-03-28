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
    private LocalDate lastUpdated;
    @ColumnInfo(defaultValue = "0")
    private int stock = 0;
    private int viewHolderLocation;

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
    public LocalDate getLastUpdated() {
        return lastUpdated;
    }

    /**
     * Sets last updated.
     *
     * @param lastUpdated the last updated
     */
    public void setLastUpdated( LocalDate lastUpdated ) {
        this.lastUpdated = lastUpdated;
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
     * Decrements the stock.
     */
    public void decrementStock() {
        if (this.stock > 0) {
            this.stock = this.stock - 1;
        }
    }

    /**
     * Refill.
     *
     * @param refillAmount the refill amount
     */
    public void refill(int refillAmount) {
        this.stock = this.stock+refillAmount;
    }


    /**
     * Gets view holder location.
     *
     * @return the view holder location
     */
    public int getViewHolderLocation() {
        return viewHolderLocation;
    }

    /**
     * Sets view holder location.
     *
     * @param viewHolderLocation the view holder location
     */
    public void setViewHolderLocation( int viewHolderLocation ) {
        this.viewHolderLocation = viewHolderLocation;
    }

    @Override
    public boolean equals( Object o ) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return id == item.id &&
                stock == item.stock &&
                Objects.equals(name, item.name) &&
                Objects.equals(lastUpdated, item.lastUpdated);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, lastUpdated, stock, viewHolderLocation);
    }

    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", lastUpdated=" + lastUpdated +
                ", stock=" + stock +
                ", viewHolderLocation=" + viewHolderLocation +
                '}';
    }
}

