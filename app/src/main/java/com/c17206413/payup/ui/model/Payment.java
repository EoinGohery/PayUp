package com.c17206413.payup.ui.model;

import java.util.Currency;

public class Payment {

    //payment information required
    private String serviceName;
    private Currency currency;
    private String username;
    private String type;
    private Double amount;
    private String clientSecret;
    private String id;
    private String dateCreated;
    private String paymentMethod;
    private String datePaid;
    private Boolean active;

    //constructor
    public Payment(String id, String serviceName, Currency currency, String username, Double amount, String clientSecret, String type, Boolean active, String dateCreated, String datePaid, String paymentMethod) {
        this.serviceName = serviceName;
        this.currency = currency;
        this.username = username;
        this.type = type;
        this.amount = amount;
        this.clientSecret = clientSecret;
        this.id = id;
        this.active = active;
        this.dateCreated = dateCreated;
        this.paymentMethod = paymentMethod;
        this.datePaid = datePaid;
    }

    //setters adn getters
    public String getServiceName() {
        return serviceName;
    }

    public Currency getCurrency() {
        return currency;
    }

    public String getUsername() {
        return username;
    }

    public Double getAmount() {
        return amount;
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

    public String getType() {
        return type;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getDatePaid() {
        return datePaid;
    }

    public void setDatePaid(String datePaid) {
        this.datePaid = datePaid;
    }
}
