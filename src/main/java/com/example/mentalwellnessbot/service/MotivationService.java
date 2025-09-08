// src/main/java/com/example/mentalwellnessbot/service/MotivationService.java
package com.example.mentalwellnessbot.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class MotivationService {
    private final Random rnd = new Random();

    private static final List<String> QUOTES = List.of(
            "🌱 Small steps daily lead to big change.",
            "💪 You’re stronger than you think.",
            "🧠 Thoughts are not facts. Breathe and choose the next helpful action.",
            "✨ Progress, not perfection.",
            "☀️ A short walk can reset your whole day."
    );

    public String quote() {
        return QUOTES.get(rnd.nextInt(QUOTES.size()));
    }

    public String badgeFor(int streak) {
        if (streak >= 21) return "🏆 Resilience Badge";
        if (streak >= 14) return "🌟 Consistency Badge";
        if (streak >= 7)  return "🌱 Growth Badge";
        return null;
    }
}
