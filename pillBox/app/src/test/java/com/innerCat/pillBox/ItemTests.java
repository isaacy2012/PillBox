package com.innerCat.pillBox;

import com.innerCat.pillBox.objects.Item;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class ItemTests {

    @Test
    public void check_date_base_constructor() {
        Item item = new Item("Test Pill");
        assertEquals("Test Pill", item.getName());
        assertNull(item.getExpiringRefill());
        assertNull(item.getLastUsed());
    }

    @Test
    public void stock_cant_go_below_zero() {
        Item item = new Item("Test", 10, false);
        for (int i = 0; i < 100; i++) {
            item.decrementStock();
        }
        assertEquals(0, item.getStock());
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
