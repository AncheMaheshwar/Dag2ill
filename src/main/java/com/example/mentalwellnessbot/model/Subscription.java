package com.example.mentalwellnessbot.model;

import java.time.LocalDate;

public class Subscription {
    private Long chatId;
    private Challenge challenge;
    private int totalDays;
    private LocalDate startDate;
    private LocalDate lastSent;

    public Subscription() {}

    public Subscription(Long chatId, Challenge challenge, int totalDays, LocalDate startDate) {
        this.chatId = chatId;
        this.challenge = challenge;
        this.totalDays = totalDays;
        this.startDate = startDate;
    }

    public Long getChatId() { return chatId; }
    public void setChatId(Long chatId) { this.chatId = chatId; }

    public Challenge getChallenge() { return challenge; }
    public void setChallenge(Challenge challenge) { this.challenge = challenge; }

    public int getTotalDays() { return totalDays; }
    public void setTotalDays(int totalDays) { this.totalDays = totalDays; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getLastSent() { return lastSent; }
    public void setLastSent(LocalDate lastSent) { this.lastSent = lastSent; }
}
