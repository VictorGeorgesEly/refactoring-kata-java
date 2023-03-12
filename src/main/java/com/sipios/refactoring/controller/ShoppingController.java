package com.sipios.refactoring.controller;

import java.util.Calendar;
import java.util.Map;
import java.util.TimeZone;

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

@RestController
@RequestMapping("/shopping")
public class ShoppingController {

    private Logger logger = LoggerFactory.getLogger(ShoppingController.class);

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

    private boolean isDiscountedPeriod(Calendar calendar) {
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);

        return (dayOfMonth > 5 && dayOfMonth < 15) && (month == 0 || month == 5);
    }

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

    private int getCustomerPriceLimit(String customerType) {
        Map<String, Integer> customerPriceLimits = Map.of(
            "STANDARD_CUSTOMER", 200,
            "PREMIUM_CUSTOMER", 800,
            "PLATINUM_CUSTOMER", 2000
        );

        return customerPriceLimits.getOrDefault(customerType, 200);
    }

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
