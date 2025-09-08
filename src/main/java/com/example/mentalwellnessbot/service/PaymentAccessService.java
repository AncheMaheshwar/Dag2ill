package com.example.mentalwellnessbot.service;

import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class PaymentAccessService {

    private final Set<String> paidUsers = new HashSet<>();

    public boolean hasPaid(String chatId) {
        return paidUsers.contains(chatId);
    }

    public void markPaid(String chatId) {
        paidUsers.add(chatId);
    }
}
