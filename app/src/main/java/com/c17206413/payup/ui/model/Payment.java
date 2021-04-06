package com.c17206413.payup.ui.model;

import java.util.Currency;

public class Payment {

    //payment information required
    private final String serviceName;
    private final Currency currency;
    private final String username;
    private final String type;
    private final Double amount;
    private final String clientSecret;
    private final String id;
    private final String dateCreated;
    private final String paymentMethod;
    private final String datePaid;
    private final Boolean active;

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

    public String getClientSecret() {
        return clientSecret;
    }

    public String getType() {
        return type;
    }

    public Boolean getActive() {
        return active;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getDatePaid() {
        return datePaid;
    }
}
