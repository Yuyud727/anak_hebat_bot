package com.mycompany.mytelegrambot.database;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.mycompany.mytelegrambot.model.BroadcastMessage;
import com.mycompany.mytelegrambot.model.Keyword;
import com.mycompany.mytelegrambot.model.Member;
import com.mycompany.mytelegrambot.model.Message;


public class DatabaseManager {
    private static DatabaseManager instance;
    private String databasePath;
    private String databaseType;
    private String mysqlUrl;
    private String mysqlUsername;
    private String mysqlPassword;
    
    private DatabaseManager() {
        loadConfig();
        initializeDatabase();
    }
    
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    private void loadConfig() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            
            // Database type selection
            this.databaseType = prop.getProperty("database.type", "sqlite");
            
            // SQLite config (backup)
            this.databasePath = prop.getProperty("database.path", "data.db");
            
            // MySQL config
            this.mysqlUrl = prop.getProperty("mysql.url", "jdbc:mysql://localhost:3306/fitness_bot2?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");
            this.mysqlUsername = prop.getProperty("mysql.username", "root");
            this.mysqlPassword = prop.getProperty("mysql.password", "");
            
            System.out.println("🔧 Database Type: " + this.databaseType.toUpperCase());
            
        } catch (Exception e) {
            // Fallback to SQLite
            this.databaseType = "sqlite";
            this.databasePath = "data.db";
            System.err.println("⚠️ Could not load config, using SQLite: " + e.getMessage());
        }
    }
    
    private Connection getConnection() throws SQLException {
        if ("mysql".equalsIgnoreCase(this.databaseType)) {
            try {
                // Load MySQL driver explicitly
                Class.forName("com.mysql.cj.jdbc.Driver");
                Connection conn = DriverManager.getConnection(mysqlUrl, mysqlUsername, mysqlPassword);
                System.out.println("✅ MySQL Connected: " + mysqlUrl);
                return conn;
            } catch (ClassNotFoundException e) {
                System.err.println("❌ MySQL Driver not found! Falling back to SQLite");
                this.databaseType = "sqlite"; // Switch to SQLite permanently for this session
                return DriverManager.getConnection("jdbc:sqlite:" + databasePath);
            } catch (SQLException e) {
                System.err.println("❌ MySQL Connection failed! Falling back to SQLite: " + e.getMessage());
                this.databaseType = "sqlite"; // Switch to SQLite permanently for this session
                return DriverManager.getConnection("jdbc:sqlite:" + databasePath);
            }
        } else {
            // SQLite connection
            return DriverManager.getConnection("jdbc:sqlite:" + databasePath);
        }
    }
    
    // Helper method for boolean values
    private String getBooleanTrue() {
        return "mysql".equalsIgnoreCase(this.databaseType) ? "TRUE" : "1";
    }
    
    private String getBooleanFalse() {
        return "mysql".equalsIgnoreCase(this.databaseType) ? "FALSE" : "0";
    }
    
    private void initializeDatabase() {
        try (Connection conn = getConnection()) {
            if ("mysql".equalsIgnoreCase(this.databaseType)) {
                initializeMySQLTables(conn);
            } else {
                initializeSQLiteTables(conn);
            }
        } catch (SQLException e) {
            System.err.println("❌ Database initialization error: " + e.getMessage());
        }
    }
    
    private void initializeMySQLTables(Connection conn) throws SQLException {
        // Create Members table
        String createMembersTable = """
            CREATE TABLE IF NOT EXISTS members (
                chat_id BIGINT PRIMARY KEY,
                name VARCHAR(255) NOT NULL,
                phone_number VARCHAR(20),
                registration_date DATETIME NOT NULL,
                is_active BOOLEAN DEFAULT TRUE,
                last_activity TEXT,
                INDEX idx_active (is_active),
                INDEX idx_registration (registration_date)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
        """;
        
        // Create Keywords table
        String createKeywordsTable = """
            CREATE TABLE IF NOT EXISTS keywords (
                id INT AUTO_INCREMENT PRIMARY KEY,
                keyword VARCHAR(255) NOT NULL,
                response TEXT NOT NULL,
                category VARCHAR(100) NOT NULL,
                is_active BOOLEAN DEFAULT TRUE,
                created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
                INDEX idx_keyword (keyword),
                INDEX idx_category (category),
                INDEX idx_active (is_active)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
        """;
        
        // Create Broadcast Messages table
        String createBroadcastTable = """
            CREATE TABLE IF NOT EXISTS broadcast_messages (
                id INT AUTO_INCREMENT PRIMARY KEY,
                title VARCHAR(255) NOT NULL,
                content TEXT NOT NULL,
                category VARCHAR(100) NOT NULL,
                created_date DATETIME NOT NULL,
                is_sent BOOLEAN DEFAULT FALSE,
                recipient_count INT DEFAULT 0,
                INDEX idx_sent (is_sent),
                INDEX idx_category (category),
                INDEX idx_created (created_date)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
        """;

        // Create Messages table
        String createMessagesTable = """
            CREATE TABLE IF NOT EXISTS messages (
                id INT AUTO_INCREMENT PRIMARY KEY,
                chat_id BIGINT NOT NULL,
                direction VARCHAR(10) NOT NULL,
                message TEXT NOT NULL,
                timestamp DATETIME NOT NULL,
                INDEX idx_chat_id (chat_id),
                INDEX idx_timestamp (timestamp)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
        """;
        
        Statement stmt = conn.createStatement();
        stmt.execute(createMembersTable);
        stmt.execute(createKeywordsTable);
        stmt.execute(createBroadcastTable);
        stmt.execute(createMessagesTable);
        
        System.out.println("✅ MySQL tables initialized successfully");
        
        // Insert default keywords if table is empty
        insertDefaultKeywords(conn);
    }
    
    private void initializeSQLiteTables(Connection conn) throws SQLException {
        // Create Members table
        String createMembersTable = """
            CREATE TABLE IF NOT EXISTS members (
                chat_id INTEGER PRIMARY KEY,
                name TEXT NOT NULL,
                phone_number TEXT,
                registration_date TEXT NOT NULL,
                is_active BOOLEAN DEFAULT 1,
                last_activity TEXT
            )
        """;
        
        // Create Keywords table
        String createKeywordsTable = """
            CREATE TABLE IF NOT EXISTS keywords (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                keyword TEXT NOT NULL,
                response TEXT NOT NULL,
                category TEXT NOT NULL,
                is_active BOOLEAN DEFAULT 1,
                created_date TEXT DEFAULT CURRENT_TIMESTAMP
            )
        """;
        
        // Create Broadcast Messages table
        String createBroadcastTable = """
            CREATE TABLE IF NOT EXISTS broadcast_messages (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                content TEXT NOT NULL,
                category TEXT NOT NULL,
                created_date TEXT NOT NULL,
                is_sent BOOLEAN DEFAULT 0,
                recipient_count INTEGER DEFAULT 0
            )
        """;

        // Create Messages table - FIXED: Added missing messages table
        String createMessagesTable = """
            CREATE TABLE IF NOT EXISTS messages (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                chat_id INTEGER NOT NULL,
                direction TEXT NOT NULL,
                message TEXT NOT NULL,
                timestamp TEXT NOT NULL
            )
        """;
        
        Statement stmt = conn.createStatement();
        stmt.execute(createMembersTable);
        stmt.execute(createKeywordsTable);
        stmt.execute(createBroadcastTable);
        stmt.execute(createMessagesTable); // FIXED: Added this line
        
        System.out.println("✅ SQLite tables initialized successfully");
        
        // Insert default keywords if table is empty
        insertDefaultKeywords(conn);
    }
    
    private void insertDefaultKeywords(Connection conn) throws SQLException {
        String checkQuery = "SELECT COUNT(*) FROM keywords";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(checkQuery);
        
        if (rs.next() && rs.getInt(1) == 0) {
            // Insert default keywords with proper SQL for both databases
            String[][] defaultKeywords = {
                {"protein", "🥛 Protein adalah nutrisi penting untuk pembentukan otot. Kebutuhan harian: 1.6-2.2g per kg berat badan untuk atlet.", "nutrisi"},
                {"kalori", "🔥 Kalori adalah satuan energi. Untuk menurunkan berat badan: defisit 300-500 kalori/hari. Untuk menambah massa otot: surplus 200-400 kalori/hari.", "nutrisi"},
                {"squat", "🏋️ Squat melatih otot quadriceps, glutes, dan hamstring.\n\nTeknik:\n1. Kaki selebar bahu\n2. Turunkan badan seperti duduk\n3. Jaga punggung tetap lurus\n4. Naik dengan mendorong tumit", "gym"},
                {"push up", "💪 Push up melatih dada, triceps, dan bahu.\n\nTeknik:\n1. Posisi plank\n2. Tangan selebar bahu\n3. Turunkan dada hingga hampir menyentuh lantai\n4. Dorong naik dengan kuat", "gym"},
                {"diet", "🥗 Diet seimbang mencakup:\n• Karbohidrat kompleks (45-65%)\n• Protein (10-35%)\n• Lemak sehat (20-35%)\n• Sayur dan buah minimal 5 porsi/hari", "nutrisi"},
                {"cardio", "🏃 Latihan kardio baik untuk jantung dan pembakaran lemak.\n\nTips:\n• 150 menit/minggu intensitas sedang\n• Atau 75 menit/minggu intensitas tinggi\n• Variasi: lari, sepeda, renang", "gym"},
                {"pull up", "🤸 Pull up melatih punggung, biceps, dan bahu.\n\nTeknik:\n1. Gantung dengan lengan lurus\n2. Tarik badan hingga dagu melewati bar\n3. Turun perlahan dengan kontrol\n4. Jaga core tetap kencang", "gym"}
            };
            
            String insertQuery = "INSERT INTO keywords (keyword, response, category) VALUES (?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(insertQuery);
            
            for (String[] keyword : defaultKeywords) {
                pstmt.setString(1, keyword[0]);
                pstmt.setString(2, keyword[1]);
                pstmt.setString(3, keyword[2]);
                pstmt.addBatch();
            }
            
            pstmt.executeBatch();
            System.out.println("✅ Default keywords inserted (" + defaultKeywords.length + " keywords)");
        }
    }
    
    // Member operations
    public void saveMember(Member member) {
        String sql;
        if ("mysql".equalsIgnoreCase(this.databaseType)) {
            sql = "INSERT INTO members (chat_id, name, phone_number, registration_date, is_active, last_activity) VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE name = VALUES(name), phone_number = VALUES(phone_number), is_active = VALUES(is_active), last_activity = VALUES(last_activity)";
        } else {
            sql = "INSERT OR REPLACE INTO members (chat_id, name, phone_number, registration_date, is_active, last_activity) VALUES (?, ?, ?, ?, ?, ?)";
        }
        
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, member.getChatId());
            pstmt.setString(2, member.getName());
            pstmt.setString(3, member.getPhoneNumber());
            
            if ("mysql".equalsIgnoreCase(this.databaseType)) {
                pstmt.setTimestamp(4, Timestamp.valueOf(member.getRegistrationDate()));
            } else {
                pstmt.setString(4, member.getRegistrationDate().toString());
            }
            
            pstmt.setBoolean(5, member.isActive());
            pstmt.setString(6, member.getLastActivity());
            pstmt.executeUpdate();
            
            System.out.println("✅ Member saved: " + member.getName());
        } catch (SQLException e) {
            System.err.println("❌ Error saving member: " + e.getMessage());
        }
    }
    
    public Member getMember(Long chatId) {
        String sql = "SELECT * FROM members WHERE chat_id = ?";
        
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, chatId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Member member = new Member();
                member.setChatId(rs.getLong("chat_id"));
                member.setName(rs.getString("name"));
                member.setPhoneNumber(rs.getString("phone_number"));
                
                // Fix DateTime parsing
                if ("mysql".equalsIgnoreCase(this.databaseType)) {
                    Timestamp timestamp = rs.getTimestamp("registration_date");
                    if (timestamp != null) {
                        member.setRegistrationDate(timestamp.toLocalDateTime());
                    }
                } else {
                    String dateStr = rs.getString("registration_date");
                    if (dateStr != null) {
                        try {
                            member.setRegistrationDate(LocalDateTime.parse(dateStr));
                        } catch (Exception e) {
                            // Fallback if parsing fails
                            member.setRegistrationDate(LocalDateTime.now());
                        }
                    }
                }
                
                member.setActive(rs.getBoolean("is_active"));
                member.setLastActivity(rs.getString("last_activity"));
                return member;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting member: " + e.getMessage());
        }
        return null;
    }
    
    public List<Member> getAllMembers() {
        List<Member> members = new ArrayList<>();
        String sql = "SELECT * FROM members WHERE is_active = " + getBooleanTrue() + " ORDER BY registration_date DESC";
        
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Member member = new Member();
                member.setChatId(rs.getLong("chat_id"));
                member.setName(rs.getString("name"));
                member.setPhoneNumber(rs.getString("phone_number"));
                
                // Fix DateTime parsing
                if ("mysql".equalsIgnoreCase(this.databaseType)) {
                    Timestamp timestamp = rs.getTimestamp("registration_date");
                    if (timestamp != null) {
                        member.setRegistrationDate(timestamp.toLocalDateTime());
                    }
                } else {
                    String dateStr = rs.getString("registration_date");
                    if (dateStr != null) {
                        try {
                            member.setRegistrationDate(LocalDateTime.parse(dateStr));
                        } catch (Exception e) {
                            // Fallback if parsing fails
                            member.setRegistrationDate(LocalDateTime.now());
                        }
                    }
                }
                
                member.setActive(rs.getBoolean("is_active"));
                member.setLastActivity(rs.getString("last_activity"));
                members.add(member);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting all members: " + e.getMessage());
        }
        return members;
    }
    
    // Method ini dipanggil di TelegramBotService tapi tidak ada di DatabaseManager
    /**
     * Hapus member dari database berdasarkan Chat ID
     */
    public boolean deleteMember(Long chatId) {
        String sql = "DELETE FROM members WHERE chat_id = ?";
        
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, chatId);
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("✅ Member deleted: Chat ID " + chatId);
                return true;
            } else {
                System.out.println("❌ No member found with Chat ID: " + chatId);
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error deleting member: " + e.getMessage());
            return false;
        }
    }

    // 2. Add partial keyword matching
    public String findPartialKeywordMatch(String input) {
        String sql = "SELECT response FROM keywords WHERE LOWER(keyword) LIKE LOWER(?) AND is_active = " + getBooleanTrue() + " LIMIT 1";
        
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + input.trim() + "%");
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("response");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error finding partial keyword: " + e.getMessage());
        }
        return null;
    }

    // 3. Add keyword management
    public boolean addKeyword(String keyword, String response, String category) {
        String sql = "INSERT INTO keywords (keyword, response, category) VALUES (?, ?, ?)";
        
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, keyword.toLowerCase().trim());
            pstmt.setString(2, response);
            pstmt.setString(3, category);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Error adding keyword: " + e.getMessage());
            return false;
        }
    }

    public boolean removeKeyword(String keyword) {
        String sql = "UPDATE keywords SET is_active = " + getBooleanFalse() + " WHERE LOWER(keyword) = LOWER(?)";
        
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, keyword.trim());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Error removing keyword: " + e.getMessage());
            return false;
        }
    }

    // 4. Add saveMessage method
    public void saveMessage(Message msg) {
        String sql = "INSERT INTO messages (chat_id, direction, message, timestamp) VALUES (?, ?, ?, ?)";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, msg.getChatId());
            pstmt.setString(2, msg.getDirection());
            pstmt.setString(3, msg.getMessage());
            if ("mysql".equalsIgnoreCase(this.databaseType)) {
                pstmt.setTimestamp(4, Timestamp.valueOf(msg.getTimestamp()));
            } else {
                pstmt.setString(4, msg.getTimestamp().toString());
            }
            pstmt.executeUpdate();
            System.out.println("✅ Message saved: " + msg.getDirection() + " | " + msg.getMessage());
        }   
        catch (SQLException e) {
            System.err.println("❌ Error saving message: " + e.getMessage());
        }   
    }
    
    // getMessage method
    public List<Message> getMessagesByChatId(long chatId) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM messages WHERE chat_id = ? ORDER BY timestamp DESC";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, chatId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Message msg = new Message();
                msg.setId(rs.getInt("id"));
                msg.setChatId(rs.getLong("chat_id"));
                msg.setDirection(rs.getString("direction"));
                msg.setMessage(rs.getString("message"));

                if ("mysql".equalsIgnoreCase(this.databaseType)) {
                    Timestamp ts = rs.getTimestamp("timestamp");
                    if (ts != null) msg.setTimestamp(ts.toLocalDateTime());
                } else {
                    String ts = rs.getString("timestamp");
                    if (ts != null) {
                        try {
                            msg.setTimestamp(LocalDateTime.parse(ts));
                        } catch (Exception e) {
                            msg.setTimestamp(LocalDateTime.now());
                        }
                    }
                }

                messages.add(msg);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error retrieving messages: " + e.getMessage());
        }

        return messages;
    }
    
    // Keyword operations
    public void saveKeyword(Keyword keyword) {
        String sql = keyword.getId() == 0 ? 
            "INSERT INTO keywords (keyword, response, category, is_active) VALUES (?, ?, ?, ?)" :
            "UPDATE keywords SET keyword = ?, response = ?, category = ?, is_active = ? WHERE id = ?";
        
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, keyword.getKeyword());
            pstmt.setString(2, keyword.getResponse());
            pstmt.setString(3, keyword.getCategory());
            pstmt.setBoolean(4, keyword.isActive());
            
            if (keyword.getId() != 0) {
                pstmt.setInt(5, keyword.getId());
            }
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Keyword saved: " + keyword.getKeyword());
            }
        } catch (SQLException e) {
            System.err.println("❌ Error saving keyword: " + e.getMessage());
        }
    }
    
    public String getKeywordResponse(String keyword) {
        String sql = "SELECT response FROM keywords WHERE LOWER(keyword) = LOWER(?) AND is_active = " + getBooleanTrue();
        
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, keyword.trim());
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("response");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting keyword response: " + e.getMessage());
        }
        return null;
    }
    
    public List<Keyword> getAllKeywords() {
        List<Keyword> keywords = new ArrayList<>();
        String sql = "SELECT * FROM keywords WHERE is_active = " + getBooleanTrue() + " ORDER BY category, keyword";
        
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Keyword keyword = new Keyword();
                keyword.setId(rs.getInt("id"));
                keyword.setKeyword(rs.getString("keyword"));
                keyword.setResponse(rs.getString("response"));
                keyword.setCategory(rs.getString("category"));
                keyword.setActive(rs.getBoolean("is_active"));
                keywords.add(keyword);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting all keywords: " + e.getMessage());
        }
        return keywords;
    }
    
    // Broadcast operations
    public void saveBroadcastMessage(BroadcastMessage message) {
        String sql = "INSERT INTO broadcast_messages (title, content, category, created_date, is_sent, recipient_count) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, message.getTitle());
            pstmt.setString(2, message.getContent());
            pstmt.setString(3, message.getCategory());
            
            if ("mysql".equalsIgnoreCase(this.databaseType)) {
                pstmt.setTimestamp(4, Timestamp.valueOf(message.getCreatedDate()));
            } else {
                pstmt.setString(4, message.getCreatedDate().toString());
            }
            
            pstmt.setBoolean(5, message.isSent());
            pstmt.setInt(6, message.getRecipientCount());
            pstmt.executeUpdate();
            
            System.out.println("✅ Broadcast message saved: " + message.getTitle());
        } catch (SQLException e) {
            System.err.println("❌ Error saving broadcast message: " + e.getMessage());
        }
    }
    
    public List<BroadcastMessage> getRecentBroadcasts(int limit) {
        List<BroadcastMessage> messages = new ArrayList<>();
        String sql = "SELECT * FROM broadcast_messages WHERE is_sent = " + getBooleanTrue() + " ORDER BY created_date DESC LIMIT ?";
        
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                BroadcastMessage message = new BroadcastMessage();
                message.setId(rs.getInt("id"));
                message.setTitle(rs.getString("title"));
                message.setContent(rs.getString("content"));
                message.setCategory(rs.getString("category"));
                message.setSent(rs.getBoolean("is_sent"));
                message.setRecipientCount(rs.getInt("recipient_count"));
                
                // Fix DateTime parsing
                if ("mysql".equalsIgnoreCase(this.databaseType)) {
                    Timestamp timestamp = rs.getTimestamp("created_date");
                    if (timestamp != null) {
                        message.setCreatedDate(timestamp.toLocalDateTime());
                    }
                } else {
                    String dateStr = rs.getString("created_date");
                    if (dateStr != null) {
                        try {
                            message.setCreatedDate(LocalDateTime.parse(dateStr));
                        } catch (Exception e) {
                            // Fallback if parsing fails
                            message.setCreatedDate(LocalDateTime.now());
                        }
                    }
                }
                
                messages.add(message);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting recent broadcasts: " + e.getMessage());
        }
        return messages;
    }
    
    // Utility method to test database connection
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("❌ Database connection test failed: " + e.getMessage());
            return false;
        }
    }
    
    // Get database statistics
    public String getDatabaseStats() {
        StringBuilder stats = new StringBuilder();
        
        try (Connection conn = getConnection()) {
            // Count members
            String memberCountSql = "SELECT COUNT(*) as count FROM members WHERE is_active = " + getBooleanTrue();
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(memberCountSql)) {
                if (rs.next()) {
                    stats.append("Active Members: ").append(rs.getInt("count")).append("\n");
                }
            }
            
            // Count keywords
            String keywordCountSql = "SELECT COUNT(*) as count FROM keywords WHERE is_active = " + getBooleanTrue();
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(keywordCountSql)) {
                if (rs.next()) {
                    stats.append("Active Keywords: ").append(rs.getInt("count")).append("\n");
                }
            }
            
            // Count broadcasts
            String broadcastCountSql = "SELECT COUNT(*) as count FROM broadcast_messages WHERE is_sent = " + getBooleanTrue();
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(broadcastCountSql)) {
                if (rs.next()) {
                    stats.append("Sent Broadcasts: ").append(rs.getInt("count")).append("\n");
                }
            }
            
            stats.append("Database Type: ").append(this.databaseType.toUpperCase());
            
        } catch (SQLException e) {
            stats.append("Error getting database stats: ").append(e.getMessage());
        }
        
        return stats.toString();
    }

    // Method-method yang perlu ditambahkan ke DatabaseManager.java
