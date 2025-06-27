package com.mycompany.mytelegrambot.model;

import java.time.LocalDateTime;

public class Member {
    private Long chatId;
    private String name;
    private String phoneNumber;
    private LocalDateTime registrationDate;
    private boolean isActive;
    private String lastActivity;

    public Member() {
        this.registrationDate = LocalDateTime.now();
        this.isActive = true;
    }

    public Member(Long chatId, String name, String phoneNumber) {
        this();
        this.chatId = chatId;
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    // Getters and Setters
    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(String lastActivity) {
        this.lastActivity = lastActivity;
    }

    @Override
    public String toString() {
        return String.format("Member{chatId=%d, name='%s', phone='%s', registrationDate=%s, isActive=%s}", 
                           chatId, name, phoneNumber, registrationDate, isActive);
    }
}