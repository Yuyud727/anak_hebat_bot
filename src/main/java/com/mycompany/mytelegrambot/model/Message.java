package com.mycompany.mytelegrambot.model;

import java.time.LocalDateTime;

public class Message {
    private int id;
    private long chatId;
    private String direction; // "IN" untuk pesan masuk, "OUT" untuk pesan keluar
    private String message;
    private LocalDateTime timestamp;
    
    // Default constructor
    public Message() {
        this.timestamp = LocalDateTime.now();
    }
    
    // Constructor dengan parameter
    public Message(long chatId, String direction, String message) {
        this.chatId = chatId;
        this.direction = direction;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
    
    // Constructor lengkap
    public Message(int id, long chatId, String direction, String message, LocalDateTime timestamp) {
        this.id = id;
        this.chatId = chatId;
        this.direction = direction;
        this.message = message;
        this.timestamp = timestamp;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public long getChatId() {
        return chatId;
    }
    
    public void setChatId(long chatId) {
        this.chatId = chatId;
    }
    
    public String getDirection() {
        return direction;
    }
    
    public void setDirection(String direction) {
        this.direction = direction;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", chatId=" + chatId +
                ", direction='" + direction + '\'' +
                ", message='" + message + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}