package com.c17206413.payup.ui.model;

import android.net.Uri;

public class User {

    private String id;
    private String username;
    private Uri imageUrl;
    private Boolean isSelected;

    public User(String id, String username, Uri imageUrl) {
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

    public Uri getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(Uri imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Boolean getSelected() { return isSelected; }

    public void swapSelected() { isSelected = !isSelected; }
}
