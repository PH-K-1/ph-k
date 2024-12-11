package com.example.ph_k;

import java.util.List;

public class Item {
    private int id;
    private String title;
    private String description;
    private String price;
    private List<String> imageUrls;  // 여러 이미지 URL을 저장할 List로 변경
    private String userId;
    private String deadline;

    // 생성자
    public Item(int id, String title, String description, String price, List<String> imageUrls, String userId, String deadline) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.imageUrls = imageUrls;  // 여러 이미지 URL 초기화
        this.userId = userId;
        this.deadline = deadline;
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

    public List<String> getImageUrls() {
        return imageUrls;  // 여러 이미지 URL 반환
    }

    public String getUserId() {
        return userId;
    }
    public String getDeadline() {
        return deadline;
    }

}