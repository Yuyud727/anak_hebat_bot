package com.mycompany.mytelegrambot.service;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.mycompany.mytelegrambot.database.DatabaseManager;
import com.mycompany.mytelegrambot.model.BroadcastMessage;
import com.mycompany.mytelegrambot.model.Member;
import com.mycompany.mytelegrambot.model.Message;


public class TelegramBotService extends TelegramLongPollingBot {
    private String botToken;
    private String botUsername;
    private Long adminChatId;
    private Properties messages;
    
    private DatabaseManager dbManager;
    private Map<Long, String> userStates = new HashMap<>();
    private Map<Long, Member> pendingRegistrations = new HashMap<>();
    
    public TelegramBotService() {
        loadConfig();
        this.dbManager = DatabaseManager.getInstance();
    }
    
    private void loadConfig() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            
            this.botToken = prop.getProperty("bot.token");
            this.botUsername = prop.getProperty("bot.username");
            this.adminChatId = Long.parseLong(prop.getProperty("admin.chat.id", "0"));
            
            this.messages = prop;
        } catch (Exception e) {
            System.err.println("Could not load config: " + e.getMessage());
        }
    }
    
    @Override
    public String getBotUsername() {
        return botUsername;
    }
    
    @Override
    public String getBotToken() {
        return botToken;
    }
    
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            String firstName = update.getMessage().getFrom().getFirstName();

            // ✅ Simpan pesan masuk (incoming)
            Message incoming = new Message();
            incoming.setChatId(chatId);
            incoming.setDirection("incoming");
            incoming.setMessage(messageText);
            incoming.setTimestamp(LocalDateTime.now());
            DatabaseManager.getInstance().saveMessage(incoming);

            // Log aktivitas pengguna
            logUserActivity(chatId, messageText);

            // Handle admin commands
            if (chatId.equals(adminChatId)) {
                handleAdminCommands(chatId, messageText); // Fixed: removed firstName parameter
                return;
            }

            // Handle user states (registration process)
            if (userStates.containsKey(chatId)) {
                handleUserState(chatId, messageText, firstName);
                return;
            }

            // Handle regular commands
            handleUserCommands(chatId, messageText, firstName);
        }
    }

    
    private void logUserActivity(Long chatId, String message) {
        Member member = dbManager.getMember(chatId);
        if (member != null) {
            member.setLastActivity(LocalDateTime.now() + ": " + message);
            dbManager.saveMember(member);
        }
    }
    
    private void handleUserState(Long chatId, String messageText, String firstName) {
        String state = userStates.get(chatId);
        
        switch (state) {
            case "AWAITING_NAME":
                Member newMember = new Member();
                newMember.setChatId(chatId);
                newMember.setName(messageText);
                pendingRegistrations.put(chatId, newMember);
                userStates.put(chatId, "AWAITING_PHONE");
                sendMessage(chatId, messages.getProperty("bot.phone.prompt"));
                break;
                
            case "AWAITING_PHONE":
                Member member = pendingRegistrations.get(chatId);
                if (member != null) {
                    member.setPhoneNumber(messageText);
                    dbManager.saveMember(member);
                    
                    userStates.remove(chatId);
                    pendingRegistrations.remove(chatId);
                    
                    String successMessage = messages.getProperty("bot.registration.success")
                        .replace("{name}", member.getName());
                    sendMessage(chatId, successMessage);
                    sendMainMenu(chatId);
                }
                break;
        }
    }
    
    private void handleUserCommands(Long chatId, String messageText, String firstName) {
        Member member = dbManager.getMember(chatId);
        String replyText;

        switch (messageText.toLowerCase()) {
            case "/start":
                if (member == null) {
                    replyText = messages.getProperty("bot.welcome.message");
                    sendAndLog(chatId, replyText);
                } else {
                    replyText = "Selamat datang kembali, " + member.getName() + "! 👋";
                    sendAndLog(chatId, replyText);
                    sendMainMenu(chatId);
                }
                break;

            case "/register":
                if (member != null) {
                    replyText = "Anda sudah terdaftar sebagai member! 😊";
                    sendAndLog(chatId, replyText);
                    sendMainMenu(chatId);
                } else {
                    userStates.put(chatId, "AWAITING_NAME");
                    replyText = messages.getProperty("bot.registration.prompt");
                    sendAndLog(chatId, replyText);
                }
                break;

            case "/menu":
                if (member == null) {
                    replyText = messages.getProperty("bot.not.registered");
                    sendAndLog(chatId, replyText);
                } else {
                    sendMainMenu(chatId);
                }
                break;

            case "/nutrisi":
                if (member == null) {
                    replyText = messages.getProperty("bot.not.registered");
                    sendAndLog(chatId, replyText);
                } else {
                    sendNutrisiMenu(chatId);
                }
                break;

            case "/gym":
                if (member == null) {
                    replyText = messages.getProperty("bot.not.registered");
                    sendAndLog(chatId, replyText);
                } else {
                    sendGymMenu(chatId);
                }
                break;

            case "/berita":
                if (member == null) {
                    replyText = messages.getProperty("bot.not.registered");
                    sendAndLog(chatId, replyText);
                } else {
                    sendBeritaMenu(chatId);
                }
                break;

            case "/broadcast":
                if (member == null) {
                    replyText = messages.getProperty("bot.not.registered");
                    sendAndLog(chatId, replyText);
                } else {
                    sendRecentBroadcasts(chatId);
                }
                break;

            default:
                if (member != null) {
                    String response = dbManager.getKeywordResponse(messageText);
                    if (response != null) {
                        sendAndLog(chatId, response);
                    } else {
                        replyText = "Maaf, saya tidak mengerti pesan Anda. Ketik /menu untuk melihat pilihan yang tersedia.";
                        sendAndLog(chatId, replyText);
                    }
                } else {
                    replyText = messages.getProperty("bot.not.registered");
                    sendAndLog(chatId, replyText);
                }
                break;
        }
    }

    
    private void handleAdminCommands(Long chatId, String messageText) {
        String[] parts = messageText.split(" ", 2);
        String command = parts[0].toLowerCase();
        
        switch (command) {
            case "/admin":
                sendAdminMenu(chatId);
                break;
                
            case "/members":
                sendMembersList(chatId);
                break;
                
            case "/broadcast":
                if (parts.length > 1) {
                    String[] broadcastParts = parts[1].split("\\|", 3);
                    if (broadcastParts.length == 3) {
                        String category = broadcastParts[0].trim();
                        String title = broadcastParts[1].trim();
                        String content = broadcastParts[2].trim();
                        
                        // Validasi kategori
                        if (!category.equals("gym") && !category.equals("nutrisi") && !category.equals("berita")) {
                            sendMessage(chatId, "❌ Kategori tidak valid!\n\n" +
                                "Kategori yang tersedia:\n" +
                                "• gym\n" +
                                "• nutrisi\n" +
                                "• berita\n\n" +
                                "Format penggunaan:\n" +
                                "/broadcast kategori|judul|isi pesan\n\n" +
                                "Pilih kategori     : gym/nutrisi/berita\n" +
                                "Judul             : [masukkan judul]\n" +
                                "Isi pesan         : [masukkan isi pesan]\n\n" +
                                "Contoh:\n" +
                                "/broadcast nutrisi|Tips Diet Sehat|Minum air putih minimal 8 gelas sehari untuk menjaga metabolisme tubuh");
                            break;
                        }
                        
                        BroadcastMessage broadcast = new BroadcastMessage(title, content, category);
                        sendBroadcastToAllMembers(broadcast);
                        
                        sendMessage(chatId, "✅ Broadcast berhasil dikirim!");
                    } else {
                        sendMessage(chatId, "❌ Format tidak lengkap!\n\n" +
                            "Kategori yang tersedia:\n" +
                            "• gym\n" +
                            "• nutrisi\n" +
                            "• berita\n\n" +
                            "Format penggunaan:\n" +
                            "/broadcast kategori|judul|isi pesan\n\n" +
                            "Pilih kategori     : gym/nutrisi/berita\n" +
                            "Judul              : [masukkan judul]\n" +
                            "Isi pesan          : [masukkan isi pesan]\n\n" +
                            "Contoh:\n" +
                            "/broadcast nutrisi|Tips Diet Sehat|Minum air putih minimal 8 gelas sehari untuk menjaga metabolisme tubuh");
                    }
                } else {
                    sendMessage(chatId, "Kategori yang tersedia:\n" +
                        "• gym\n" +
                        "• nutrisi\n" +
                        "• berita\n\n" +
                        "Format penggunaan:\n" +
                        "/broadcast kategori  | judul  |  isi pesan\n\n" +
                        "Pilih kategori     : gym/nutrisi/berita\n" +
                        "Judul              : [masukkan judul]\n" +
                        "Isi pesan          : [masukkan isi pesan]\n\n" +
                        "Contoh:\n" +
                        "/broadcast nutrisi|Tips Diet Sehat|Minum air putih minimal 8 gelas sehari untuk menjaga metabolisme tubuh");
                }
                break;
                
            case "/addmember":
            
                if (parts.length > 1) {
                    String[] memberParts = parts[1].split("\\|", 3);
                    if (memberParts.length == 3) {
                        try {
                            String chatIdStr = memberParts[0].trim();
                            String nama = memberParts[1].trim();
                            String telepon = memberParts[2].trim();
                            
                            // Validasi input
                            if (chatIdStr.isEmpty() || nama.isEmpty() || telepon.isEmpty()) {
                                sendMessage(chatId, "❌ Semua field harus diisi!\n\n" +
                                    "Format: /addmember chatid|nama|telepon\n\n" +
                                    "Chat ID           : [ID Telegram user]\n" +
                                    "Nama              : [Nama lengkap]\n" +
                                    "Telepon           : [Nomor telepon]\n\n" +
                                    "Contoh:\n" +
                                    "/addmember 123456789|John Doe|081234567890");
                                break;
                            }
                            
                            // Parse chat ID
                            Long memberChatId = Long.parseLong(chatIdStr);
                            
                            // Validasi nomor telepon
                            if (!isValidPhoneNumber(telepon)) {
                                sendMessage(chatId, "❌ Format nomor telepon tidak valid!\n" +
                                    "Gunakan format: 08xxxxxxxxxx atau +62xxxxxxxxxx");
                                break;
                            }
                            
                            // Cek apakah member sudah terdaftar
                            Member existingMember = dbManager.getMember(memberChatId);
                            if (existingMember != null) {
                                sendMessage(chatId, "❌ Member dengan Chat ID " + memberChatId + " sudah terdaftar!\n\n" +
                                    "📋 Data member yang sudah ada:\n" +
                                    "Nama: " + existingMember.getName() + "\n" +
                                    "Telepon: " + existingMember.getPhoneNumber() + "\n" +
                                    "Terdaftar: " + existingMember.getRegistrationDate().toLocalDate());
                                break;
                            }
                            
                            // Buat member baru
                            Member newMember = new Member(memberChatId, nama, telepon);
                            
                            // Simpan ke database menggunakan method yang sudah ada
                            dbManager.saveMember(newMember);
                            
                            // Kirim pesan selamat datang ke member baru
                            sendWelcomeMessage(memberChatId, nama);
                            
                            // Konfirmasi ke admin
                            sendMessage(chatId, "✅ Member berhasil ditambahkan!\n\n" +
                                "📋 Detail Member:\n" +
                                "Chat ID: " + memberChatId + "\n" +
                                "Nama: " + nama + "\n" +
                                "Telepon: " + telepon + "\n" +
                                "Status: Aktif\n\n" +
                                "Pesan selamat datang telah dikirim ke member.");
                            
                        } catch (NumberFormatException e) {
                            sendMessage(chatId, "❌ Chat ID harus berupa angka!\n\n" +
                                "Contoh Chat ID yang valid: 123456789");
                        } catch (Exception e) {
                            sendMessage(chatId, "❌ Terjadi kesalahan saat menambahkan member: " + e.getMessage());
                            System.err.println("Error adding member: " + e.getMessage());
                        }
                    } else {
                        sendMessage(chatId, "❌ Format tidak lengkap!\n\n" +
                            "Format: /addmember chatid|nama|telepon\n\n" +
                            "Chat ID           : [ID Telegram user]\n" +
                            "Nama              : [Nama lengkap]\n" +
                            "Telepon           : [Nomor telepon]\n\n" +
                            "Contoh:\n" +
                            "/addmember 123456789|John Doe|081234567890\n\n" +
                            "💡 Tips mendapatkan Chat ID:\n" +
                            "1. Minta user mengirim pesan ke bot\n" +
                            "2. Chat ID akan muncul di log bot\n" +
                            "3. Atau gunakan bot @userinfobot");
                    }
                } else {
                    sendMessage(chatId, "Format: /addmember chatid|nama|telepon\n\n" +
                        "Chat ID           : [ID Telegram user]\n" +
                        "Nama              : [Nama lengkap]\n" +
                        "Telepon           : [Nomor telepon]\n\n" +
                        "Contoh:\n" +
                        "/addmember 123456789|John Doe|081234567890\n\n" +
                        "💡 Tips mendapatkan Chat ID:\n" +
                        "1. Minta user mengirim pesan ke bot\n" +
                        "2. Chat ID akan muncul di log bot\n" +
                        "3. Atau gunakan bot @userinfobot");
                }
                break;
                
                case "/deletemember":
                    if (parts.length > 1) {
                        try {
                            String chatIdStr = parts[1].trim();
                            
                            // Validasi input
                            if (chatIdStr.isEmpty()) {
                                sendMessage(chatId, "❌ Chat ID tidak boleh kosong!\n\n" +
                                    "Format: /deletemember chatid\n\n" +
                                    "Contoh:\n" +
                                    "/deletemember 123456789");
                                break;
                            }
                            
                            // Parse chat ID
                            Long memberChatId = Long.parseLong(chatIdStr);
                            
                            // Cek apakah member ada
                            Member existingMember = dbManager.getMember(memberChatId);
                            if (existingMember == null) {
                                sendMessage(chatId, "❌ Member dengan Chat ID " + memberChatId + " tidak ditemukan!\n\n" +
                                    "💡 Tips:\n" +
                                    "• Pastikan Chat ID benar\n" +
                                    "• Gunakan /members untuk melihat daftar member\n" +
                                    "• Chat ID harus berupa angka");
                                break;
                            }
                            
                            // Simpan data member untuk konfirmasi
                            String memberName = existingMember.getName();
                            String memberPhone = existingMember.getPhoneNumber();
                            
                            // Hapus member dari database
                            boolean deleted = deleteMemberFromDatabase(memberChatId);
                            
                            if (deleted) {
                                // Kirim pesan pemberitahuan ke member yang dihapus
                                sendFarewellMessage(memberChatId, memberName);
                                
                                // Konfirmasi ke admin
                                sendMessage(chatId, "✅ Member berhasil dihapus!\n\n" +
                                    "📋 Detail member yang dihapus:\n" +
                                    "Chat ID: " + memberChatId + "\n" +
                                    "Nama: " + memberName + "\n" +
                                    "Telepon: " + memberPhone + "\n" +
                                    "Tanggal bergabung: " + existingMember.getRegistrationDate().toLocalDate() + "\n\n" +
                                    "Pemberitahuan telah dikirim ke member yang bersangkutan.");
                            } else {
                                sendMessage(chatId, "❌ Gagal menghapus member. Silakan coba lagi.");
                            }
                            
                        } catch (NumberFormatException e) {
                            sendMessage(chatId, "❌ Chat ID harus berupa angka!\n\n" +
                                "Format: /deletemember chatid\n\n" +
                                "Contoh Chat ID yang valid: 123456789");
                        } catch (Exception e) {
                            sendMessage(chatId, "❌ Terjadi kesalahan saat menghapus member: " + e.getMessage());
                            System.err.println("Error deleting member: " + e.getMessage());
                        }
                    } else {
                        sendMessage(chatId, "Format: /deletemember chatid\n\n" +
                            "Chat ID           : [ID Telegram user yang akan dihapus]\n\n" +
                            "Contoh:\n" +
                            "/deletemember 123456789\n\n" +
                            "💡 Tips:\n" +
                            "• Gunakan /members untuk melihat daftar member\n" +
                            "• Chat ID bisa dilihat di daftar member\n" +
                            "• Pastikan Chat ID benar sebelum menghapus");
                    }
                    break;
            
                case "/stats":
                sendBotStats(chatId);
                break;

                case "/messages":
                case "/incoming":
                case "/outgoing":
                case "/searchmessage":
                case "/messagesbymember":
                case "/clearmessages":
                case "/exportmessages":
                case "/messagestats":
                handleMessageManagementCommands(chatId, messageText);
                break;
                
            default:
                sendAdminMenu(chatId);
                break;  
        }
    }
