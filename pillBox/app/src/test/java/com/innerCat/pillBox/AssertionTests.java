package com.innerCat.pillBox;

import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AssertionTests {

    @Test
    public void assert_true_false_no_message() {
        try {
            Assertions.assertTrue(false);
        } catch (AssertionError e) {
            assertEquals(e.getMessage(), "Assertion Failed");
            return;
        }
        assert false;
    }

    @Test
    public void assert_true_true_no_message() {
        Assertions.assertTrue(true);
    }

    @Test
    public void assert_true_false_with_message() {
        try {
            Assertions.assertTrue(false, "Message");
        } catch (AssertionError e) {
            assertEquals(e.getMessage(), "Message");
            return;
        }
        assert false;
    }

    @Test
    public void assert_true_false_with_multiple_message() {
        try {
            Assertions.assertTrue(false, "Message", "Message2");
        } catch (AssertionError e) {
            assertEquals("Message\nMessage2", e.getMessage());
            return;
        }
        assert false;
    }

    @Test
    public void assert_false_true_no_message() {
        try {
            Assertions.assertFalse(true);
        } catch (AssertionError e) {
            assertEquals(e.getMessage(), "Assertion Failed");
            return;
        }
        assert false;
    }

    @Test
    public void assert_false_false_no_message() {
        Assertions.assertFalse(false);
    }

    @Test
    public void assert_false_true_with_message() {
        try {
            Assertions.assertFalse(true, "Message");
        } catch (AssertionError e) {
            assertEquals(e.getMessage(), "Message");
            return;
        }
        assert false;
    }

    @Test
    public void assert_false_true_with_multiple_message() {
        try {
            Assertions.assertFalse(true, "Message", "Message2");
        } catch (AssertionError e) {
            assertEquals("Message\nMessage2", e.getMessage());
            return;
        }
        assert false;
    }

    @Test
    public void assert_null_null() {
        Assertions.assertNull(null);
    }

    @Test
    public void assert_null_notnull() {
        try {
            Assertions.assertNull(2);
        } catch (AssertionError ignored) {
            return;
        }
        assert false;
    }

    @Test
    public void assert_notnull_notnull() {
        Assertions.assertNotNull(2);
    }

    @Test
    public void assert_notnull_null() {
        try {
            Assertions.assertNotNull(null);
        } catch (AssertionError ignored) {
            return;
        }
        assert false;
    }

    @Test
    public void assert_equals_equals() {
        Assertions.assertEquals(1, 1);
    }

    @Test
    public void assert_equals_notequals() {
        try {
            Assertions.assertEquals(1, 2);
        } catch (AssertionError ignored) {
            return;
        }
        assert false;
    }
}
