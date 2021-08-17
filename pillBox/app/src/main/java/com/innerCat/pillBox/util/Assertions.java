package com.innerCat.pillBox.util;

import java.util.Objects;

/**
 * The type Assertions.
 */
public class Assertions {

    public static void throwAssertionError(String... message) {
        if (message.length == 0) {
            throw new AssertionError("Assertion Failed");
        } else {
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (String s : message) {
                if (first) {
                    sb.append(s);
                    first = false;
                } else {
                    sb.append("\n").append(s);
                }
            }
            throw new AssertionError(sb.toString());
        }
    }

    /**
     * Assert true.
     *
     * @param predicate the predicate
     * @param message   the message
     */
    public static void assertTrue( boolean predicate, String... message) {
        if (predicate == false) {
            throwAssertionError(message);
        }
    }

    /**
     * Assert false.
     *
     * @param predicate the predicate
     * @param message   the message
     */
    public static void assertFalse(boolean predicate, String... message) {
        if (predicate) {
            throwAssertionError(message);
        }
    }

    /**
     * Assert null.
     *
     * @param obj     the obj
     * @param message the message
     */
    public static void assertNull(Object obj, String... message) {
        if (obj != null) {
            throwAssertionError(message);
        }
    }

    /**
     * Assert not null.
     *
     * @param obj     the obj
     * @param message the message
     */
    public static void assertNotNull(Object obj, String... message) {
        if (obj == null) {
            throwAssertionError(message);
        }
    }

    /**
     * Assert equals.
     *
     * @param a       the a
     * @param b       the b
     * @param message the message
     */
    public static void assertEquals(Object a, Object b, String... message) {
        if (Objects.equals(a, b) == false) {
            throwAssertionError(message);
        }
    }

}
