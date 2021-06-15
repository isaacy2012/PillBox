package com.innerCat.pillBox;

import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AssertionTests {

    @Test
    public void assert_true_false_no_message() {
        try {
            Assertions.assertTrue(false);
            assert false;
        } catch (AssertionError e) {
            assertEquals(e.getMessage(), "Assertion Failed");
        }
    }

    @Test
    public void assert_true_true_no_message() {
        Assertions.assertTrue(true);
    }

    @Test
    public void assert_true_false_with_message() {
        try {
            Assertions.assertTrue(false, "Message");
            assert false;
        } catch (AssertionError e) {
            assertEquals(e.getMessage(), "Message");
        }
    }

    @Test
    public void assert_true_false_with_multiple_message() {
        try {
            Assertions.assertTrue(false, "Message", "Message2");
            assert false;
        } catch (AssertionError e) {
            assertEquals("Message\nMessage2", e.getMessage());
        }
    }
}
