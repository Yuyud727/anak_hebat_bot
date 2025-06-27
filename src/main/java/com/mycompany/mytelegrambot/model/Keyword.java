package com.mycompany.mytelegrambot.model;

public class Keyword {
    private int id;
    private String keyword;
    private String response;
    private String category; // nutrisi, gym, berita
    private boolean isActive;

    public Keyword() {
        this.isActive = true;
    }

    public Keyword(String keyword, String response, String category) {
        this();
        this.keyword = keyword;
        this.response = response;
        this.category = category;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return String.format("Keyword{id=%d, keyword='%s', category='%s', isActive=%s}", 
                           id, keyword, category, isActive);
    }
}