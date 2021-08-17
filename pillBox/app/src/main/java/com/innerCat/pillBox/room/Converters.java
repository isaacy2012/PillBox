package com.innerCat.pillBox.room;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.TypedValue;

import androidx.room.TypeConverter;

import com.innerCat.pillBox.objects.Item;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Converters {
    /**
     * Convert from a timestamp String to a LocalDate (with date ONLY)
     * @param value the String to convert
     * @return the LocalDate
     */
    @TypeConverter
    public static LocalDate fromTimestamp( String value ) {
        if (value == null) {
            return null;
        }
        return LocalDate.parse(value);
    }

    /**
     * Converts the date to a String timestamp
     * @param date the date (LocalDate with date ONLY)
     * @return the timestamp as a String
     */
    @TypeConverter
    public static String dateToTimestamp( LocalDate date ) {
        if (date == null) {
            return null;
        }
        return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    /**
     * @return Today's dateString
     */
    public static String todayString() {
        return dateToTimestamp(LocalDate.now());
    }

    /**
     * Converts dp to pixels. Used for setting padding programmatically and responsively
     *
     * @param dp the dp
     * @param r  resources
     * @return the number of pixels
     */
    public static int fromDpToPixels( int dp, Resources r ) {
        float px = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                (float) dp,
                r.getDisplayMetrics()
        );
        return (int) px;
    }

    /**
     * Gets edit bundle from item and position for editing.
     *
     * @param item     the item
     * @param position the position
     * @return the edit bundle from item and position
     */
    public static Bundle getExtrasFromItemAndPosition( Item item, int position ) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("item", item);
        bundle.putInt("position", position);
        return bundle;
    }

    /**
     * Gets int from string shared preferences.
     *
     * @param context                the context
     * @param sharedPreferencesValue the shared preferences value
     * @param defaultValue           the default value
     * @return the int from string shared preferences
     */
    public static int getIntFromStringSharedPreferences(Context context, String sharedPreferencesValue, int defaultValue) {
        if (sharedPreferencesValue != null) {
            try {
                return Integer.parseInt(sharedPreferencesValue);
            } catch (NumberFormatException ignored) {}
        }
        return defaultValue;
    }

}
