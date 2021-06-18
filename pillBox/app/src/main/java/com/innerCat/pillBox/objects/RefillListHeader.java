package com.innerCat.pillBox.objects;

/**
 * The type Refill list header.
 */
public class RefillListHeader extends RefillListObject {
    /**
     * The Title.
     */
    private final String title;
    /**
     * The Color.
     */
    private final int color;

    /**
     * Instantiates a new Refill list header.
     *
     * @param title the title
     * @param color the color
     */
    public RefillListHeader( String title, int color ) {
        this.title = title;
        this.color = color;
    }

    /**
     * Gets title.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets color.
     *
     * @return the color
     */
    public int getColor() {
        return color;
    }
}