// untuk mendukung fitur message management

/**
 * Get messages by direction (incoming/outgoing)
 */
public List<Message> getMessagesByDirection(String direction, int limit) {
    List<Message> messages = new ArrayList<>();
    String sql = "SELECT * FROM messages WHERE direction = ? ORDER BY timestamp DESC LIMIT ?";
    
    try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, direction);
        pstmt.setInt(2, limit);
        ResultSet rs = pstmt.executeQuery();
        
        while (rs.next()) {
            Message msg = new Message();
            msg.setId(rs.getInt("id"));
            msg.setChatId(rs.getLong("chat_id"));
            msg.setDirection(rs.getString("direction"));
            msg.setMessage(rs.getString("message"));
            
            if ("mysql".equalsIgnoreCase(this.databaseType)) {
                Timestamp ts = rs.getTimestamp("timestamp");
                if (ts != null) msg.setTimestamp(ts.toLocalDateTime());
            } else {
                String ts = rs.getString("timestamp");
                if (ts != null) {
                    try {
                        msg.setTimestamp(LocalDateTime.parse(ts));
                    } catch (Exception e) {
                        msg.setTimestamp(LocalDateTime.now());
                    }
                }
            }
            
            messages.add(msg);
        }
    } catch (SQLException e) {
        System.err.println("❌ Error getting messages by direction: " + e.getMessage());
    }
    
    return messages;
}

