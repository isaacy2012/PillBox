package com.innerCat.pillBox.objects;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.time.LocalDate;

import static java.time.temporal.ChronoUnit.DAYS;

/**
 * The type Box.
 */
//table name is 'refills'
@Entity(tableName = "refills")
public class Refill extends RefillListObject implements Comparable<Refill> {
    //set the primary key to auto generate and increment
    @PrimaryKey(autoGenerate = true)
    //placeholder id
    private int id = 0;

    private int itemId;
    private int amount;
    private LocalDate expiryDate;


    /**
     * Instantiates a new Box.
     *
     * @param expiryDate the expiry date
     * @param amount     the amount
     */
    public Refill( int itemId, int amount, LocalDate expiryDate ) {
        this.itemId = itemId;
        this.amount = amount;
        this.expiryDate = expiryDate;
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
     * Gets item id.
     *
     * @return the item id
     */
    public int getItemId() {
        return itemId;
    }

    /**
     * Sets item id.
     *
     * @param itemId the item id
     */
    public void setItemId( int itemId ) {
        this.itemId = itemId;
    }

    /**
     * Gets expiry date.
     *
     * @return the expiry date
     */
    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    /**
     * Sets expiry date.
     *
     * @param expiryDate the expiry date
     */
    public void setExpiryDate( LocalDate expiryDate ) {
        this.expiryDate = expiryDate;
    }

    /**
     * Gets amount.
     *
     * @return the amount
     */
    public int getAmount() {
        return amount;
    }

    /**
     * Sets amount.
     *
     * @param amount the amount
     */
    public void setAmount( int amount ) {
        this.amount = amount;
    }

    /**
     * Merge with other Refill
     *
     * @param other the other Refill
     */
    public void mergeWith( Refill other ) {
        int currentAmount = this.amount;
        this.amount = currentAmount+other.getAmount();
    }
    @Override
    public int hashCode() {
        int result = itemId;
        result = 31 * result + (expiryDate != null ? expiryDate.hashCode() : 0);
        return result;
    }


    @Override
    public int compareTo( Refill o ) {
        return (int) DAYS.between(o.getExpiryDate(), this.getExpiryDate());
    }

    @Override
    public String toString() {
        return "Refill{" +
                "id=" + id +
                ", itemId=" + itemId +
                ", expiryDate=" + expiryDate +
                ", amount=" + amount +
                '}';
    }
}
