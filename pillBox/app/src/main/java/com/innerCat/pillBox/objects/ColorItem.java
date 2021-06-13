package com.innerCat.pillBox.objects;

import android.graphics.Color;

import androidx.annotation.ColorInt;
import androidx.core.graphics.ColorUtils;

import java.util.Objects;

/**
 * The type Color item.
 */
public class ColorItem {

    public static int NO_COLOR = -1;
    /**
     * The Color.
     */
    @ColorInt private int color;
    /**
     * The Selected.
     */
    private boolean selected;

    /**
     * Instantiates a new Color item.
     *
     * @param colorString the color string
     */
    public ColorItem(String colorString) {
        color = Color.parseColor(colorString);
    }

    /**
     * Gets color.
     *
     * @return the color
     */
    public int getColor() {
        return color;
    }

    /**
     * Gets outline color.
     *
     * @return the outline color
     */
    public int getOutlineColor() {
        return ColorUtils.blendARGB(color, Color.BLACK, 0.2f);
    }

    /**
     * Is selected boolean.
     *
     * @return the boolean
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * Sets selected.
     *
     * @param selected the selected
     */
    public void setSelected( boolean selected ) {
        this.selected = selected;
    }

    /**
     * Toggle selected.
     */
    public void toggleSelected() {
        this.selected = !this.selected;
    }

    @Override
    public boolean equals( Object o ) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColorItem colorItem = (ColorItem) o;
        return color == colorItem.color;
    }

    @Override
    public int hashCode() {
        return Objects.hash(color);
    }
}