/**
 * Get all messages with limit
 */
public List<Message> getAllMessages(int limit) {
    List<Message> messages = new ArrayList<>();
    String sql = "SELECT * FROM messages ORDER BY timestamp DESC LIMIT ?";
    
    try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setInt(1, limit);
        ResultSet rs = pstmt.executeQuery();
        
        while (rs.next()) {
            Message msg = new Message();
            msg.setId(rs.getInt("id"));
            msg.setChatId(rs.getLong("chat_id"));
            msg.setDirection(rs.getString("direction"));
            msg.setMessage(rs.getString("message"));
            
            if ("mysql".equalsIgnoreCase(this.databaseType)) {
                Timestamp ts = rs.getTimestamp("timestamp");
                if (ts != null) msg.setTimestamp(ts.toLocalDateTime());
            } else {
                String ts = rs.getString("timestamp");
                if (ts != null) {
                    try {
                        msg.setTimestamp(LocalDateTime.parse(ts));
                    } catch (Exception e) {
                        msg.setTimestamp(LocalDateTime.now());
                    }
                }
            }
            
            messages.add(msg);
        }
    } catch (SQLException e) {
        System.err.println("❌ Error getting all messages: " + e.getMessage());
    }
    
    return messages;
}

/**
 * Search messages by keyword
 */
