package com.example.ph_k;

public class CreateChatRoomRequest {
    private String auctionId;
    private String bidAmount;

    public CreateChatRoomRequest(String auctionId, String bidAmount) {
        this.auctionId = auctionId;
        this.bidAmount = bidAmount;
    }

    // getter와 setter
    public String getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(String auctionId) {
        this.auctionId = auctionId;
    }

    public String getBidAmount() {
        return bidAmount;
    }

    public void setBidAmount(String bidAmount) {
        this.bidAmount = bidAmount;
    }
}
