// src/main/java/com/example/mentalwellnessbot/service/UserProgressService.java
package com.example.mentalwellnessbot.service;

import com.example.mentalwellnessbot.model.Mood;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserProgressService {

    private static final class Progress {
        int streak = 0;
        LocalDate lastActive = null;
        final Deque<Mood> recentMoods = new ArrayDeque<>(); // keep latest 7
        String pendingReminderPayload = null; // e.g. "ANXIETY:14"
    }

    private final Map<Long, Progress> store = new ConcurrentHashMap<>();

    private Progress p(long chatId) {
        return store.computeIfAbsent(chatId, k -> new Progress());
    }

    public void markActive(long chatId) {
        Progress pg = p(chatId);
        LocalDate today = LocalDate.now();
        if (pg.lastActive == null) {
            pg.streak = 1;
        } else if (pg.lastActive.plusDays(1).isEqual(today)) {
            pg.streak += 1;
        } else if (!pg.lastActive.isEqual(today)) {
            pg.streak = 1;
        }
        pg.lastActive = today;
    }

    public int getStreak(long chatId) {
        return p(chatId).streak;
    }

    public void recordMood(long chatId, Mood mood) {
        Progress pg = p(chatId);
        if (pg.recentMoods.size() == 7) pg.recentMoods.removeFirst();
        pg.recentMoods.addLast(mood);
        markActive(chatId);
    }

    public List<Mood> lastMoods(long chatId) {
        return List.copyOf(p(chatId).recentMoods);
    }

    public void setPendingReminder(long chatId, String payload) {
        p(chatId).pendingReminderPayload = payload;
    }

    public Optional<String> popPendingReminder(long chatId) {
        Progress pg = p(chatId);
        String val = pg.pendingReminderPayload;
        pg.pendingReminderPayload = null;
        return Optional.ofNullable(val);
    }
}
