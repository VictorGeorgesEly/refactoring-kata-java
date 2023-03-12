package com.sipios.refactoring.controller;

import com.sipios.refactoring.entity.Body;
import com.sipios.refactoring.entity.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Calendar;
import java.util.Map;
import java.util.TimeZone;

@RestController
@RequestMapping("/shopping")
public class ShoppingController {

    private final Logger logger = LoggerFactory.getLogger(ShoppingController.class);

    /**
     * This function is a REST API endpoint that takes in an HTTP POST request and calculates the total price of items in the request body based on their type, quantity, and any applicable discounts. The function first initializes a Calendar object with the timezone set to "Europe/Paris". It then retrieves the discount for the customer from the getDiscount() function and checks if the current date falls within a discounted period using the isDiscountedPeriod() function. If the request body contains no items, the function returns a string "0".
     * <p>
     * For each item in the request body, the function retrieves its price using the getItemPrice() function and calculates the total price of all items, taking into account the discount and quantity.
     * <p>
     * The function then retrieves the price limit for the customer type using the getCustomerPriceLimit() function. If the total price exceeds the price limit, the function throws a ResponseStatusException with a status code of 400 (Bad Request) and a message indicating that the price is too high for the customer type.
     * <p>
     * Finally, the function returns a string representation of the total price. If an error occurs during the execution of the function, it will be caught by the Spring framework and handled appropriately based on the configured error handling behavior.
     */
    @PostMapping
    public String getPrice(@RequestBody Body body) {
        double totalPrice = 0;

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Paris"));
        double discount = getDiscount(body.getType());
        boolean isDiscountedPeriod = isDiscountedPeriod(calendar);

        if (body.getItems() == null) {
            return "0";
        }

        for (Item item : body.getItems()) {
            double itemPrice = getItemPrice(item.getType(), isDiscountedPeriod);
            totalPrice += itemPrice * item.getQuantity() * discount;
        }

        int priceLimit = getCustomerPriceLimit(body.getType());
        if (totalPrice > priceLimit) {
            String message = "Price (" + totalPrice + ") is too high for " + body.getType().toLowerCase() + " customer";
            logger.debug(message);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }

        return String.valueOf(totalPrice);
    }

    /**
     * The isDiscountedPeriod function checks if the current date corresponds to a discount period or not. It takes as parameter a Calendar object which represents the current date.
     * <p>
     * The function first retrieves the day of the month and the current month from the Calendar object. Then it checks if the day of the month is between 6 and 14 (included) and if the month is January (0) or June (5). If it is the case, the function returns true, otherwise it returns false.
     * <p>
     * So if the function returns true, it means that the current date is a discount period and the price of the items will be adjusted accordingly in the getPrice function. Otherwise, the price will remain unchanged.
     */
    private boolean isDiscountedPeriod(Calendar calendar) {
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);

        return (dayOfMonth > 5 && dayOfMonth < 15) && (month == 0 || month == 5);
    }

    /**
     * This function takes in an item type and a boolean indicating if the current period is a discounted period. It then returns the price of the item based on its type and whether or not it is currently on sale.
     * <p>
     * The function uses a switch statement to handle different item types, with TSHIRT always being priced at 30. For DRESS and JACKET, the price depends on whether it is currently a discounted period or not. If it is, the item is priced lower (40 for DRESS and 90 for JACKET), otherwise the item is priced higher (50 for DRESS and 100 for JACKET). If the item type is not recognized, the function throws a BadRequestException.
     * <p>
     * This function is useful for calculating the price of each item in a shopping cart and can be used in conjunction with other functions to calculate the total cost of the shopping cart.
     */
    private double getItemPrice(String itemType, boolean isDiscountedPeriod) {
        switch (itemType) {
            case "TSHIRT":
                return 30;
            case "DRESS":
                return isDiscountedPeriod ? 40 : 50;
            case "JACKET":
                return isDiscountedPeriod ? 90 : 100;
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * This function takes a customer type as input and returns the corresponding price limit for that customer type. The price limits for each customer type are stored in a Map where the keys are the customer types and the values are the price limits. If the input customer type is not found in the Map, a default value of 200 is returned.
     * <p>
     * The function first initializes the customerPriceLimits Map with the price limits for each customer type using the Map.of method. This method creates an immutable Map with the specified key-value pairs. Then, the function retrieves the price limit for the input customer type using the getOrDefault method of the Map object. This method returns the value associated with the specified key if it exists in the Map, or the default value (200) if the key is not found. Finally, the function returns the retrieved price limit.
     */
    private int getCustomerPriceLimit(String customerType) {
        Map<String, Integer> customerPriceLimits = Map.of(
            "STANDARD_CUSTOMER", 200,
            "PREMIUM_CUSTOMER", 800,
            "PLATINUM_CUSTOMER", 2000
        );

        return customerPriceLimits.getOrDefault(customerType, 200);
    }

    /**
     * This function takes as input the type of customer ("STANDARD_CUSTOMER", "PREMIUM_CUSTOMER" or "PLATINUM_CUSTOMER") and returns the discount rate that applies for this type of customer.
     * <p>
     * If the customer type is not one of the three expected types, the function throws a "ResponseStatusException" with an HTTP code 400 (Bad Request) to indicate that the request is incorrect.
     * <p>
     * The discount rates associated with the different client types are defined in the function with a "switch" statement.
     */
    private double getDiscount(String customerType) {
        switch (customerType) {
            case "STANDARD_CUSTOMER":
                return 1.0;
            case "PREMIUM_CUSTOMER":
                return 0.9;
            case "PLATINUM_CUSTOMER":
                return 0.5;
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

}