public List<Message> searchMessages(String keyword, int limit) {
    List<Message> messages = new ArrayList<>();
    String sql = "SELECT * FROM messages WHERE LOWER(message) LIKE LOWER(?) ORDER BY timestamp DESC LIMIT ?";
    
    try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, "%" + keyword + "%");
        pstmt.setInt(2, limit);
        ResultSet rs = pstmt.executeQuery();
        
        while (rs.next()) {
            Message msg = new Message();
            msg.setId(rs.getInt("id"));
            msg.setChatId(rs.getLong("chat_id"));
            msg.setDirection(rs.getString("direction"));
            msg.setMessage(rs.getString("message"));
            
            if ("mysql".equalsIgnoreCase(this.databaseType)) {
                Timestamp ts = rs.getTimestamp("timestamp");
                if (ts != null) msg.setTimestamp(ts.toLocalDateTime());
            } else {
                String ts = rs.getString("timestamp");
                if (ts != null) {
                    try {
                        msg.setTimestamp(LocalDateTime.parse(ts));
                    } catch (Exception e) {
                        msg.setTimestamp(LocalDateTime.now());
                    }
                }
            }
            
            messages.add(msg);
        }
    } catch (SQLException e) {
        System.err.println("❌ Error searching messages: " + e.getMessage());
    }
    
    return messages;
}

