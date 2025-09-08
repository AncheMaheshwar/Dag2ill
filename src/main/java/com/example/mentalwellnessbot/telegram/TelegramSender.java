package com.example.mentalwellnessbot.telegram;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.Serializable;

@Component
public class TelegramSender {

    private final MentalWellnessBot bot;

    public TelegramSender(MentalWellnessBot bot) {
        this.bot = bot;
    }

    public <T extends Serializable> void send(BotApiMethod<T> method) {
        try {
            bot.execute(method);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void send(SendDocument doc) {
        try {
            bot.execute(doc);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
