package com.example.ph_k;

public class DeleteRequest {
    private String item_id;

    public DeleteRequest(String item_id) {
        this.item_id = item_id;
    }

    public String getItemId() {
        return item_id;
    }

    public void setItemId(String item_id) {
        this.item_id = item_id;
    }
}

