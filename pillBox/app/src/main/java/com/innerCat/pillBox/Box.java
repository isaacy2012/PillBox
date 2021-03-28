package com.innerCat.pillBox;

import java.time.LocalDate;

import static java.time.temporal.ChronoUnit.DAYS;

/**
 * The type Box.
 */
public class Box implements Comparable<Box> {
    LocalDate expiryDate;
    int amount;

    /**
     * Instantiates a new Box.
     *
     * @param expiryDate the expiry date
     * @param amount     the amount
     */
    public Box( LocalDate expiryDate, int amount ) {
        this.expiryDate = expiryDate;
        this.amount = amount;
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

    @Override
    public int compareTo( Box o ) {
        return (int) DAYS.between(o.getExpiryDate(), this.getExpiryDate());
    }

}
