package com.innerCat.pillBox.objects;

/**
 * The type Refill list header.
 */
public class RefillListHeader extends RefillListObject {
    /**
     * The Title.
     */
    private String title;

    /**
     * Instantiates a new Refill list header.
     *
     * @param title the title
     */
    public RefillListHeader( String title ) {
        this.title = title;
    }

    /**
     * Gets title.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }
}
