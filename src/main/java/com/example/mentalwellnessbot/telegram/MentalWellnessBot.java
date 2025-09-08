package com.example.mentalwellnessbot.telegram;

import com.example.mentalwellnessbot.config.BotProperties;
import com.example.mentalwellnessbot.model.Challenge;
import com.example.mentalwellnessbot.service.FlowService;
import com.example.mentalwellnessbot.service.ReminderService;
import com.example.mentalwellnessbot.service.PaymentService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
public class MentalWellnessBot extends TelegramLongPollingBot {

    private final BotProperties properties;
    private final FlowService flowService;
    private final ReminderService reminderService;
    private final PaymentService paymentService;

    public MentalWellnessBot(
            BotProperties properties,
            FlowService flowService,
            @Lazy ReminderService reminderService,
            PaymentService paymentService
    ) {
        super(properties.getToken());
        this.properties = properties;
        this.flowService = flowService;
        this.reminderService = reminderService;
        this.paymentService = paymentService;
    }

    @Override
    public String getBotUsername() {
        return properties.getUsername();
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                String chatId = update.getMessage().getChatId().toString();
                String text = update.getMessage().getText().trim();
                String username = update.getMessage().getFrom().getFirstName();

                if (text.equalsIgnoreCase("/start") || text.equalsIgnoreCase("hi") || text.equalsIgnoreCase("hello")) {
                    // ✅ Check payment first
                    if (!paymentService.checkPayment(chatId)) {
                        String link = paymentService.createPaymentLink(chatId, 29);
                        sendText(update.getMessage().getChatId(),
                                "💳 To continue using the Mental Wellness Assistant, please complete your subscription:\n\n" +
                                        "👉 [Pay Now](" + link + ")\n\n" +
                                        "After payment, type /menu to unlock.");
                        return;
                    }
                    sendWelcome(chatId, username);

                } else if (text.equalsIgnoreCase("/pay")) {
                    String link = paymentService.createPaymentLink(chatId, 29);
                    sendText(update.getMessage().getChatId(),
                            "💳 Please complete your subscription:\n\n" +
                                    "👉 [Pay Now](" + link + ")\n\n" +
                                    "After payment, type /menu to unlock.");
                } else if (text.equalsIgnoreCase("/menu")) {
                    if (!paymentService.checkPayment(chatId)) {
                        sendText(update.getMessage().getChatId(),
                                "🚫 Access Locked!\n\n" +
                                        "You need a subscription to use this bot.\n\n" +
                                        "👉 Use /pay to purchase access (₹1).");
                        return;
                    }
                    sendMenu(chatId);

                } else if (text.equalsIgnoreCase("/stop")) {
                    reminderService.removeForChat(update.getMessage().getChatId());
                    sendText(update.getMessage().getChatId(),
                            "⏹ Okay, I’ve stopped your reminders.\n\n👉 You can start again anytime with /menu.");
                } else {
                    sendText(update.getMessage().getChatId(),
                            "🤔 I didn’t quite get that.\n\nPlease use the buttons below or type /menu to continue 👇");
                }

            } else if (update.hasCallbackQuery()) {
                var cq = update.getCallbackQuery();
                Long chatId = cq.getMessage().getChatId();
                String data = cq.getData();

                // ✅ Block callbacks if not paid
                if (!paymentService.checkPayment(chatId.toString())) {
                    sendText(chatId,
                            "🚫 Access Locked!\n\n" +
                                    "You need a subscription to use this bot.\n\n" +
                                    "👉 Use /pay to purchase access (₹1).");
                    return;
                }

                if ("MENU:OPEN".equals(data)) {
                    sendMenu(chatId.toString());
                    return;
                }

                // "FLOW:NEXT:<CHALLENGE>:<DAY>"
                if (data.startsWith("FLOW:NEXT:")) {
                    try {
                        String[] p = data.split(":");
                        Challenge c = Challenge.valueOf(p[2]);
                        int day = Integer.parseInt(p[3]);
                        send(flowService.flow(chatId.toString(), c, day));
                    } catch (Exception e) {
                        sendText(chatId, "⚠️ Couldn’t open the next day. Try /menu.");
                    }
                    return;
                }

                // "CHALLENGE:<NAME>"
                if (data.startsWith("CHALLENGE:") && !data.startsWith("CHALLENGE:CHECKIN:")) {
                    try {
                        Challenge c = Challenge.valueOf(data.split(":")[1]);
                        send(flowService.flow(chatId.toString(), c));
                    } catch (Exception e) {
                        sendText(chatId, "⚠️ Unknown challenge. Try /menu.");
                    }
                    return;
                }

                // "REMIND:START:<CHALLENGE>:<DAYS>"
                if (data.startsWith("REMIND:START:")) {
                    try {
                        String[] parts = data.split(":");
                        Challenge c = Challenge.valueOf(parts[2]);
                        int days = Integer.parseInt(parts[3]);
                        reminderService.addSubscription(chatId, c, days);
                        sendText(chatId,
                                "✅ Done! I’ll remind you daily for " + days + " days.\n\nUse /stop anytime to cancel reminders.");
                    } catch (Exception e) {
                        sendText(chatId, "⚠️ Couldn’t set reminders. Try again from /menu.");
                    }
                    return;
                }

                // Optional: check-in tap at Day 21
                if (data.startsWith("CHALLENGE:CHECKIN:")) {
                    sendText(chatId,
                            "🗣️ Thanks for completing the program!\n\nIf you’d like a psychologist check-in, please reply with your preferred time window (e.g., “Evenings 7–9pm, IST”). We’ll get back to you.");
                    return;
                }

                // fallback
                sendText(chatId, "🤷 Not sure what that was. Try /menu.");
            }
        } catch (Exception e) {
            try {
                Long chatId = null;
                if (update.hasMessage()) chatId = update.getMessage().getChatId();
                else if (update.hasCallbackQuery()) chatId = update.getCallbackQuery().getMessage().getChatId();
                if (chatId != null) sendText(chatId, "⚠️ Oops, something went wrong. Try /menu.");
            } catch (Exception ignore) {}
        }
    }

    /** ----------- Helpers ----------- **/

    private void sendWelcome(String chatId, String username) {
        String welcomeText = "👋 Hello <b>" + (username == null ? "there" : username) + "</b>!\n\n" +
                "I’m your <b>Mental Wellness Assistant</b> 🌱\n\n" +
                "I’ll guide you with short videos, simple tools, and daily nudges.\n\n" +
                "Which challenge would you like to work on today?";
        SendMessage msg = SendMessage.builder()
                .chatId(chatId)
                .text(welcomeText)
                .parseMode("HTML")
                .replyMarkup(flowService.mainMenu())
                .build();
        send(List.of(msg));
    }

    private void sendMenu(String chatId) {
        SendMessage m = SendMessage.builder()
                .chatId(chatId)
                .text("🌟 Please choose a challenge below:")
                .replyMarkup(flowService.mainMenu())
                .build();
        send(List.of(m));
    }

    public void sendText(Long chatId, String text) {
        try {
            execute(SendMessage.builder()
                    .chatId(chatId.toString())
                    .text(text)
                    .parseMode("Markdown") // so [Pay Now](url) works
                    .build());
        } catch (Exception ignored) {}
    }

    private void send(List<SendMessage> messages) {
        for (SendMessage m : messages) {
            try {
                if (m.getParseMode() == null) m.setParseMode("HTML");
                execute(m);
            } catch (Exception ignored) {}
        }
    }
}
