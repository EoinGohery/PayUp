package com.c17206413.payup.ui.model;

import android.net.Uri;

//current user extends from this
public class User {

    //information required for users
    private String id;
    private String username;
    private Uri imageUrl;
    private Boolean isSelected;

    //constructor
    public User(String id, String username, Uri imageUrl) {
        this.id = id;
        this.username = username;
        this.imageUrl = imageUrl;
        //defaults to selected as false
        this.isSelected = false;
    }

    //setters and getters
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
