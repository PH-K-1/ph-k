package com.example.ph_k;
public class Item {
    private int id;
    private String title;
    private String description;
    private String price;
    private String imageUrl;
    private String userId; // user_id 추가

    public Item(int id, String title, String description, String price, String imageUrl, String userId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.userId = userId; // 초기화
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getPrice() {
        return price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getUserId() {
        return userId;
    }
}
