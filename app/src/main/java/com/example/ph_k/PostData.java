package com.example.ph_k;

import java.util.List;

public class PostData {
    private int postId;
    private String title;
    private String description;
    private String price;
    private List<String> imageUrls;

    private String userId;

    public PostData(int postId, String title, String description, String price, List<String> imageUrls, String updatedUserId) {
        this.postId = postId;
        this.title = title;
        this.description = description;
        this.price = price;
        this.imageUrls = imageUrls;
        this.userId = userId;  // userId를 추가로 초기화
    }

    public int getPostId() {
        return postId;
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
