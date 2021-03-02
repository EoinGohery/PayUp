package com.c17206413.payup.ui.Model;

public class Payment {

    private String serviceName;
    private String currency;
    private String username;
    private String type;
    private String amount;
    private String clientSecret;
    private String id;
    private Boolean active;

    public Payment(String id, String serviceName, String currency, String username, String amount, String clientSecret, String type, Boolean active) {
        this.serviceName = serviceName;
        this.currency = currency;
        this.username = username;
        this.type = type;
        this.amount = amount;
        this.clientSecret = clientSecret;
        this.id = id;
        this.active = active;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}