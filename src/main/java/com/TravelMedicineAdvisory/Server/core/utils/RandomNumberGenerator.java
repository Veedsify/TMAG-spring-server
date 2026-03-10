package com.TravelMedicineAdvisory.Server.core.utils;

import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Component;

@Component
public class RandomNumberGenerator {

    public String generateNumber() {
        return String.format("%06d",
                ThreadLocalRandom.current().nextInt(1000000));
    }
}
