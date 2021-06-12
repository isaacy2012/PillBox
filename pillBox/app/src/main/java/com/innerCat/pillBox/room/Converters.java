package com.innerCat.pillBox.room;

import android.content.res.Resources;
import android.os.Bundle;
import android.util.TypedValue;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.innerCat.pillBox.objects.Refill;
import com.innerCat.pillBox.objects.Item;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
     * From box list string.
     *
     * @param refills the boxes
     * @return the string
     */
    @TypeConverter
    public String fromBoxList(List<Refill> refills ) {
        if (refills == null) {
            return (null);
        }
        Gson gson = new Gson();
        Type type = new TypeToken<List<Refill>>() {}.getType();
        return gson.toJson(refills, type);
    }

    /**
     * To box list list.
     *
     * @param boxesString the boxes string
     * @return the list
     */
    @TypeConverter
    public List<Refill> toBoxList( String boxesString) {
        if (boxesString == null) {
            return (null);
        }
        Gson gson = new Gson();
        Type type = new TypeToken<List<Refill>>() {}.getType();
        return gson.fromJson(boxesString, type);
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
     * Checks if a bundle contains all the keys required
     *
     * @param bundle the bundle
     * @param keys   the keys
     * @return the boolean
     */
    private static boolean bundleContainsAllKeys( Bundle bundle, List<String> keys ) {
        for (String key : keys) {
            if (bundle.containsKey(key) == false) {
                return false;
            }
        }
        return true;
    }

}