/**
 * Get messages by specific chat ID with limit
 */
public List<Message> getMessagesByChatId(Long chatId, int limit) {
    List<Message> messages = new ArrayList<>();
    String sql = "SELECT * FROM messages WHERE chat_id = ? ORDER BY timestamp DESC LIMIT ?";
    
    try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setLong(1, chatId);
        pstmt.setInt(2, limit);
        ResultSet rs = pstmt.executeQuery();
        
        while (rs.next()) {
            Message msg = new Message();
            msg.setId(rs.getInt("id"));
            msg.setChatId(rs.getLong("chat_id"));
            msg.setDirection(rs.getString("direction"));
            msg.setMessage(rs.getString("message"));
            
            if ("mysql".equalsIgnoreCase(this.databaseType)) {
                Timestamp ts = rs.getTimestamp("timestamp");
                if (ts != null) msg.setTimestamp(ts.toLocalDateTime());
            } else {
                String ts = rs.getString("timestamp");
                if (ts != null) {
                    try {
                        msg.setTimestamp(LocalDateTime.parse(ts));
                    } catch (Exception e) {
                        msg.setTimestamp(LocalDateTime.now());
                    }
                }
            }
            
            messages.add(msg);
        }
    } catch (SQLException e) {
        System.err.println("❌ Error getting messages by chat ID: " + e.getMessage());
    }
    
    return messages;
}

