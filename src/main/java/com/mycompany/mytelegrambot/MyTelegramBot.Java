package com.mycompany.mytelegrambot;

import com.mycompany.mytelegrambot.service.TelegramBotService;

/**
 * MyTelegramBot - Main bot class
 * 
 * This class extends TelegramBotService which contains all the bot logic.
 * It's kept simple to maintain clean architecture.
 * 
 * The actual bot implementation is in TelegramBotService.java
 */
public class MyTelegramBot extends TelegramBotService {
    
    public MyTelegramBot() {
        super();
        System.out.println("🤖 MyTelegramBot instance created");
    }
    
    @Override
    public void onRegister() {
        super.onRegister();
        System.out.println("📝 Bot registered successfully with Telegram API");
    }
    private void sendAndLog(Long chatId, String text) {
    SendMessage message = new SendMessage();
    message.setChatId(chatId.toString());
    message.setText(text);

    try {
        execute(message);

        // Simpan outgoing message
        Message outgoing = new Message();
        outgoing.setChatId(chatId);
        outgoing.setDirection("outgoing");
        outgoing.setMessage(text);
        outgoing.setTimestamp(LocalDateTime.now());
        DatabaseManager.getInstance().saveMessage(outgoing);

    } catch (TelegramApiException e) {
        e.printStackTrace();
    }
}

}