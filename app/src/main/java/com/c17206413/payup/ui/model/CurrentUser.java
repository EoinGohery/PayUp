package com.c17206413.payup.ui.model;

import android.net.Uri;

public class CurrentUser extends User {

    private String account_id;
    private String email;
    private String provider_id;
    private Boolean darkMode;

    public CurrentUser(String id, String username, Uri imageUrl, String account_id, String provider_id, Boolean darkMode, String email) {
        super(id, username, imageUrl);
        this.account_id = account_id;
        this.email = email;
        this.provider_id = provider_id;
        this.darkMode = darkMode;
    }

    public CurrentUser() {
        super(null, null, null);
        this.account_id = null;
        this.email = null;
        this.provider_id = null;
        this.darkMode = false;
    }

    public void reset() {
        super.setId(null);
        super.setImageUrl(null);
        super.setUsername(null);
        this.account_id = null;
        this.email = null;
        this.provider_id = null;
        this.darkMode = false;
    }

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

    public String getProvider_id() {
        return provider_id;
    }

    public void setProvider_id(String provider_id) {
        this.provider_id = provider_id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