/**
 * Delete all messages
 */
public int deleteAllMessages() {
    String sql = "DELETE FROM messages";
    
    try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
        return pstmt.executeUpdate();
    } catch (SQLException e) {
        System.err.println("❌ Error deleting all messages: " + e.getMessage());
        return 0;
    }
}

/**
 * Delete messages by direction
 */
public int deleteMessagesByDirection(String direction) {
    String sql = "DELETE FROM messages WHERE direction = ?";
    
    try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, direction);
        return pstmt.executeUpdate();
    } catch (SQLException e) {
        System.err.println("❌ Error deleting messages by direction: " + e.getMessage());
        return 0;
    }
}

/**
 * Delete old messages (older than specified days)
 */
public int deleteOldMessages(int days) {
    String sql;
    if ("mysql".equalsIgnoreCase(this.databaseType)) {
        sql = "DELETE FROM messages WHERE timestamp < DATE_SUB(NOW(), INTERVAL ? DAY)";
    } else {
        sql = "DELETE FROM messages WHERE timestamp < datetime('now', '-' || ? || ' day')";
    }
    
    try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setInt(1, days);
        return pstmt.executeUpdate();
    } catch (SQLException e) {
        System.err.println("❌ Error deleting old messages: " + e.getMessage());
        return 0;
    }
}

