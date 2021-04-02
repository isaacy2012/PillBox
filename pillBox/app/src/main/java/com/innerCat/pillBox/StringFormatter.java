package com.innerCat.pillBox;

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
            sb.append("Last taken ");
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
}
