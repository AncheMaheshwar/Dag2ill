# Mental Wellness Telegram Bot (Java + Spring Boot)

This bot implements your complete mental wellness flows (Overthinking, Anxiety, Low Self-Esteem, Depression, Social Comparison, Loneliness, Burnout, Stress) with inline keyboards and daily reminders.

## Setup
1. Install JDK 17+ and Maven.
2. Edit `src/main/resources/application.yml` and set:
```
telegram:
  bot:
    username: YOUR_BOT_USERNAME
    token: YOUR_BOT_TOKEN
reminders:
  hour: 9
  minute: 0
```
3. Run:
```
mvn spring-boot:run
```

## Commands
- `/start` – Greeting + main menu
- `/menu` – Show challenge menu
- `/stop` – Stop all reminders for your chat

Reminders are stored in `subscriptions.json` in the working directory and sent daily at the configured time (Asia/Kolkata).

> Note: Replace `<Google Form Link>` texts in `ReminderService` with your actual links if you have them.
