package com.innerCat.pillBox;

import com.innerCat.pillBox.objects.Item;
import com.innerCat.pillBox.objects.Refill;

import java.time.LocalDate;

import static java.time.temporal.ChronoUnit.DAYS;

public class StringFormatter {

    /**
     * Gets last taken text.
     *
     * @param item              the item
     */
    public static String getLastTakenText( Item item ) {
        LocalDate lastUsed = item.getLastUsed();
        if (lastUsed != null) {
            int daysBetween = (int) DAYS.between(lastUsed, LocalDate.now());
            StringBuilder sb = new StringBuilder();
            if (item.isAutoDec()) {
                sb.append("Last manually taken ");
            } else {
                sb.append("Last taken ");
            }
            if (daysBetween == 0) {
                sb.append("today");
            } else if (daysBetween == 1) {
                sb.append("yesterday");
            } else {
                sb.append(daysBetween).append(" days ago");
            }
            return sb.toString();
        } else {
            return "";
        }
    }


    /**
     * Date to string string.
     *
     * @param date the date
     * @return the string
     */
    public static String dateToString(LocalDate date) {
        return date.toString();
    }

    /**
     * Gets expiry text.
     *
     * @param refill the refill
     * @return the expiry text
     */
    public static String getExpiryText( Refill refill) {
        long daysTillExpiry = DAYS.between(LocalDate.now(), refill.getExpiryDate());
        if (daysTillExpiry == 0) {
            return refill.getAmount() + " expiring today";
        } else if (daysTillExpiry == 1) {
            return refill.getAmount() + " expiring tomorrow";
        } else {
            return refill.getAmount() + " expiring in " + daysTillExpiry + " days";
        }
    }
}
