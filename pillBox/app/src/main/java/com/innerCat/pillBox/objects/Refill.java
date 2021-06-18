package com.innerCat.pillBox.objects;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.innerCat.pillBox.Assertions;

import java.io.Serializable;
import java.time.LocalDate;

import static java.time.temporal.ChronoUnit.DAYS;

/**
 * The type Box.
 */
//table name is 'refills'
@Entity(tableName = "refills")
public class Refill extends RefillListObject implements Comparable<Refill>, Serializable {
    /**
     * The Id.
     */
//set the primary key to auto generate and increment
    @PrimaryKey(autoGenerate = true)
    //placeholder id
    private int id = 0;

    /**
     * The Item id.
     */
    private int itemId;
    /**
     * The Amount.
     */
    private int amount;
    /**
     * The Expiry date.
     */
    private LocalDate expiryDate;
    /**
     * The Expires.
     */
    private boolean expires;


    /**
     * Instantiates a new Box.
     *
     * @param itemId     the item id
     * @param amount     the amount
     * @param expiryDate the expiry date
     */
    public Refill( int itemId, int amount, LocalDate expiryDate ) {
        this.itemId = itemId;
        this.amount = amount;
        setExpiryDate(expiryDate);
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
        setExpires(expiryDate != null);
        this.expiryDate = expiryDate;
    }

    /**
     * Is expires boolean.
     *
     * @return the boolean
     */
    public boolean getExpires() {
        return expires;
    }

    /**
     * Sets expires.
     *
     * @param expires the expires
     */
    public void setExpires( boolean expires ) {
        this.expires = expires;
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
        Assertions.assertNotNull(this.getExpiryDate());
        Assertions.assertNotNull(o.getExpiryDate());
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
