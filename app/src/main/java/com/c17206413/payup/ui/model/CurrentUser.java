package com.c17206413.payup.ui.model;

public class CurrentUser extends User {

    //additional information required for currently logged in user
    private String account_id;
    private String email;
    private Boolean darkMode;

    //current user is always constructed empty
    public CurrentUser() {
        super(null, null, null);
        this.account_id = null;
        this.email = null;
        this.darkMode = false;
    }

    //reset the user object when the current user is logged out
    public void reset() {
        super.setId(null);
        super.setImageUrl(null);
        super.setUsername(null);
        this.account_id = null;
        this.email = null;
        this.darkMode = false;
    }

    //Setters and Getters
    public String getAccount_id() {
        return account_id;
    }

    public void setAccount_id(String account_id) {
        this.account_id = account_id;
    }

    public Boolean getDarkMode() {
        return darkMode;
    }

    public void setDarkMode(Boolean darkMode) {
        this.darkMode = darkMode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
