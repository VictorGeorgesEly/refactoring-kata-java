package com.sipios.refactoring.controller;

import com.sipios.refactoring.UnitTest;
import com.sipios.refactoring.entity.Body;
import com.sipios.refactoring.entity.Item;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.web.server.ResponseStatusException;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShoppingControllerTests extends UnitTest {

    @InjectMocks
    private ShoppingController controller;

    @Test
    void should_not_throw() {
        Assertions.assertDoesNotThrow(
            () -> controller.getPrice(new Body(new Item[] {}, "STANDARD_CUSTOMER"))
        );
    }

    @Test
    public void testGetDiscount() {
        ShoppingController shoppingController = new ShoppingController();
        // Test standard customer
        double discount = shoppingController.getDiscount("STANDARD_CUSTOMER");
        assertEquals(1.0, discount, 0.001);

        // Test premium customer
        discount = shoppingController.getDiscount("PREMIUM_CUSTOMER");
        assertEquals(0.9, discount, 0.001);

        // Test platinum customer
        discount = shoppingController.getDiscount("PLATINUM_CUSTOMER");
        assertEquals(0.5, discount, 0.001);

        // Test invalid customer
        Assertions.assertThrows(ResponseStatusException.class, () -> {
            shoppingController.getDiscount("INVALID_CUSTOMER");
        });
    }

    @Test
    void testGetCustomerPriceLimit() {
        ShoppingController shoppingController = new ShoppingController();

        // Test for standard customer
        int priceLimit = shoppingController.getCustomerPriceLimit("STANDARD_CUSTOMER");
        assertEquals(200, priceLimit);

        // Test for premium customer
        priceLimit = shoppingController.getCustomerPriceLimit("PREMIUM_CUSTOMER");
        assertEquals(800, priceLimit);

        // Test for platinum customer
        priceLimit = shoppingController.getCustomerPriceLimit("PLATINUM_CUSTOMER");
        assertEquals(2000, priceLimit);

        // Test for unknown customer type
        priceLimit = shoppingController.getCustomerPriceLimit("UNKNOWN_CUSTOMER");
        assertEquals(200, priceLimit);
    }

    @Test
    void testGetItemPrice() {
        ShoppingController shoppingController = new ShoppingController();
        // Test with TSHIRT item type
        double price = shoppingController.getItemPrice("TSHIRT", false);
        assertEquals(30, price);

        // Test with DRESS item type during discounted period
        price = shoppingController.getItemPrice("DRESS", true);
        assertEquals(40, price);

        // Test with DRESS item type outside of discounted period
        price = shoppingController.getItemPrice("DRESS", false);
        assertEquals(50, price);

        // Test with JACKET item type during discounted period
        price = shoppingController.getItemPrice("JACKET", true);
        assertEquals(90, price);

        // Test with JACKET item type outside of discounted period
        price = shoppingController.getItemPrice("JACKET", false);
        assertEquals(100, price);

        // Test with invalid item type
        Assertions.assertThrows(ResponseStatusException.class, () -> shoppingController.getItemPrice("INVALID", true));
    }

    @Test
    void testIsDiscountedPeriod() {
        // create a calendar for January 10th
        Calendar january10th = new GregorianCalendar(2023, 0, 10);
        january10th.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));

        ShoppingController shoppingController = new ShoppingController();

        // check that January 10th is not in a discounted period
        Assertions.assertTrue(shoppingController.isDiscountedPeriod(january10th));

        // create a calendar for June 1st
        Calendar june1st = new GregorianCalendar(2023, 5, 1);
        june1st.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));

        // check that June 1st is in a discounted period
        Assertions.assertFalse(shoppingController.isDiscountedPeriod(june1st));
    }

    @Test
    void getPrice() {
        // Create a new Body instance
        Item[] items = {new Item("TSHIRT", 2), new Item("DRESS", 1)};
        Body body = new Body(items, "STANDARD_CUSTOMER");

        // Call the function and check the result
        ShoppingController shoppingController = new ShoppingController();
        String result = shoppingController.getPrice(body);

        assertEquals("90.0", result);
    }

    @Test
    void getPriceWithDiscount() {
        // Create a new Body instance for a premium customer during a discounted period
        Item[] items = {new Item("TSHIRT", 2), new Item("DRESS", 1), new Item("JACKET", 1)};
        Body body = new Body(items, "PREMIUM_CUSTOMER");

        // Call the function and check the result
        ShoppingController shoppingController = new ShoppingController();
        String result = shoppingController.getPrice(body);

        assertEquals("225.0", result);
    }

    @Test
    void getPriceWithLimitExceeded() {
        // Create a new Body instance with items that exceed the customer's price limit
        Item[] items = {new Item("JACKET", 4)};
        Body body = new Body(items, "STANDARD_CUSTOMER");

        // Call the function and check that it throws an exception
        ShoppingController shoppingController = new ShoppingController();
        Assertions.assertThrows(ResponseStatusException.class, () -> shoppingController.getPrice(body), "Price (400.0) is too high for standard customer customer");
    }
}
