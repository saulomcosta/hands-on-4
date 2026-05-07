package com.hands_on.arquiteto.integration;

import java.util.Random;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    public void processPayment() {
        System.out.println("Processing payment...");

        if (new Random().nextBoolean()) {
            throw new RuntimeException("Payment failed");
        }

        System.out.println("Payment processed successfully");
    }
}
