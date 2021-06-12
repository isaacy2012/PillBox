package com.innerCat.pillBox;

import com.innerCat.pillBox.objects.ColorItem;
import com.innerCat.pillBox.objects.Item;

import org.junit.Test;

import java.time.LocalDate;

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
        assertEquals(0, item.getRawStock());
    }

    @Test
    public void calculated_stock_1_1_1() {
        //Yesterday
        int minusDays = 1;
        Item item = new Item("Test", 100, ColorItem.NO_COLOR, false, LocalDate.now().minusDays(minusDays), 1, 1);
        assertEquals(99, item.getCalculatedStock());
    }

    @Test
    public void calculated_stock_5_1_1() {
        //Yesterday
        int minusDays = 5;
        Item item = new Item("Test", 100, ColorItem.NO_COLOR, false, LocalDate.now().minusDays(minusDays), 1, 1);
        assertEquals(95, item.getCalculatedStock());
    }

    @Test
    public void calculated_stock_5_2_1() {
        //Yesterday
        int minusDays = 5;
        Item item = new Item("Test", 100, ColorItem.NO_COLOR, false, LocalDate.now().minusDays(minusDays), 2, 1);
        assertEquals(90, item.getCalculatedStock());
    }

    @Test
    public void calculated_stock_5_2_3() {
        //Yesterday
        int minusDays = 5;
        Item item = new Item("Test", 100, ColorItem.NO_COLOR, false, LocalDate.now().minusDays(minusDays), 2, 3);
        assertEquals(98, item.getCalculatedStock());
    }

    @Test
    public void calculated_stock_10_5_7() {
        //Yesterday
        int minusDays = 20;
        Item item = new Item("Test", 100, ColorItem.NO_COLOR, false, LocalDate.now().minusDays(minusDays), 5, 7);
        assertEquals(90, item.getCalculatedStock());
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
