package com.innerCat.pillBox.room;

import android.content.res.Resources;
import android.util.TypedValue;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.innerCat.pillBox.Box;

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
     * @param boxes the boxes
     * @return the string
     */
    @TypeConverter
    public String fromBoxList(List<Box> boxes) {
        if (boxes == null) {
            return (null);
        }
        Gson gson = new Gson();
        Type type = new TypeToken<List<Box>>() {}.getType();
        return gson.toJson(boxes, type);
    }

    /**
     * To box list list.
     *
     * @param boxesString the boxes string
     * @return the list
     */
    @TypeConverter
    public List<Box> toBoxList(String boxesString) {
        if (boxesString == null) {
            return (null);
        }
        Gson gson = new Gson();
        Type type = new TypeToken<List<Box>>() {}.getType();
        return gson.fromJson(boxesString, type);
    }
}
