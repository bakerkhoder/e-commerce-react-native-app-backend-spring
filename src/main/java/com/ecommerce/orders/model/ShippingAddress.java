package com.ecommerce.orders.model;

import jakarta.persistence.Embeddable;

@Embeddable
public class ShippingAddress {
    private String fullName;
    private String phone;
    private String country = "Lebanon";
    private String city;       // e.g. "Beirut", "Akkar" — free text, matches how Lebanese addresses actually work
    private String addressLine;
    private String notes;      // optional delivery instructions

    public ShippingAddress() {}

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getAddressLine() { return addressLine; }
    public void setAddressLine(String addressLine) { this.addressLine = addressLine; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}