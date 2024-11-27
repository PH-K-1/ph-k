package com.example.ph_k;

public class Item {
    private int id;
    private String title;
    private String description;
    private String price;
    private String imagePath;

    public Item(int id, String title, String description, String price, String imagePath) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.imagePath = imagePath;
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

    public String getImagePath() {
        return imagePath;
    }
}
