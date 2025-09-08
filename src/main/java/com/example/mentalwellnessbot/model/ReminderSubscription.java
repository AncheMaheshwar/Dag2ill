// src/main/java/com/example/mentalwellnessbot/model/ReminderSubscription.java
package com.example.mentalwellnessbot.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class ReminderSubscription {
    private final long chatId;
    private final Challenge challenge;
    private final int totalDays;
    private final LocalDate startDate;
    private LocalTime preferredTime; // local time
    private LocalDate lastSentOn;
    private int completedDays;

    public ReminderSubscription(long chatId, Challenge challenge, int totalDays, LocalTime preferredTime) {
        this.chatId = chatId;
        this.challenge = challenge;
        this.totalDays = totalDays;
        this.startDate = LocalDate.now();
        this.preferredTime = preferredTime;
        this.lastSentOn = null;
        this.completedDays = 0;
    }

    public long getChatId() { return chatId; }
    public Challenge getChallenge() { return challenge; }
    public int getTotalDays() { return totalDays; }
    public LocalDate getStartDate() { return startDate; }
    public LocalTime getPreferredTime() { return preferredTime; }
    public void setPreferredTime(LocalTime t) { this.preferredTime = t; }
    public LocalDate getLastSentOn() { return lastSentOn; }
    public void setLastSentOn(LocalDate d) { this.lastSentOn = d; }
    public int getCompletedDays() { return completedDays; }
    public void incrementCompletedDays() { this.completedDays++; }

    public boolean isFinished() {
        return completedDays >= totalDays;
    }

    public boolean shouldSendNow(LocalDateTime now) {
        if (isFinished()) return false;
        if (lastSentOn != null && lastSentOn.isEqual(now.toLocalDate())) return false;
        return now.toLocalTime().getHour() == preferredTime.getHour()
                && now.toLocalTime().getMinute() == preferredTime.getMinute();
    }
}
