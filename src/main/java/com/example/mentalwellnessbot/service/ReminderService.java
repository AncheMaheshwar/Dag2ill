package com.example.mentalwellnessbot.service;

import com.example.mentalwellnessbot.model.Challenge;
import com.example.mentalwellnessbot.telegram.MentalWellnessBot;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ReminderService {

    private final MentalWellnessBot bot;
    private final Map<Long, Subscription> subscriptions = new HashMap<>();

    public ReminderService(MentalWellnessBot bot) {
        this.bot = bot;
    }

    public void addSubscription(Long chatId, Challenge challenge, int days) {
        subscriptions.put(chatId, new Subscription(challenge, days, new Date()));
    }

    public void removeForChat(Long chatId) {
        subscriptions.remove(chatId);
    }

    @Scheduled(fixedRate = 86400000) // 24 hours
    public void sendReminders() {
        Date now = new Date();
        subscriptions.forEach((chatId, sub) -> {
            int daysPassed = (int) ((now.getTime() - sub.startDate.getTime()) / (1000 * 60 * 60 * 24));
            if (daysPassed < sub.days) {
                bot.sendText(chatId, "🔔 Reminder: Stay consistent with your <b>" + sub.challenge + "</b> practice today!");
            }
        });
    }

    private record Subscription(Challenge challenge, int days, Date startDate) {}
}