/**
 * Get total message count
 */
public int getTotalMessageCount() {
    String sql = "SELECT COUNT(*) as count FROM messages";
    
    try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); 
         ResultSet rs = stmt.executeQuery(sql)) {
        if (rs.next()) {
            return rs.getInt("count");
        }
    } catch (SQLException e) {
        System.err.println("❌ Error getting total message count: " + e.getMessage());
    }
    return 0;
}

/**
 * Get message count by direction
 */
public int getMessageCountByDirection(String direction) {
    String sql = "SELECT COUNT(*) as count FROM messages WHERE direction = ?";
    
    try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, direction);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            return rs.getInt("count");
        }
    } catch (SQLException e) {
        System.err.println("❌ Error getting message count by direction: " + e.getMessage());
    }
    return 0;
}

/**
 * Get today's message count
 */
public int getTodayMessageCount() {
    String sql;
    if ("mysql".equalsIgnoreCase(this.databaseType)) {
        sql = "SELECT COUNT(*) as count FROM messages WHERE DATE(timestamp) = CURDATE()";
    } else {
        sql = "SELECT COUNT(*) as count FROM messages WHERE DATE(timestamp) = DATE('now')";
    }
    
    try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); 
         ResultSet rs = stmt.executeQuery(sql)) {
        if (rs.next()) {
            return rs.getInt("count");
        }
    } catch (SQLException e) {
        System.err.println("❌ Error getting today's message count: " + e.getMessage());
    }
    return 0;
}

