package com.mycompany.mytelegrambot;

import java.io.InputStream;
import java.util.Properties;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import com.mycompany.mytelegrambot.service.TelegramBotService;

public class MainApp {
    
    public static void main(String[] args) {
        System.out.println("🚀 Starting Fitness Telegram Bot...");
        
        // Validate configuration
        if (!validateConfiguration()) {
            System.err.println("❌ Bot configuration is incomplete. Please check config.properties");
            return;
        }
        
        try {
            // Initialize Telegram Bots API
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            
            // Register bot
            TelegramBotService bot = new TelegramBotService();
            botsApi.registerBot(bot);
            
            System.out.println("✅ Fitness Bot started successfully!");
            System.out.println("📱 Bot is ready to receive messages...");
            System.out.println("\n📋 Features available:");
            System.out.println("   • Member registration and verification");
            System.out.println("   • Nutrition information and tips");
            System.out.println("   • Gym exercises and workout guides");
            System.out.println("   • Health and fitness news");
            System.out.println("   • Admin panel for member management");
            System.out.println("   • Broadcast messaging system");
            System.out.println("\n🔧 Admin commands:");
            System.out.println("   • /admin - Show admin panel");
            System.out.println("   • /members - List all members");
            System.out.println("   • /broadcast category|title|content - Send broadcast");
            System.out.println("   • /stats - Show bot statistics");
            System.out.println("\n👤 User commands:");
            System.out.println("   • /start - Start interaction with bot");
            System.out.println("   • /register - Register as new member");
            System.out.println("   • /menu - Show main menu");
            System.out.println("   • /nutrisi - Nutrition information");
            System.out.println("   • /gym - Gym and exercise guides");
            System.out.println("   • /berita - Health and fitness news");
            System.out.println("   • /broadcast - View recent broadcasts");
            System.out.println("\n💡 Keywords supported:");
            System.out.println("   • protein, kalori, diet (nutrition)");
            System.out.println("   • squat, push up, pull up (exercises)");
            System.out.println("   • trending, research, tips (news)");
            
        } catch (TelegramApiException e) {
            System.err.println("❌ Error starting bot: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static boolean validateConfiguration() {
        try (InputStream input = MainApp.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.err.println("❌ config.properties file not found in resources folder");
                return false;
            }
            
            Properties prop = new Properties();
            prop.load(input);
            
            String botToken = prop.getProperty("bot.token");
            String botUsername = prop.getProperty("bot.username");
            String adminChatId = prop.getProperty("admin.chat.id");
            
            if (botToken == null || botToken.equals("YOUR_BOT_TOKEN_HERE") || botToken.trim().isEmpty()) {
                System.err.println("❌ Bot token is not configured. Please set bot.token in config.properties");
                return false;
            }
            
            if (botUsername == null || botUsername.equals("YOUR_BOT_USERNAME_HERE") || botUsername.trim().isEmpty()) {
                System.err.println("❌ Bot username is not configured. Please set bot.username in config.properties");
                return false;
            }
            
            if (adminChatId == null || adminChatId.equals("YOUR_ADMIN_CHAT_ID_HERE") || adminChatId.trim().isEmpty()) {
                System.out.println("⚠️  Admin chat ID is not configured. Admin features will be disabled.");
                System.out.println("   To enable admin features, set admin.chat.id in config.properties");
            }
            
            System.out.println("✅ Configuration validated successfully");
            System.out.println("   Bot Username: " + botUsername);
            System.out.println("   Admin Chat ID: " + (adminChatId != null && !adminChatId.equals("YOUR_ADMIN_CHAT_ID_HERE") ? adminChatId : "Not configured"));
            
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Error reading configuration: " + e.getMessage());
            return false;
        }
    }
}