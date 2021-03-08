package com.c17206413.payup.ui.model;

import java.util.Currency;

public class Payment {

    private String serviceName;
    private Currency currency;
    private String username;
    private String type;
    private Double amount;
    private String clientSecret;
    private String id;
    private final String dateTime;
    private Boolean active;

    public Payment(String id, String serviceName, Currency currency, String username, Double amount, String clientSecret, String type, Boolean active, String dateTime) {
        this.serviceName = serviceName;
        this.currency = currency;
        this.username = username;
        this.type = type;
        this.amount = amount;
        this.clientSecret = clientSecret;
        this.id = id;
        this.active = active;
        this.dateTime = dateTime;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
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

    public String getDateTime() {
        return dateTime;
    }
}
