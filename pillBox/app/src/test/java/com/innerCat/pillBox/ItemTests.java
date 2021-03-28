package com.innerCat.pillBox;

import org.junit.Test;
import org.testng.annotations.AfterTest;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class ItemTests {

    @AfterTest
    public void check_date_base_constructor() {
        Item item = new Item("TestTask");
        assertEquals(LocalDate.now(), item.getLastUpdated());
        assertEquals(0, item.getStock());
        assertFalse(item.wasLate());
        assertFalse(item.runningLate());
        assertFalse(item.isComplete());
        assertNull(item.getCompleteDate());
    }

    @Test
    public void check_date_extended_constructor() {
        Item item = new Item("Extended", LocalDate.now().plusDays(3));
        assertEquals(LocalDate.now().plusDays(3), item.getLastUpdated());
    }

    @Test
    public void check_base_and_extended_constructor_equal() {
        Item a = new Item("A");
        Item b = new Item("A", LocalDate.now());
        assertEquals(a, b);
    }

    @Test
    public void should_be_late() {
        Item item = new Item("Late", LocalDate.now().minusDays(1));
        assertFalse(item.isComplete());
        assertFalse(item.wasLate());
        assertTrue(item.runningLate());
        item.toggleComplete();
        assertTrue(item.isComplete());
        assertTrue(item.wasLate());
    }

    @Test
    public void check_toggleComplete() {
        Item item = new Item("Toggle Complete");
        assertFalse(item.isComplete());
        item.toggleComplete();
        assertTrue(item.isComplete());
        item.toggleComplete();
        assertFalse(item.isComplete());
    }


    @Test
    public void not_late() {
        Item item = new Item("On Time", LocalDate.now());
        assertFalse(item.isComplete());
        assertFalse(item.wasLate());
        assertFalse(item.runningLate());
        item.toggleComplete();
        assertTrue(item.isComplete());
        assertFalse(item.wasLate());
    }

    @Test
    public void test_repeat_times() {
        Item item = new Item("Repeat");
        assertEquals(0, item.getStock());
        int repeatTime = 3;
        item.setStock(repeatTime);
        assertEquals(repeatTime, item.getStock());
    }

    @Test
    public void test_equals_true() {
        Item a = new Item("A");
        Item b = new Item("A");
        int id = 3;
        a.setId(id);
        b.setId(id);
        assertEquals(a, b);
    }

    @Test
    public void test_equals_false_name() {
        Item a = new Item("A");
        Item b = new Item("B");
        int id = 3;
        a.setId(id);
        b.setId(id);
        assertNotEquals(a, b);
    }

    @Test
    public void test_equals_false_id() {
        Item a = new Item("A");
        Item b = new Item("A");
        a.setId(3);
        b.setId(2);
        assertNotEquals(a, b);
    }
}
