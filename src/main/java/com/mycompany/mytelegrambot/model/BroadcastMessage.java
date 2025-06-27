package com.mycompany.mytelegrambot.model;

import java.time.LocalDateTime;

public class BroadcastMessage {
    private int id;
    private String title;
    private String content;
    private String category; // nutrisi, gym, berita
    private LocalDateTime createdDate;
    private boolean isSent;
    private int recipientCount;

    public BroadcastMessage() {
        this.createdDate = LocalDateTime.now();
        this.isSent = false;
        this.recipientCount = 0;
    }

    public BroadcastMessage(String title, String content, String category) {
        this();
        this.title = title;
        this.content = content;
        this.category = category;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public boolean isSent() {
        return isSent;
    }

    public void setSent(boolean sent) {
        isSent = sent;
    }

    public int getRecipientCount() {
        return recipientCount;
    }

    public void setRecipientCount(int recipientCount) {
        this.recipientCount = recipientCount;
    }

    public String getFormattedMessage() {
        return String.format("📢 %s\n\n%s\n\n📅 %s", title, content, createdDate.toString());
    }

    @Override
    public String toString() {
        return String.format("BroadcastMessage{id=%d, title='%s', category='%s', sent=%s, recipients=%d}", 
                           id, title, category, isSent, recipientCount);
    }
}