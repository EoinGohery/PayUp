package com.c17206413.payup.ui.Model;

public class PaymentDetails {

    private String id;
    private String username;
    private String imageUrl;
    private Boolean isSelected;

    public PaymentDetails(String id, String username, String imageUrl) {
        this.id = id;
        this.username = username;
        this.imageUrl = imageUrl;
        this.isSelected = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Boolean getSelected() {
        return isSelected;
    }

    public void setSelected(Boolean selected) {
        isSelected = selected;
    }

    public void swapSelected() {
        isSelected = !isSelected;
    }

}