/**
 * Get active members count (members who sent messages recently)
 */
public int getActiveMembersCount() {
    String sql = "SELECT COUNT(DISTINCT chat_id) as count FROM messages WHERE timestamp >= ?";
    LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
    
    try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
        if ("mysql".equalsIgnoreCase(this.databaseType)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(sevenDaysAgo));
        } else {
            pstmt.setString(1, sevenDaysAgo.toString());
        }
        
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            return rs.getInt("count");
        }
    } catch (SQLException e) {
        System.err.println("❌ Error getting active members count: " + e.getMessage());
    }
    return 0;
}

/**
 * Get top active members
 */
public List<Member> getTopActiveMembers(int limit) {
    List<Member> topMembers = new ArrayList<>();
    String sql = """
        SELECT m.*, COUNT(msg.id) as message_count 
        FROM members m 
        LEFT JOIN messages msg ON m.chat_id = msg.chat_id 
        WHERE m.is_active = %s 
        GROUP BY m.chat_id 
        ORDER BY message_count DESC 
        LIMIT ?
    """.formatted(getBooleanTrue());
    
    try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setInt(1, limit);
        ResultSet rs = pstmt.executeQuery();
        
        while (rs.next()) {
            Member member = new Member();
            member.setChatId(rs.getLong("chat_id"));
            member.setName(rs.getString("name"));
            member.setPhoneNumber(rs.getString("phone_number"));
            member.setActive(rs.getBoolean("is_active"));
            member.setLastActivity(rs.getString("last_activity"));
            
            if ("mysql".equalsIgnoreCase(this.databaseType)) {
                Timestamp timestamp = rs.getTimestamp("registration_date");
                if (timestamp != null) {
                    member.setRegistrationDate(timestamp.toLocalDateTime());
                }
            } else {
                String dateStr = rs.getString("registration_date");
                if (dateStr != null) {
                    try {
                        member.setRegistrationDate(LocalDateTime.parse(dateStr));
                    } catch (Exception e) {
                        member.setRegistrationDate(LocalDateTime.now());
                    }
                }
            }
            
            topMembers.add(member);
        }
    } catch (SQLException e) {
        System.err.println("❌ Error getting top active members: " + e.getMessage());
    }
    
    return topMembers;
}

/**
 * Get message count for specific chat ID
 */
public int getMessageCountByChatId(Long chatId) {
    String sql = "SELECT COUNT(*) as count FROM messages WHERE chat_id = ?";
    
    try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setLong(1, chatId);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            return rs.getInt("count");
        }
    } catch (SQLException e) {
        System.err.println("❌ Error getting message count by chat ID: " + e.getMessage());
    }
    return 0;
}

/**
 * Get hourly message statistics for last 24 hours
 */
public java.util.Map<String, Integer> getHourlyMessageStats() {
    java.util.Map<String, Integer> hourlyStats = new java.util.HashMap<>();
    String sql;
    
    if ("mysql".equalsIgnoreCase(this.databaseType)) {
        sql = """
            SELECT HOUR(timestamp) as hour, COUNT(*) as count 
            FROM messages 
            WHERE timestamp >= DATE_SUB(NOW(), INTERVAL 24 HOUR) 
            GROUP BY HOUR(timestamp) 
            ORDER BY hour
        """;
    } else {
        sql = """
            SELECT strftime('%H', timestamp) as hour, COUNT(*) as count 
            FROM messages 
            WHERE timestamp >= datetime('now', '-24 hours') 
            GROUP BY strftime('%H', timestamp) 
            ORDER BY hour
        """;
    }
    
    try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); 
         ResultSet rs = stmt.executeQuery(sql)) {
        while (rs.next()) {
            String hour = rs.getString("hour");
            int count = rs.getInt("count");
            hourlyStats.put(hour, count);
        }
    } catch (SQLException e) {
        System.err.println("❌ Error getting hourly message stats: " + e.getMessage());
    }
    
    return hourlyStats;
}
}