/**
 * Handle admin commands untuk message management
 */
private void handleMessageManagementCommands(Long chatId, String messageText) {
    String[] parts = messageText.split(" ", 3);
    String command = parts[0].toLowerCase();
    
    switch (command) {
        case "/messages":
            sendMessageHistory(chatId, parts.length > 1 ? parts[1] : "all");
            break;
            
        case "/incoming":
            sendIncomingMessages(chatId, parts.length > 1 ? Integer.parseInt(parts[1]) : 10);
            break;
            
        case "/outgoing":
            sendOutgoingMessages(chatId, parts.length > 1 ? Integer.parseInt(parts[1]) : 10);
            break;
            
        case "/searchmessage":
            if (parts.length > 1) {
                searchMessages(chatId, parts[1]);
            } else {
                sendMessage(chatId, "Format: /searchmessage kata_kunci");
            }
            break;
            
        case "/messagesbymember":
            if (parts.length > 1) {
                Long memberChatId = Long.parseLong(parts[1]);
                sendMessagesByMember(chatId, memberChatId);
            } else {
                sendMessage(chatId, "Format: /messagesbymember chat_id");
            }
            break;
            
        case "/clearmessages":
            if (parts.length > 1) {
                clearMessageHistory(chatId, parts[1]);
            } else {
                sendMessage(chatId, "Format: /clearmessages [all|older_than_days]");
            }
            break;
            
        case "/exportmessages":
            exportMessages(chatId, parts.length > 1 ? parts[1] : "all");
            break;
            
        case "/messagestats":
            sendMessageStatistics(chatId);
            break;
    }
}
    
    private void sendMainMenu(Long chatId) {
        sendMessage(chatId, messages.getProperty("menu.main"));
    }
    
    private void sendNutrisiMenu(Long chatId) {
        String nutrisiMenu = """
            🥗 MENU NUTRISI & DIET
            
            Ketik kata kunci berikut untuk informasi:
            • protein - Info tentang protein
            • kalori - Info tentang kalori
            • diet - Tips diet seimbang
            • karbohidrat - Info karbohidrat
            • lemak - Info lemak sehat
            • vitamin - Info vitamin penting
            
            Atau ketik /menu untuk kembali ke menu utama
            """;
        sendMessage(chatId, nutrisiMenu);
    }
    
    private void sendGymMenu(Long chatId) {
        String gymMenu = """
            🏋️ MENU GYM & LATIHAN
            
            Ketik kata kunci berikut untuk panduan:
            • squat - Teknik squat yang benar
            • push up - Teknik push up
            • pull up - Teknik pull up
            • deadlift - Teknik deadlift
            • bench press - Teknik bench press
            • cardio - Info latihan kardio
            
            Atau ketik /menu untuk kembali ke menu utama
            """;
        sendMessage(chatId, gymMenu);
    }
    
    private void sendBeritaMenu(Long chatId) {
        String beritaMenu = """
            📰 BERITA KESEHATAN & FITNESS
            
            Ketik kata kunci berikut untuk info terbaru:
            • trending - Tren fitness terbaru
            • research - Penelitian kesehatan
            • tips - Tips kesehatan harian
            • workout - Program workout terbaru
            
            Atau ketik /broadcast untuk melihat pengumuman terbaru
            Atau ketik /menu untuk kembali ke menu utama
            """;
        sendMessage(chatId, beritaMenu);
    }
    
    private void sendRecentBroadcasts(Long chatId) {
        List<BroadcastMessage> broadcasts = dbManager.getRecentBroadcasts(5);
        
        if (broadcasts.isEmpty()) {
            sendMessage(chatId, "📢 Belum ada broadcast terbaru.");
            return;
        }
        
        StringBuilder sb = new StringBuilder("📢 BROADCAST TERBARU:\n\n");
        for (int i = 0; i < broadcasts.size(); i++) {
            BroadcastMessage msg = broadcasts.get(i);
            sb.append(String.format("%d. [%s] %s\n%s\n\n", 
                i + 1, msg.getCategory().toUpperCase(), msg.getTitle(), msg.getContent()));
        }
        
        sendMessage(chatId, sb.toString());
    }
    
    private void sendAdminMenu(Long chatId) {
        String adminMenu = """
            👑 ADMIN PANEL
            
            📊 MEMBER MANAGEMENT:
            • /members - Lihat daftar member
            • /addmember chatid|nama|telepon - Tambah member manual
            • /deletemember chatid - Hapus member
            • /stats - Statistik bot
            
            📨 MESSAGE MANAGEMENT:
            • /messages [all|incoming|outgoing] - History pesan
            • /incoming [jumlah] - Pesan masuk terbaru
            • /outgoing [jumlah] - Pesan keluar terbaru
            • /searchmessage kata_kunci - Cari pesan
            • /messagesbymember chat_id - Pesan dari member
            • /clearmessages [all|incoming|outgoing|hari] - Hapus pesan
            • /exportmessages [all|incoming|outgoing] - Export pesan
            • /messagestats - Statistik pesan
            
            📢 BROADCAST:
            • /broadcast kategori|judul|isi - Kirim broadcast
            
            Contoh penggunaan:
            /messages incoming
            /searchmessage protein
            /clearmessages 30
            """;
        sendMessage(chatId, adminMenu);
    }
    
    private void sendMembersList(Long chatId) {
        List<Member> members = dbManager.getAllMembers();
        
        if (members.isEmpty()) {
            sendMessage(chatId, "Belum ada member yang terdaftar.");
            return;
        }
        
        StringBuilder sb = new StringBuilder("👥 DAFTAR MEMBER (" + members.size() + "):\n\n");
        for (int i = 0; i < members.size(); i++) {
            Member member = members.get(i);
            sb.append(String.format("%d. %s\n   ID: %d\n   Telepon: %s\n   Terdaftar: %s\n\n", 
                i + 1, member.getName(), member.getChatId(), 
                member.getPhoneNumber(), member.getRegistrationDate().toLocalDate()));
        }
        
        sendMessage(chatId, sb.toString());
    }
    
    private void sendBotStats(Long chatId) {
        List<Member> allMembers = dbManager.getAllMembers();
        List<BroadcastMessage> broadcasts = dbManager.getRecentBroadcasts(100);
        
        String stats = String.format("""
            📊 STATISTIK BOT
            
            👥 Total Member: %d
            📢 Total Broadcast: %d
            🕐 Bot aktif sejak startup
            
            📈 Aktivitas:
            • Member aktif: %d
            • Broadcast terkirim: %d
            """, 
            allMembers.size(),
            broadcasts.size(),
            (int) allMembers.stream().filter(Member::isActive).count(),
            (int) broadcasts.stream().filter(BroadcastMessage::isSent).count()
        );
        
        sendMessage(chatId, stats);
    }
    
    private void sendBroadcastToAllMembers(BroadcastMessage broadcast) {
        List<Member> members = dbManager.getAllMembers();
        int sentCount = 0;
        
        String message = String.format("📢 %s\n\n%s\n\n📅 %s", 
            broadcast.getTitle(), broadcast.getContent(), 
            broadcast.getCreatedDate().toLocalDate());
        
        for (Member member : members) {
            try {
                sendMessage(member.getChatId(), message);
                sentCount++;
                Thread.sleep(50); // Avoid rate limiting
            } catch (Exception e) {
                System.err.println("Failed to send broadcast to " + member.getChatId() + ": " + e.getMessage());
            }
        }
        
        broadcast.setSent(true);
        broadcast.setRecipientCount(sentCount);
        dbManager.saveBroadcastMessage(broadcast);
        
        // Notify admin
        if (adminChatId != null && adminChatId != 0) {
            sendMessage(adminChatId, String.format("✅ Broadcast '%s' berhasil dikirim ke %d member", 
                broadcast.getTitle(), sentCount));
        }
    }
    
    private void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        
        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
    }
    
    private void sendMessageWithKeyboard(Long chatId, String text, ReplyKeyboardMarkup keyboard) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        message.setReplyMarkup(keyboard);
        
        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.err.println("Error sending message with keyboard: " + e.getMessage());
        }
    }
    
    private ReplyKeyboardMarkup createMainMenuKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        
        KeyboardRow row1 = new KeyboardRow();
        row1.add("🥗 Nutrisi");
        row1.add("🏋️ Gym");
        
        KeyboardRow row2 = new KeyboardRow();
        row2.add("📰 Berita");
        row2.add("📢 Broadcast");
        
        KeyboardRow row3 = new KeyboardRow();
        row3.add("🏠 Menu Utama");
        
        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        
        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);
        
        return keyboardMarkup;
    }

    /**
     * Validasi format nomor telepon Indonesia
     */
    private boolean isValidPhoneNumber(String phoneNumber) {
        // Regex untuk validasi nomor telepon Indonesia
        String phoneRegex = "^(\\+62|62|0)[0-9]{9,13}$";
        return phoneNumber.matches(phoneRegex);
    }

    /**
     * Kirim pesan selamat datang ke member yang baru ditambahkan
     */
    private void sendWelcomeMessage(Long chatId, String nama) {
        String welcomeMessage = "🎉 Selamat datang, " + nama + "!\n\n" +
            "Anda telah berhasil ditambahkan sebagai member oleh admin.\n\n" +
            "🤖 Perintah yang tersedia:\n" +
            "• /menu - Menu utama\n" +
            "• /nutrisi - Info nutrisi & diet\n" +
            "• /gym - Info gym & latihan\n" +
            "• /berita - Berita kesehatan\n" +
            "• /broadcast - Lihat pengumuman terbaru\n\n" +
            "Jika ada pertanyaan, silakan hubungi admin.\n" +
            "Selamat bergabung! 💪";
        
        try {
            sendMessage(chatId, welcomeMessage);
        } catch (Exception e) {
            // Jika gagal mengirim pesan, log error tapi jangan gagalkan proses
            System.err.println("Gagal mengirim pesan selamat datang ke: " + chatId + " - " + e.getMessage());
        }
    }

    /**
    * Hapus member dari database berdasarkan Chat ID
    */
    private boolean deleteMemberFromDatabase(Long chatId) {
        return dbManager.deleteMember(chatId);
    }

    /**
     * Kirim pesan perpisahan ke member yang dihapus
     */
    private void sendFarewellMessage(Long chatId, String nama) {
        String farewellMessage = "👋 Selamat tinggal, " + nama + "\n\n" +
            "Akun Anda telah dihapus dari sistem oleh admin.\n" +
            "Terima kasih telah menjadi bagian dari komunitas kami.\n\n" +
            "Jika Anda ingin bergabung kembali di masa depan, " +
            "silakan hubungi admin atau ketik /register untuk mendaftar ulang.\n\n" +
            "Semoga sukses selalu! 🙏";
        
        try {
            sendMessage(chatId, farewellMessage);
        } catch (Exception e) {
            // Jika gagal mengirim pesan, log error tapi jangan gagalkan proses
            System.err.println("Gagal mengirim pesan perpisahan ke: " + chatId + " - " + e.getMessage());
        }
    }
    private void sendAndLog(Long chatId, String text) {
    SendMessage message = new SendMessage();
    message.setChatId(chatId.toString());
    message.setText(text);

        try {
            execute(message);

            // Simpan ke database sebagai pesan keluar
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
    /**
 * Kirim history pesan (incoming/outgoing/all)
 */
private void sendMessageHistory(Long chatId, String type) {
    try {
        List<Message> messages = new ArrayList<>();
        
        switch (type.toLowerCase()) {
            case "incoming":
                messages = dbManager.getMessagesByDirection("incoming", 20);
                break;
            case "outgoing":
                messages = dbManager.getMessagesByDirection("outgoing", 20);
                break;
            case "all":
            default:
                messages = dbManager.getAllMessages(20);
                break;
        }
        
        if (messages.isEmpty()) {
            sendMessage(chatId, "📭 Tidak ada pesan " + type + " yang ditemukan.");
            return;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("📨 HISTORY PESAN ").append(type.toUpperCase()).append("\n");
        sb.append("Total: ").append(messages.size()).append(" pesan\n\n");
        
        for (int i = 0; i < Math.min(messages.size(), 20); i++) {
            Message msg = messages.get(i);
            Member member = dbManager.getMember(msg.getChatId());
            String memberName = (member != null) ? member.getName() : "Unknown";
            
            sb.append(String.format("📍 %d. %s %s\n", 
                i + 1, 
                msg.getDirection().equals("incoming") ? "📥" : "📤",
                msg.getDirection().toUpperCase()));
            sb.append(String.format("👤 Member: %s (ID: %d)\n", memberName, msg.getChatId()));
            sb.append(String.format("🕒 Waktu: %s\n", msg.getTimestamp().toString()));
            sb.append(String.format("💬 Pesan: %s\n\n", 
                msg.getMessage().length() > 100 ? 
                msg.getMessage().substring(0, 100) + "..." : 
                msg.getMessage()));
        }
        
        sendMessage(chatId, sb.toString());
        
    } catch (Exception e) {
        sendMessage(chatId, "❌ Error mengambil history pesan: " + e.getMessage());
        System.err.println("Error getting message history: " + e.getMessage());
    }
}

/**
 * Kirim pesan masuk terbaru
 */
private void sendIncomingMessages(Long chatId, int limit) {
    try {
        List<Message> incomingMessages = dbManager.getMessagesByDirection("incoming", limit);
        
        if (incomingMessages.isEmpty()) {
            sendMessage(chatId, "📭 Tidak ada pesan masuk.");
            return;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("📥 PESAN MASUK TERBARU\n");
        sb.append("Menampilkan ").append(Math.min(limit, incomingMessages.size())).append(" pesan terakhir\n\n");
        
        for (int i = 0; i < Math.min(incomingMessages.size(), limit); i++) {
            Message msg = incomingMessages.get(i);
            Member member = dbManager.getMember(msg.getChatId());
            String memberName = (member != null) ? member.getName() : "Unknown User";
            
            sb.append(String.format("📨 %d. Dari: %s\n", i + 1, memberName));
            sb.append(String.format("🆔 Chat ID: %d\n", msg.getChatId()));
            sb.append(String.format("🕒 %s\n", msg.getTimestamp().toString()));
            sb.append(String.format("💬 \"%s\"\n\n", msg.getMessage()));
        }
        
        sendMessage(chatId, sb.toString());
        
    } catch (Exception e) {
        sendMessage(chatId, "❌ Error mengambil pesan masuk: " + e.getMessage());
    }
}

/**
 * Kirim pesan keluar terbaru
 */
    private void sendOutgoingMessages(Long chatId, int limit) {
        try {
            List<Message> outgoingMessages = dbManager.getMessagesByDirection("outgoing", limit);
            
            if (outgoingMessages.isEmpty()) {
                sendMessage(chatId, "📭 Tidak ada pesan keluar.");
                return;
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append("📤 PESAN KELUAR TERBARU\n");
            sb.append("Menampilkan ").append(Math.min(limit, outgoingMessages.size())).append(" pesan terakhir\n\n");
            
            for (int i = 0; i < Math.min(outgoingMessages.size(), limit); i++) {
                Message msg = outgoingMessages.get(i);
                Member member = dbManager.getMember(msg.getChatId());
                String memberName = (member != null) ? member.getName() : "Unknown User";
                
                sb.append(String.format("📨 %d. Ke: %s\n", i + 1, memberName));
                sb.append(String.format("🆔 Chat ID: %d\n", msg.getChatId()));
                sb.append(String.format("🕒 %s\n", msg.getTimestamp().toString()));
                sb.append(String.format("💬 \"%s\"\n\n", msg.getMessage()));
            }
            
            sendMessage(chatId, sb.toString());
            
        } catch (Exception e) {
            sendMessage(chatId, "❌ Error mengambil pesan keluar: " + e.getMessage());
        }
}
/**
 * Cari pesan berdasarkan kata kunci
 */
private void searchMessages(Long chatId, String keyword) {
    try {
        List<Message> foundMessages = dbManager.searchMessages(keyword, 15);
        
        if (foundMessages.isEmpty()) {
            sendMessage(chatId, "🔍 Tidak ditemukan pesan dengan kata kunci: \"" + keyword + "\"");
            return;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("🔍 HASIL PENCARIAN: \"").append(keyword).append("\"\n");
        sb.append("Ditemukan ").append(foundMessages.size()).append(" pesan\n\n");
        
        for (int i = 0; i < Math.min(foundMessages.size(), 15); i++) {
            Message msg = foundMessages.get(i);
            Member member = dbManager.getMember(msg.getChatId());
            String memberName = (member != null) ? member.getName() : "Unknown";
            String direction = msg.getDirection().equals("incoming") ? "📥 Masuk" : "📤 Keluar";
            
            sb.append(String.format("📍 %d. %s dari/ke %s\n", i + 1, direction, memberName));
            sb.append(String.format("🕒 %s\n", msg.getTimestamp().toString()));
            sb.append(String.format("💬 %s\n\n", msg.getMessage()));
        }
        
        sendMessage(chatId, sb.toString());
        
    } catch (Exception e) {
        sendMessage(chatId, "❌ Error mencari pesan: " + e.getMessage());
    }
}

/**
 * Kirim semua pesan dari member tertentu
 */
private void sendMessagesByMember(Long chatId, Long memberChatId) {
    try {
        Member member = dbManager.getMember(memberChatId);
        if (member == null) {
            sendMessage(chatId, "❌ Member dengan Chat ID " + memberChatId + " tidak ditemukan.");
            return;
        }
        
        List<Message> memberMessages = dbManager.getMessagesByChatId(memberChatId, 20);
        
        if (memberMessages.isEmpty()) {
            sendMessage(chatId, "📭 Tidak ada pesan dari member: " + member.getName());
            return;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("👤 PESAN DARI: ").append(member.getName()).append("\n");
        sb.append("📱 Telepon: ").append(member.getPhoneNumber()).append("\n");
        sb.append("🆔 Chat ID: ").append(memberChatId).append("\n");
        sb.append("📊 Total pesan: ").append(memberMessages.size()).append("\n\n");
        
        for (int i = 0; i < Math.min(memberMessages.size(), 20); i++) {
            Message msg = memberMessages.get(i);
            String direction = msg.getDirection().equals("incoming") ? "📥" : "📤";
            
            sb.append(String.format("%s %d. %s\n", direction, i + 1, msg.getDirection().toUpperCase()));
            sb.append(String.format("🕒 %s\n", msg.getTimestamp().toString()));
            sb.append(String.format("💬 %s\n\n", msg.getMessage()));
        }
        
        sendMessage(chatId, sb.toString());
        
    } catch (Exception e) {
        sendMessage(chatId, "❌ Error mengambil pesan member: " + e.getMessage());
    }
}
/**
 * Hapus history pesan
 */
private void clearMessageHistory(Long chatId, String type) {
    try {
        int deletedCount = 0;
        
        switch (type.toLowerCase()) {
            case "all":
                deletedCount = dbManager.deleteAllMessages();
                sendMessage(chatId, "🗑️ Berhasil menghapus " + deletedCount + " pesan dari database.");
                break;
                
            case "incoming":
                deletedCount = dbManager.deleteMessagesByDirection("incoming");
                sendMessage(chatId, "🗑️ Berhasil menghapus " + deletedCount + " pesan masuk.");
                break;
                
            case "outgoing":
                deletedCount = dbManager.deleteMessagesByDirection("outgoing");
                sendMessage(chatId, "🗑️ Berhasil menghapus " + deletedCount + " pesan keluar.");
                break;
                
            default:
                // Hapus pesan lebih lama dari X hari
                try {
                    int days = Integer.parseInt(type);
                    deletedCount = dbManager.deleteOldMessages(days);
                    sendMessage(chatId, "🗑️ Berhasil menghapus " + deletedCount + " pesan yang lebih lama dari " + days + " hari.");
                } catch (NumberFormatException e) {
                    sendMessage(chatId, "❌ Format tidak valid. Gunakan: all, incoming, outgoing, atau angka (hari)");
                }
                break;
        }
        
    } catch (Exception e) {
        sendMessage(chatId, "❌ Error menghapus pesan: " + e.getMessage());
    }
}
/**
 * Export pesan ke format teks
 */
private void exportMessages(Long chatId, String type) {
    try {
        List<Message> messages = new ArrayList<>();
        
        switch (type.toLowerCase()) {
            case "incoming":
                messages = dbManager.getMessagesByDirection("incoming", 1000);
                break;
            case "outgoing":
                messages = dbManager.getMessagesByDirection("outgoing", 1000);
                break;
            case "all":
            default:
                messages = dbManager.getAllMessages(1000);
                break;
        }
        
        if (messages.isEmpty()) {
            sendMessage(chatId, "📭 Tidak ada pesan untuk diekspor.");
            return;
        }
        
        StringBuilder export = new StringBuilder();
        export.append("=== EXPORT PESAN TELEGRAM BOT ===\n");
        export.append("Tanggal Export: ").append(LocalDateTime.now().toString()).append("\n");
        export.append("Type: ").append(type.toUpperCase()).append("\n");
        export.append("Total Pesan: ").append(messages.size()).append("\n");
        export.append("=========================================\n\n");
        
        for (Message msg : messages) {
            Member member = dbManager.getMember(msg.getChatId());
            String memberName = (member != null) ? member.getName() : "Unknown";
            
            export.append("---\n");
            export.append("Direction: ").append(msg.getDirection().toUpperCase()).append("\n");
            export.append("Member: ").append(memberName).append(" (").append(msg.getChatId()).append(")\n");
            export.append("Timestamp: ").append(msg.getTimestamp().toString()).append("\n");
            export.append("Message: ").append(msg.getMessage()).append("\n");
            export.append("---\n\n");
        }
        
        // Karena Telegram ada limit panjang pesan, kita bagi menjadi beberapa bagian
        String exportText = export.toString();
        int maxLength = 4000; // Batas aman untuk Telegram
        
        if (exportText.length() <= maxLength) {
            sendMessage(chatId, exportText);
        } else {
            // Bagi menjadi beberapa pesan
            int parts = (int) Math.ceil((double) exportText.length() / maxLength);
            sendMessage(chatId, "📄 Export terlalu panjang, akan dikirim dalam " + parts + " bagian:");
            
            for (int i = 0; i < parts; i++) {
                int start = i * maxLength;
                int end = Math.min(start + maxLength, exportText.length());
                String part = exportText.substring(start, end);
                
                sendMessage(chatId, "📄 Bagian " + (i + 1) + "/" + parts + ":\n\n" + part);
                Thread.sleep(500); // Hindari rate limiting
            }
        }
        
    } catch (Exception e) {
        sendMessage(chatId, "❌ Error mengekspor pesan: " + e.getMessage());
    }
}

/**
 * Kirim statistik pesan
 */
private void sendMessageStatistics(Long chatId) {
    try {
        int totalMessages = dbManager.getTotalMessageCount();
        int incomingCount = dbManager.getMessageCountByDirection("incoming");
        int outgoingCount = dbManager.getMessageCountByDirection("outgoing");
        int todayMessages = dbManager.getTodayMessageCount();
        int activeMembers = dbManager.getActiveMembersCount();
        
        // Statistik per member
        List<Member> topActiveMembers = dbManager.getTopActiveMembers(5);
        
        StringBuilder stats = new StringBuilder();
        stats.append("📊 STATISTIK PESAN BOT\n\n");
        stats.append("📈 Total Pesan: ").append(totalMessages).append("\n");
        stats.append("📥 Pesan Masuk: ").append(incomingCount).append("\n");
        stats.append("📤 Pesan Keluar: ").append(outgoingCount).append("\n");
        stats.append("📅 Pesan Hari Ini: ").append(todayMessages).append("\n");
        stats.append("👥 Member Aktif: ").append(activeMembers).append("\n\n");
        
        if (!topActiveMembers.isEmpty()) {
            stats.append("🏆 TOP 5 MEMBER TERAKTIF:\n");
            for (int i = 0; i < topActiveMembers.size(); i++) {
                Member member = topActiveMembers.get(i);
                int memberMessageCount = dbManager.getMessageCountByChatId(member.getChatId());
                stats.append(String.format("%d. %s - %d pesan\n", 
                    i + 1, member.getName(), memberMessageCount));
            }
            stats.append("\n");
        }
        
        // Statistik waktu
        Map<String, Integer> hourlyStats = dbManager.getHourlyMessageStats();
        if (!hourlyStats.isEmpty()) {
            stats.append("🕐 AKTIVITAS PER JAM (24 jam terakhir):\n");
            hourlyStats.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .forEach(entry -> 
                    stats.append(String.format("• %s:00 - %d pesan\n", entry.getKey(), entry.getValue())));
        }
        
        sendMessage(chatId, stats.toString());
        
    } catch (Exception e) {
        sendMessage(chatId, "❌ Error mengambil statistik: " + e.getMessage());
    }
}

}